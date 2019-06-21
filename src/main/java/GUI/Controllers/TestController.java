package GUI.Controllers;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.FakeDataFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

class CustomThing {
    private String _displayedName;
    private String _host;
    public String getDisplayedName() {
        return _displayedName;
    }
    public String GetHost() {
        return _host;
    }
    public CustomThing(String name, String host) {
        super();
        this._displayedName = name;
        this._host = host;
    }
}

class CustomListCell extends ListCell<CustomThing> {
    private HBox content;
    private Text name;
    private Text price;

    private static Image image;
    static
    {
        image = new Image(CustomListCell.class.getResource("/pics/computer.png").toString());
    }

    public CustomListCell() {
        super();
        name = new Text();
        name.setFont(new Font(20));
        price = new Text();
        VBox vBox = new VBox(name, price);;
        Label label = new Label("[Graphic]");
        Image image = new Image(CustomListCell.class.getResource("/pics/pc.png").toString());

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(24);
        imageView.setFitWidth(24);
        imageView.setSmooth(true);
        content = new HBox(imageView, vBox);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(CustomThing item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) { // <== test for null item and empty parameter
            name.setText(item.getDisplayedName());
            price.setText(item.GetHost());
            setGraphic(content);
        } else {
            setGraphic(null);
        }
    }
}

public class TestController implements Initializable
{
    ObservableList data = FXCollections.observableArrayList();
    @FXML
    private ListView<CustomThing> listView;

    @FXML
    private Button btn;

    @FXML
    void dodajCheese(ActionEvent event) {
        data.add(new CustomThing("Cheese spierdala", "XD"));
    }

    public void NotifyChanged(ChangedEvent changeEvent)
    {
        // TODO: Implement

    }

    @FXML
    void editComputer(ActionEvent event) {
        FXMLLoader fxmlLoader = null;
        try
        {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ComputerInfo.fxml"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Computer computer = _computersAndSshConfigsManager.GetComputerByDisplayedName("test-comp-to-remove");

        ComputerInfoController computerInfoController =
                new ComputerInfoController(this, computer, _computersAndSshConfigsManager);
        fxmlLoader.setController(computerInfoController);

        try
        {
            final Parent root = fxmlLoader.load();
            final Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setOnCloseRequest(computerInfoController::OnCloseAction);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(false);

            scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;

    private void LoadData()
    {
        _computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
//        Computer comp = _computersAndSshConfigsManager.GetComputerByDisplayedName("L-G2");
//        FakeDataFactory.CreateCpuLogsForComputer(comp, 10);
//        FakeDataFactory.CreateSwapLogsForComputer(comp, 10);
//
//        Computer comp = FakeDataFactory.AddComputerWithLocalSshConfig("test-comp-to-remove", "test-comp-to-remove");
//        FakeDataFactory.CreateSwapLogsForComputer(comp, 10);
//        FakeDataFactory.CreateCpuLogsForComputer(comp, 10);

        for (Computer computer : _computersAndSshConfigsManager.GetComputers())
        {
            data.add(new CustomThing(computer.GetDisplayedName(), computer.GetHost()));
        }

        listView.setItems(data);
        listView.setCellFactory(new Callback<ListView<CustomThing>, ListCell<CustomThing>>() {
            @Override
            public ListCell<CustomThing> call(ListView<CustomThing> listView) {
                return new CustomListCell();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        LoadData();
    }
}
