package GUI.ListItems;

import GUI.Controllers.AddOrUpdateSshConfigController;
import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.Controllers.MainWindowController;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Utilities;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
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

public class SshConfigListCell extends ListCell<SshConfigItem>
{
    private static Image configIcon = new Image(ComputerListCell.class.getResource("/pics/config.png").toString());

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private MainWindowController _controller;

    private HBox content;
    private Text DisplayedName;
    private Text Username;

    public SshConfigListCell(ComputersAndSshConfigsManager computersAndSshConfigsManager, MainWindowController controller)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = controller;

        DisplayedName = new Text();
        DisplayedName.setFont(new Font(17.5));
        Username = new Text();

        VBox vBox = new VBox(DisplayedName, Username);

        ImageView editIconImageView = new ImageView(MainWindowController.editIcon);
        editIconImageView.setFitHeight(16);
        editIconImageView.setFitWidth(16);
        editIconImageView.setSmooth(true);

        Button editButton = new Button();
        editButton.setGraphic(editIconImageView);
        editButton.getStyleClass().add("edit-button");
        editButton.setCursor(Cursor.HAND);

        ImageView configIconImageView = new ImageView(configIcon);
        configIconImageView.setFitHeight(16);
        configIconImageView.setFitWidth(16);
        configIconImageView.setSmooth(true);

        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);

        content = new HBox(configIconImageView, vBox, spacer, editButton);
        content.setPadding(new Insets(0,0,0,7));
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);

        editButton.setOnAction(event ->
        {
            try
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddOrUpdateSshConfig.fxml"));

                SshConfig sshConfig = _computersAndSshConfigsManager.GetGlobalSshConfigByName(DisplayedName.getText());
                AddOrUpdateSshConfigController addOrUpdateSshConfigController =
                        new AddOrUpdateSshConfigController(this, sshConfig, _computersAndSshConfigsManager);

                fxmlLoader.setController(addOrUpdateSshConfigController);

                final Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                Stage stage = new Stage(StageStyle.DECORATED);
                stage.setOnCloseRequest(addOrUpdateSshConfigController::OnCloseAction);
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
            SshConfigItem ssgConfigItemToUpdate = new SshConfigItem()
            {{
                DisplayedName = changeEvent.SshConfig.GetName();
                Username = changeEvent.SshConfig.GetUsername();
            }};

            _controller.sshConfigItemsObservableList.set(getIndex(), ssgConfigItemToUpdate);
        }
        else if(changeEvent.ChangeType == ChangeEventType.REMOVED)
        {
            _controller.sshConfigItemsObservableList.remove(getIndex());
        }
    }

    @Override
    protected void updateItem(SshConfigItem item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null && !empty)
        {
            DisplayedName.setText(item.DisplayedName);
            Username.setText(item.Username);
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
