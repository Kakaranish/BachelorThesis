package GUI.Controllers;

import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.ListItems.*;
import Healthcheck.*;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.AppLoggerEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import Healthcheck.LogsManagement.LogsManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable
{
    public static Image editIcon = new Image(ComputerListCell.class.getResource("/pics/edit.png").toString());
    private static Image addIcon = new Image(ComputerListCell.class.getResource("/pics/add.png").toString());

    private ObservableList<AppLoggerEntry> appLoggerEntries = FXCollections.observableArrayList();

    private ObservableList<ConnectedComputerEntry> connectedComputers = FXCollections.observableArrayList();

    public ObservableList<ComputerItem> computerItemsObservableList = FXCollections.observableArrayList();

    public ObservableList<SshConfigItem> sshConfigItemsObservableList = FXCollections.observableArrayList();


    @FXML
    private ListView<ComputerItem> computerItemsListView;

    @FXML
    private ListView<SshConfigItem> sshConfigItemsListView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button addComputerOrSshConfigButton;

    @FXML
    private TableView<AppLoggerEntry> appLoggerTableView;

    @FXML
    private TableColumn<AppLoggerEntry, String> dateTimeColumn;

    @FXML
    private TableColumn<AppLoggerEntry, String> logTypeColumn;

    @FXML
    private TableColumn<AppLoggerEntry, String> contentColumn;

    @FXML
    private TableView<ConnectedComputerEntry> connectedComputersTableView;

    @FXML
    private TableColumn<ConnectedComputerEntry, String> connectedComputerColumn;

    @FXML
    private Button startOrStopGatheringLogsButton;

    @FXML
    private Button clearAppLogsButton;

    private MainWindowController thisController = this;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private LogsManager _logsManager;

    private boolean _isEditionAndRemovingAllowed = true;

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        AppLogger.SetTargetObservableList(appLoggerEntries);
        AppLogger.SetTargetTableView(appLoggerTableView);
        AppLogger.SetEnabledOutputToConsole(true);

        _computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        _logsManager = new LogsManager(_computersAndSshConfigsManager, this);

        InitializeAppLogsTableView();
        InitializeConnectedComputersTableView();

        InitializeStartOrStopGatheringLogsButton();
        InitializeAddComputerOrSshConfigButton();
        InitializeClearAppLogsButton();
        InitializeTabPaneSelectionListener();

        LoadComputersToListView();
        LoadSshConfigsToListView();
    }

    private void InitializeAppLogsTableView()
    {
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("DateTime"));
        logTypeColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("LogType"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("Content"));

        appLoggerTableView.setItems(appLoggerEntries);
    }

    private void InitializeConnectedComputersTableView()
    {
        connectedComputerColumn.setCellValueFactory(
                new PropertyValueFactory<ConnectedComputerEntry, String>("UsernameAndHost"));

        connectedComputersTableView.setItems(connectedComputers);
    }

    private void InitializeStartOrStopGatheringLogsButton()
    {
        startOrStopGatheringLogsButton.setOnAction(event ->
        {
            if(_logsManager.IsWorking() == false)
            {
                _logsManager.StartWork();
                startOrStopGatheringLogsButton.setText("Stop Gathering Logs");
                _isEditionAndRemovingAllowed = false;
            }
            else
            {
                _logsManager.StopWork();
                startOrStopGatheringLogsButton.setText("Start Gathering Logs");
                _isEditionAndRemovingAllowed = true;
            }
        });
    }

    private void InitializeClearAppLogsButton()
    {
        clearAppLogsButton.setOnAction(event ->
        {
            appLoggerTableView.getItems().clear();
            appLoggerTableView.refresh();
        });
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
        addComputerOrSshConfigButton.setVisible(false);
    }

    private void InitializeTabPaneSelectionListener()
    {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if(IsInAddComputerMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddComputer(this, _computersAndSshConfigsManager));
                addComputerOrSshConfigButton.setVisible(true);
            }
            else if(IsInAddSshConfigMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddSshConfig(this, _computersAndSshConfigsManager));
                addComputerOrSshConfigButton.setVisible(true);
            }
            else
            {
                addComputerOrSshConfigButton.setOnAction(event -> {});
                addComputerOrSshConfigButton.setVisible(false);
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

    private void AddComputer(MainWindowController parentController, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddOrUpdateComputer.fxml"));

            AddOrUpdateComputerController addOrUpdateComputerController =
                    new AddOrUpdateComputerController(parentController, null, _computersAndSshConfigsManager);
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
    }

    private void AddSshConfig(MainWindowController parentController, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AddOrUpdateSshConfig.fxml"));

            AddOrUpdateSshConfigController addOrUpdateSshConfigController =
                    new AddOrUpdateSshConfigController(parentController, null, _computersAndSshConfigsManager);
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
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public void OnCloseAction(WindowEvent event)
    {
        if(_logsManager.IsWorking())
        {
            _logsManager.StopWork();
        }
    }

    public void NotifyChanged(ChangeEvent changeEvent)
    {
        if(changeEvent.Computer != null && changeEvent.ChangeType == ChangeEventType.ADDED)
        {
            ComputerItem computerItemToAdd = new ComputerItem()
            {{
                IsSelected = changeEvent.Computer.IsSelected();
                DisplayedName = changeEvent.Computer.GetDisplayedName();
                Host = changeEvent.Computer.GetHost();
            }};

            computerItemsObservableList.add(computerItemToAdd);
        }
        else if(changeEvent.SshConfig != null && changeEvent.ChangeType == ChangeEventType.ADDED)
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
        return tabNum == 2;
    }

    private boolean IsInAddSshConfigMode(int tabNum)
    {
        return tabNum == 3;
    }

    // ---  LogsManager CALLBACKS  -------------------------------------------------------------------------------------

    public void Callback_LogsManager_StartedWork(List<Computer> selectedComputers)
    {
        for (Computer selectedComputer : selectedComputers)
        {
            connectedComputers.add(new ConnectedComputerEntry()
            {{
                UsernameAndHost = new SimpleStringProperty(
                        selectedComputer.GetSshConfig().GetUsername() + "@" + selectedComputer.GetHost());
            }});
        }
    }

    public void Callback_LogsManager_StoppedWork()
    {
        connectedComputers.clear();
    }

    public void Callback_LogsManager_ComputerDisconnected(Computer computer)
    {
        String usernameAndHostToRemove = computer.GetSshConfig().GetUsername() + "@" + computer.GetHost();
        connectedComputers.removeIf(c -> c.UsernameAndHost.get().equals(usernameAndHostToRemove));
    }

    public void ClearInitListViewOfGatheredComputers()
    {
        connectedComputers.clear();
    }

    public void Callback_LogsManager_StoppedWork_FatalError()
    {
        Utilities.ShowErrorDialog("LogsManager stopped work. Fatal error occurred.");
        connectedComputers.clear();
    }

    public void Callback_LogsManager_StoppedWork_NothingToDo()
    {
        Utilities.ShowInfoDialog("LogsManager stopped work. No connected computers.");
        connectedComputers.clear();
    }

    public boolean IsLogsManagerWorking()
    {
        return _logsManager.IsWorking();
    }

    public boolean IsEditionAllowed()
    {
        return _isEditionAndRemovingAllowed;
    }

    public boolean IsRemovingAllowed()
    {
        return _isEditionAndRemovingAllowed;
    }
}
