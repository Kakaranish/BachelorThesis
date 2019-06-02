package GUI.Controllers;

import Healthcheck.Computer;
import Healthcheck.ComputerManager;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.UsersManager;
import Healthcheck.Utilities;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
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

    @FXML
    void editComputer(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ComputerInfo.fxml"));
        Computer computer = computerManager.GetComputerById(6);
        try
        {

            Encrypter.GetInstance().Decrypt(computer.ComputerEntity.GetEncryptedPassword());
            ComputerInfoController computerInfoController = new ComputerInfoController(computer, computerManager, usersManager);

            fxmlLoader.setController(computerInfoController);


            try
            {
                final Parent root = fxmlLoader.load();
                final Scene scene = new Scene(root);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setResizable(false);

                scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

                stage.setScene(scene);
                stage.show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (EncrypterException e)
        {
            Utilities.ShowFatalErrorDialog("Loading computer failed.\nSSH Password cannot be decrypted.");
        }

    }

    private UsersManager usersManager;
    private ComputerManager computerManager;

    private void LoadData()
    {

        computerManager = new ComputerManager();
        usersManager = new UsersManager();

        for (Computer computer : computerManager.GetComputers())
        {
            data.add(new CustomThing(computer.ComputerEntity.DisplayedName, computer.ComputerEntity.Host));
        }



//        final ListView<CustomThing> listView2 = new ListView<CustomThing>(data);
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
        System.out.println("Before load");
        LoadData();
    }
}
