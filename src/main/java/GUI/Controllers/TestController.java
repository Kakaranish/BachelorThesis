package GUI.Controllers;

import Healthcheck.*;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import Healthcheck.LogsManagement.LogsManager;
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
import javafx.util.Callback;
import java.net.URL;
import java.util.ResourceBundle;

public class TestController implements Initializable
{
    public static Image editIcon = new Image(ComputerListCell.class.getResource("/pics/edit.png").toString());
    private static Image addIcon = new Image(ComputerListCell.class.getResource("/pics/add.png").toString());

    private ObservableList<AppLoggerEntry> appLoggerEntries = FXCollections.observableArrayList();

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

    @FXML
    private TableView<AppLoggerEntry> appLoggerTableView;

    @FXML
    private TableColumn<AppLoggerEntry, String> dateTimeColumn;

    @FXML
    private TableColumn<AppLoggerEntry, String> logTypeColumn;

    @FXML
    private TableColumn<AppLoggerEntry, String> contentColumn;

    @FXML
    private Button startOrStopGatheringLogsButton;

    private TestController thisController = this;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private LogsManager _logsManager;

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        AppLogger.SetTargetObservableList(appLoggerEntries);

        _computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        _logsManager = new LogsManager(_computersAndSshConfigsManager);

        InitializeTableColumns();
        appLoggerTableView.setItems(appLoggerEntries);

        InitializeStartOrStopGatheringLogsButton();
        InitializeAddComputerOrSshConfigButton();
        InitializeTabPaneSelectionListener();

        LoadComputersToListView();
        LoadSshConfigsToListView();
    }

    private void InitializeTableColumns()
    {
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("DateTime"));
        logTypeColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("LogType"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<AppLoggerEntry, String>("Content"));
    }

    private void InitializeStartOrStopGatheringLogsButton()
    {
        startOrStopGatheringLogsButton.setOnAction(event -> _logsManager.StartWork());
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
        return tabNum == 2;
    }

    private boolean IsInAddSshConfigMode(int tabNum)
    {
        return tabNum == 3;
    }
}
