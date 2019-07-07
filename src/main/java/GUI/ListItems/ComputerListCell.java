package GUI.ListItems;

import GUI.Controllers.*;
import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
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
import java.io.IOException;

public class ComputerListCell extends ListCell<ComputerItem>
{
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private MainWindowController _controller;

    private HBox content;
    private CheckBox IsSelected;
    private Text DisplayedName;
    private Text Host;

    private Boolean IsSelectedValue = null;

    public ComputerListCell(ComputersAndSshConfigsManager computersAndSshConfigsManager, MainWindowController controller)
    {
        super();

        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = controller;

        IsSelected = new CheckBox();
        IsSelected.setPrefSize(6,6);
        IsSelected.setPadding(new Insets(0,7,0,7));
        DisplayedName = new Text();
        DisplayedName.setFont(new Font(17.5));
        Host = new Text();

        VBox vBox = new VBox(DisplayedName, Host);

        ImageView removeIconImageView = new ImageView(MainWindowController.removeIcon);
        removeIconImageView.setFitHeight(16);
        removeIconImageView.setFitWidth(16);
        removeIconImageView.setSmooth(true);

        Button removeButton = new Button();
        removeButton.setGraphic(removeIconImageView);
        removeButton.getStyleClass().add("remove-button");
        removeButton.setCursor(Cursor.HAND);

        ImageView statsIconImageView = new ImageView(MainWindowController.statsIcon);
        statsIconImageView.setFitHeight(16);
        statsIconImageView.setFitWidth(16);
        statsIconImageView.setSmooth(true);

        Button statsButton = new Button();
        statsButton.setGraphic(statsIconImageView);
        statsButton.getStyleClass().add("stats-button");
        statsButton.setCursor(Cursor.HAND);

        ImageView logIconImageView = new ImageView(MainWindowController.logIcon);
        logIconImageView.setFitHeight(16);
        logIconImageView.setFitWidth(16);
        logIconImageView.setSmooth(true);

        Button logButton = new Button();
        logButton.setGraphic(logIconImageView);
        logButton.getStyleClass().add("log-button");
        logButton.setCursor(Cursor.HAND);

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

        content = new HBox(IsSelected, vBox, spacer, removeButton, statsButton, logButton, editButton);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);

        InitRemoveButton(removeButton);
        InitStatsButton(statsButton);
        InitLogButton(logButton);
        InitEditButtonAction(editButton);
        InitIsSelectedCheckbox();
    }

    private void InitIsSelectedCheckbox()
    {
        IsSelected.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            IsSelectedValue = newValue;

            Computer computer = _computersAndSshConfigsManager.GetComputerByDisplayedName(DisplayedName.getText());
            computer.SetSelected(IsSelectedValue);
            computer.UpdateInDb();
        });
    }

    private void InitRemoveButton(Button removeButton)
    {
        removeButton.setOnAction(event ->
        {
            boolean response = Utilities.ShowYesNoDialog("Discard changes?", "Do you want to discard changes?");
            if(response == false)
            {
                return;
            }

            Computer computerToRemove =
                    _computersAndSshConfigsManager.GetComputerByDisplayedName(DisplayedName.getText());

            computerToRemove.RemoveFromDb();
            NotifyChanged(new ChangeEvent()
            {{
                ChangeType = ChangeEventType.REMOVED;
                Computer = computerToRemove;
            }});

            _controller.RefreshStatsChoiceBox();
        });
    }

    private void InitStatsButton(Button statsButton)
    {
        statsButton.setOnAction(event ->
        {
            try
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DateTimePopup.fxml"));

                Computer computer = _computersAndSshConfigsManager.GetComputerByDisplayedName(DisplayedName.getText());
                DateTimePopupController dateTimePopupController = new DateTimePopupController(computer);

                fxmlLoader.setController(dateTimePopupController);

                final Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                Stage stage = new Stage(StageStyle.DECORATED);
                stage.setResizable(false);
                stage.setScene(scene);
                stage.setTitle("Choose date and time");

                stage.show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    private void InitLogButton(Button logButton)
    {
        logButton.setOnAction(event ->
        {
            try
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LogsForComputer.fxml"));

                Computer computer = _computersAndSshConfigsManager.GetComputerByDisplayedName(DisplayedName.getText());
                LogsForComputerController logsForComputerController = new LogsForComputerController(computer);

                fxmlLoader.setController(logsForComputerController);

                final Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

                Stage stage = new Stage(StageStyle.DECORATED);
                stage.setResizable(false);
                stage.setTitle(computer.GetUsernameAndHost() + " - Logs");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);

                stage.show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Utilities.ShowErrorDialog("Unable to show logs for computer.");
            }
        });
    }

    private void InitEditButtonAction(Button editButton)
    {
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
            IsSelectedValue = changeEvent.Computer.IsSelected();

            ComputerItem computerItemToUpdate = new ComputerItem()
            {{
                IsSelected = IsSelectedValue;
                DisplayedName = changeEvent.Computer.GetDisplayedName();
                Host = changeEvent.Computer.GetHost();
            }};

            _controller.computerItemsObservableList.set(getIndex(), computerItemToUpdate);
            _controller.RefreshComputersListView();
            _controller.RefreshStatsChoiceBox();
        }
        else if(changeEvent.ChangeType == ChangeEventType.REMOVED)
        {
            _controller.computerItemsObservableList.remove(getIndex());
            _controller.RefreshComputersListView();
            _controller.RefreshStatsChoiceBox();
        }
    }

    @Override
    protected void updateItem(ComputerItem item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null && !empty)
        {
            if(IsSelectedValue == null)
            {
                IsSelectedValue = item.IsSelected;
            }

            IsSelected.setSelected(IsSelectedValue);
            DisplayedName.setText(item.DisplayedName);
            Host.setText(item.Host);

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
