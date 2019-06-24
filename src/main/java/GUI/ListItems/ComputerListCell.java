package GUI.ListItems;

import GUI.Controllers.AddOrUpdateComputerController;
import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.Controllers.MainWindowController;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Utilities;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ComputerListCell extends ListCell<ComputerItem>
{

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private MainWindowController _controller;

    private HBox content;
    private CheckBox IsSelected;
    private Text DisplayedName;
    private Text Host;

    public ComputerListCell(ComputersAndSshConfigsManager computersAndSshConfigsManager, MainWindowController controller)
    {
        super();

        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = controller;

        IsSelected = new CheckBox();
        IsSelected.setPrefSize(6,6);
        IsSelected.setPadding(new Insets(0,7,0,7));
        IsSelected.setDisable(true);
        DisplayedName = new Text();
        DisplayedName.setFont(new Font(17.5));
        Host = new Text();

        VBox vBox = new VBox(DisplayedName, Host);

        ImageView editIconImageView = new ImageView(MainWindowController.editIcon);
        editIconImageView.setFitHeight(16);
        editIconImageView.setFitWidth(16);
        editIconImageView.setSmooth(true);

        Button editButton = new Button();
        editButton.setGraphic(editIconImageView);
        editButton.getStyleClass().add("edit-button");
        editButton.setCursor(Cursor.HAND);

        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);

        content = new HBox(IsSelected, vBox, spacer, editButton);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);

        editButton.setOnAction(event ->
        {
            try
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddOrUpdateComputer.fxml"));

                Computer computer = _computersAndSshConfigsManager.GetComputerByDisplayedName(DisplayedName.getText());
                AddOrUpdateComputerController addOrUpdateComputerController =
                        new AddOrUpdateComputerController(this, computer, _computersAndSshConfigsManager);

                fxmlLoader.setController(addOrUpdateComputerController);

                final Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                Stage stage = new Stage(StageStyle.DECORATED);
                stage.setOnCloseRequest(addOrUpdateComputerController::OnCloseAction);
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);

                stage.show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Utilities.ShowErrorDialog("Unable to edit computer.");
            }
        });
    }

    public void NotifyChanged(ChangeEvent changeEvent)
    {
        if(changeEvent.ChangeType == ChangeEventType.UPDATED)
        {
            ComputerItem computerItemToUpdate = new ComputerItem()
            {{
                IsSelected= changeEvent.Computer.IsSelected();
                DisplayedName = changeEvent.Computer.GetDisplayedName();
                Host = changeEvent.Computer.GetHost();
            }};

            _controller.computerItemsObservableList.set(getIndex(), computerItemToUpdate);
        }
        else if(changeEvent.ChangeType == ChangeEventType.REMOVED)
        {
            _controller.computerItemsObservableList.remove(getIndex());
        }
    }

    @Override
    protected void updateItem(ComputerItem item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null && !empty)
        {
            DisplayedName.setText(item.DisplayedName);
            Host.setText(item.Host);
            IsSelected.setSelected(item.IsSelected);
            setGraphic(content);
        }
        else
        {
            setGraphic(null);
        }
    }

    public MainWindowController GetController()
    {
        return _controller;
    }
}
