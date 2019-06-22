package GUI.Controllers;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Utilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import java.net.URL;
import java.util.ResourceBundle;

public class TestController implements Initializable
{
    public static Image editIcon = new Image(ComputerListCell.class.getResource("/pics/edit.png").toString());
    private static Image addIcon = new Image(ComputerListCell.class.getResource("/pics/add.png").toString());

    public ObservableList<ComputerItem> computerItemsObservableList = FXCollections.observableArrayList();

    @FXML
    private ListView<ComputerItem> computerItemsListView;

    public ObservableList<SshConfigItem> sshConfigItemsObservableList = FXCollections.observableArrayList();

    @FXML
    private ListView<SshConfigItem> sshConfigItemsListView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button addComputerOrSshConfigButton;

    private TestController thisController = this;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        _computersAndSshConfigsManager = new ComputersAndSshConfigsManager();

        InitializeAddComputerOrSshConfigButton();
        InitializeTabPaneSelectionListener();

        LoadComputersToListView();
        LoadSshConfigsToListView();
    }

    private void InitializeAddComputerOrSshConfigButton()
    {
        ImageView imageView = new ImageView(addIcon);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setSmooth(true);
        addComputerOrSshConfigButton.setGraphic(imageView);
        addComputerOrSshConfigButton.getStyleClass().add("edit-button");
        addComputerOrSshConfigButton.setCursor(Cursor.HAND);
        addComputerOrSshConfigButton.setOnAction(event -> AddComputer(this, _computersAndSshConfigsManager));
    }

    private void InitializeTabPaneSelectionListener()
    {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if(IsInAddComputerMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddComputer(this, _computersAndSshConfigsManager));
            }
            else if(IsInAddSshConfigMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddSshConfig(this, _computersAndSshConfigsManager));
            }
            else
            {
                addComputerOrSshConfigButton.setOnAction(event -> {});
            }
        });
    }

    private void LoadComputersToListView()
    {
        for (Computer computer : _computersAndSshConfigsManager.GetComputers())
        {
            computerItemsObservableList.add(new ComputerItem()
            {{
                IsSelected = computer.IsSelected();
                DisplayedName = computer.GetDisplayedName();
                Host = computer.GetHost();
            }});
        }

        computerItemsListView.setItems(computerItemsObservableList);
        computerItemsListView.setCellFactory(new Callback<ListView<ComputerItem>, ListCell<ComputerItem>>()
        {
            @Override
            public ListCell<ComputerItem> call(ListView<ComputerItem> listView)
            {
                return new ComputerListCell(_computersAndSshConfigsManager, thisController);
            }
        });
    }

    private void LoadSshConfigsToListView()
    {
        for (SshConfig sshConfig: _computersAndSshConfigsManager.GetGlobalSshConfigs())
        {
            sshConfigItemsObservableList.add(new SshConfigItem()
            {{
                DisplayedName = sshConfig.GetName();
                Username = sshConfig.GetUsername();
            }});
        }

        sshConfigItemsListView.setItems(sshConfigItemsObservableList);
        sshConfigItemsListView.setCellFactory(new Callback<ListView<SshConfigItem>, ListCell<SshConfigItem>>()
        {
            @Override
            public ListCell<SshConfigItem> call(ListView<SshConfigItem> listView)
            {
                return new SshConfigListCell(_computersAndSshConfigsManager, thisController);
            }
        });
    }

    // ---  ADD ACTIONS  -----------------------------------------------------------------------------------------------

    private void AddComputer(TestController parentController, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ComputerInfo.fxml"));

            ComputerInfoController computerInfoController =
                    new ComputerInfoController(parentController, null, _computersAndSshConfigsManager);
            fxmlLoader.setController(computerInfoController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setOnCloseRequest(computerInfoController::OnCloseAction);
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
    }

    private void AddSshConfig(TestController parentController, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SshConfig.fxml"));

            SshConfigController sshConfigController =
                    new SshConfigController(parentController, null, _computersAndSshConfigsManager);
            fxmlLoader.setController(sshConfigController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setOnCloseRequest(sshConfigController::OnCloseAction);
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
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public void NotifyChanged(ChangeEvent changeEvent)
    {
        if(changeEvent.Computer != null && changeEvent.ChangeType == ChangedEventType.ADDED)
        {
            ComputerItem computerItemToAdd = new ComputerItem()
            {{
                IsSelected = changeEvent.Computer.IsSelected();
                DisplayedName = changeEvent.Computer.GetDisplayedName();
                Host = changeEvent.Computer.GetHost();
            }};

            computerItemsObservableList.add(computerItemToAdd);
        }
        else if(changeEvent.SshConfig != null && changeEvent.ChangeType == ChangedEventType.ADDED)
        {
            SshConfigItem sshConfigItemToAdd = new SshConfigItem()
            {{
                DisplayedName = changeEvent.SshConfig.GetName();
                Username = changeEvent.SshConfig.GetUsername();
            }};

            sshConfigItemsObservableList.add(sshConfigItemToAdd);
        }
    }

    private boolean IsInAddComputerMode(int tabNum)
    {
        return tabNum == 0;
    }

    private boolean IsInAddSshConfigMode(int tabNum)
    {
        return tabNum == 1;
    }
}
