package GUI.Controllers;

import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.ListItems.*;
import Healthcheck.*;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.AppLoggerEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.RamLog;
import Healthcheck.Entities.Logs.SwapLog;
import Healthcheck.Entities.SshConfig;
import Healthcheck.LogsManagement.LogsGetter;
import Healthcheck.LogsManagement.LogsManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable
{
    public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss");

    public static Image removeIcon = new Image(ComputerListCell.class.getResource("/pics/remove.png").toString());
    public static Image editIcon = new Image(ComputerListCell.class.getResource("/pics/edit.png").toString());
    public static Image logIcon = new Image(ComputerListCell.class.getResource("/pics/log.png").toString());
    public static Image statsIcon = new Image(ComputerListCell.class.getResource("/pics/stats.png").toString());
    private static Image addIcon = new Image(ComputerListCell.class.getResource("/pics/add.png").toString());
    private static Image refreshIcon = new Image(ComputerListCell.class.getResource("/pics/refresh.png").toString());
    private static Image controlsIcon = new Image(ComputerListCell.class.getResource("/pics/controls.png").toString());

    private ObservableList<AppLoggerEntry> appLoggerEntries = FXCollections.observableArrayList();

    private ObservableList<ConnectedComputerEntry> connectedComputers = FXCollections.observableArrayList();

    public ObservableList<ComputerItem> computerItemsObservableList = FXCollections.observableArrayList();

    public ObservableList<SshConfigItem> sshConfigItemsObservableList = FXCollections.observableArrayList();

    private ObservableList<String> statsScopeObservableList = FXCollections.observableArrayList();

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

    @FXML
    private ChoiceBox statsScopeChoiceBox;

    @FXML
    private Button generateChartsButton;

    @FXML
    private VBox generalStatsVBox;

    @FXML
    private Button groupSettingsButton;

    private MainWindowController thisController = this;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private LogsManager _logsManager;
    private final String AllComputersString = "All Computers";
    private final String SelectedComputersString = "Selected Computers";

    private boolean _logsManagerIsNotWorking = true;

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

        InitializeStartOrStopGatheringLogs();
        InitializeAddComputerOrSshConfigButton();
        InitializeClearAppLogsButton();
        InitializeTabPaneSelectionListener();
        InitializeGenerateChartsButton();
        InitializeGroupSettingsButton();

        statsScopeChoiceBox.setItems(statsScopeObservableList);
        RefreshStatsChoiceBox();

        LoadComputersToListView();
        LoadSshConfigsToListView();
    }

    private void InitializeStartOrStopGatheringLogs()
    {
        startOrStopGatheringLogsButton.setOnAction(event -> _logsManager.StartWork());
        startOrStopGatheringLogsButton.setText("Start Gathering Logs");
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

    private void InitializeGroupSettingsButton()
    {
        ImageView imageView = new ImageView(controlsIcon);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setSmooth(true);
        groupSettingsButton.setGraphic(imageView);
        groupSettingsButton.getStyleClass().add("edit-button");
        groupSettingsButton.setCursor(Cursor.HAND);
        groupSettingsButton.setOnAction(event ->
        {
            // TODO:
        });
        groupSettingsButton.setVisible(false);
    }

    private void InitializeTabPaneSelectionListener()
    {
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        {
            if(IsInAddComputerMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddComputer(this, _computersAndSshConfigsManager));
                addComputerOrSshConfigButton.setVisible(true);
                groupSettingsButton.setVisible(true);
            }
            else if(IsInAddSshConfigMode(newValue.intValue()))
            {
                addComputerOrSshConfigButton.setOnAction(event -> AddSshConfig(this, _computersAndSshConfigsManager));
                addComputerOrSshConfigButton.setVisible(true);
                groupSettingsButton.setVisible(false);
            }
            else
            {
                addComputerOrSshConfigButton.setOnAction(event -> {});
                addComputerOrSshConfigButton.setVisible(false);
                groupSettingsButton.setVisible(false);
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

    private void InitializeGenerateChartsButton()
    {
        generateChartsButton.setOnAction(event ->
        {
            generalStatsVBox.getChildren().clear();
            String selectedScope = (String) statsScopeChoiceBox.getSelectionModel().getSelectedItem();

            List<Computer> computers;
            if(selectedScope.equals(AllComputersString))
            {
                computers = _computersAndSshConfigsManager.GetComputers();
                GenerateGeneralStatsTitleAndNumOfComputersLabels(computers, selectedScope);
            }
            else if(selectedScope.equals(SelectedComputersString))
            {
                computers = _computersAndSshConfigsManager.GetSelectedComputers();
                GenerateGeneralStatsTitleAndNumOfComputersLabels(computers, selectedScope);
            }
            else
            {
                computers = _computersAndSshConfigsManager.GetComputersForClassroom(selectedScope);
                GenerateGeneralStatsTitleAndNumOfComputersLabels(computers, "Classroom: " + selectedScope);
            }

            GenerateLatestAvgNumOfLoggedUsersForComputers(computers);
            GenerateLatestAvgOfAvgCpuUtilForComputers(computers);

            HBox swapAndRamHBox = new HBox();
            swapAndRamHBox.setAlignment(Pos.CENTER);

            PieChart latestAvgOfSwapUsageChart = GetLatestAvgOfSwapUsageForComputers(computers);
            if(latestAvgOfSwapUsageChart != null)
            {
                swapAndRamHBox.getChildren().add(GetLatestAvgOfSwapUsageForComputers(computers));
            }
            else
            {
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setPadding(new Insets(10, 0, 0, 0));

                Label noChartsLabel = new Label();
                noChartsLabel.setText("Average swap usage chart cannot be generated.");
                noChartsLabel.setFont(new Font(20));

                Label noLogsGatheredLabel = new Label();
                noLogsGatheredLabel.setText("No logs gathered.");

                vBox.getChildren().add(noChartsLabel);
                vBox.getChildren().add(noLogsGatheredLabel);

                swapAndRamHBox.getChildren().add(vBox);
            }

            PieChart latestAvgOfRamUsageChart = GenerateLatestAvgOfRamUsageForComputers(computers);
            if(latestAvgOfRamUsageChart != null)
            {
                swapAndRamHBox.getChildren().add(GenerateLatestAvgOfRamUsageForComputers(computers));
            }
            else
            {
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setPadding(new Insets(10, 0, 0, 0));

                Label noChartsLabel = new Label();
                noChartsLabel.setText("Average ram usage chart cannot be generated.");
                noChartsLabel.setFont(new Font(20));

                Label noLogsGatheredLabel = new Label();
                noLogsGatheredLabel.setText("No logs gathered.");

                vBox.getChildren().add(noChartsLabel);
                vBox.getChildren().add(noLogsGatheredLabel);

                swapAndRamHBox.getChildren().add(vBox);
            }

            generalStatsVBox.getChildren().add(swapAndRamHBox);
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

    // ---  GENERATING CHARTS IN GENERAL STATS SECTION  ----------------------------------------------------------------

    private void GenerateGeneralStatsTitleAndNumOfComputersLabels(List<Computer> computers, String title)
    {
        Label titleLabel = new Label();
        titleLabel.setFont(new Font(20));
        titleLabel.setText(title);

        Label numOfComputersLabel = new Label();
        numOfComputersLabel.setText("Number of computers: " + computers.size());

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(titleLabel);
        vbox.getChildren().add(numOfComputersLabel);
        generalStatsVBox.getChildren().add(vbox);
    }

    private void GenerateLatestAvgOfAvgCpuUtilForComputers(List<Computer> computers)
    {
        HBox hbox = new HBox();
        String[] fromPeriod = {"1m", "5m", "15m"};

        for (String from : fromPeriod)
        {
            List<Double> percentageCpuUtils = new ArrayList<>();
            for (Computer computer : computers)
            {
                List<CpuLog> latestCpuLogsForComputer = LogsGetter.GetLatestCpuLogsForComputer(computer);
                if(latestCpuLogsForComputer.size() == 0)
                {
                    continue;
                }

                double percentageCpuUsage = 0;
                if(from.equals("1m"))
                {
                    percentageCpuUsage = latestCpuLogsForComputer.get(0).CpuInfo.Last1MinuteAvgCpuUtil;
                }
                else if(from.equals("5m"))
                {
                    percentageCpuUsage = latestCpuLogsForComputer.get(0).CpuInfo.Last5MinutesAvgCpuUtil;
                }
                else if(from.equals("15m"))
                {
                    percentageCpuUsage = latestCpuLogsForComputer.get(0).CpuInfo.Last15MinutesAvgCpuUtil;
                }

                percentageCpuUtils.add(percentageCpuUsage);
            }

            if(percentageCpuUtils.isEmpty())
            {
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setPadding(new Insets(10, 0, 0, 0));

                Label noChartsLabel = new Label();
                noChartsLabel.setText("Average of average CPU utils chart cannot be generated.");
                noChartsLabel.setFont(new Font(20));

                Label noLogsGatheredLabel = new Label();
                noLogsGatheredLabel.setText("No logs gathered.");

                vBox.getChildren().add(noChartsLabel);
                vBox.getChildren().add(noLogsGatheredLabel);

                generalStatsVBox.getChildren().add(vBox);
                return;
            }

            double percentageCpuUsage = percentageCpuUtils.stream()
                    .mapToDouble(Double::doubleValue).sum() / percentageCpuUtils.size();
            if(percentageCpuUsage > 100)
            {
                percentageCpuUsage = 100;
            }

            PieChart chart = new PieChart();
            chart.setTitle("Latest avg of avgs CPU util from " + from);
            ObservableList<PieChart.Data> pieChartData =
                    FXCollections.observableArrayList(
                            new PieChart.Data("Free CPU - " + String.format("%.2f", 100 - percentageCpuUsage) + "%",
                                    100 - percentageCpuUsage),
                            new PieChart.Data("Used CPU - " + String.format("%.2f", percentageCpuUsage) + "%",
                                    percentageCpuUsage));
            chart.setData(pieChartData);
            hbox.getChildren().add(chart);

        }
        generalStatsVBox.getChildren().add(hbox);
    }

    private PieChart GetLatestAvgOfSwapUsageForComputers(List<Computer> computers)
    {
        List<Double> percentageUsages = new ArrayList<>();

        for (Computer computer : computers)
        {
            List<SwapLog> latestSwapLogsForComputer = LogsGetter.GetLatestSwapLogsForComputer(computer);

            if(latestSwapLogsForComputer.size() == 0) // Computer has no logs in cache and in main db
            {
                continue;
            }

            double used = latestSwapLogsForComputer.get(0).SwapInfo.Used;
            double free = latestSwapLogsForComputer.get(0).SwapInfo.Free;
            if(used == 0 && free == 0)
            {
                continue;
            }

            double percentageUsage = used / (used + free) * 100;
            percentageUsages.add(percentageUsage);
        }

        if(percentageUsages.isEmpty())
        {
            return null;
        }

        double avgPercentageUsage = percentageUsages.stream()
                .mapToDouble(Double::doubleValue).sum() / percentageUsages.size();

        if(avgPercentageUsage > 100)
        {
            avgPercentageUsage = 100;
        }

        PieChart chart = new PieChart();
        chart.setTitle("Latest average Swap usage for computers");
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Free swap - " + String.format("%.2f", 100 - avgPercentageUsage) + "%",
                        100 - avgPercentageUsage),
                new PieChart.Data("Used swap - " + String.format("%.2f", avgPercentageUsage) + "%",
                        avgPercentageUsage));
        chart.setData(pieChartData);

        return chart;
    }

    private PieChart GenerateLatestAvgOfRamUsageForComputers(List<Computer> computers)
    {
        List<Double> percentageUsages = new ArrayList<>();

        for (Computer computer : computers)
        {
            List<RamLog> latestRamLogsForComputer = LogsGetter.GetLatestRamLogsForComputer(computer);
            if(latestRamLogsForComputer.size() == 0)
            {
                continue;
            }

            double used = latestRamLogsForComputer.get(0).RamInfo.Used;
            double free = latestRamLogsForComputer.get(0).RamInfo.Free;
            if(used == 0 && free == 0)
            {
                continue;
            }

            double percentageUsage = used / (used + free) * 100;
            percentageUsages.add(percentageUsage);
        }

        if(percentageUsages.isEmpty())
        {
            return null;
        }

        double avgPercentageUsage =
                percentageUsages.stream().mapToDouble(Double::doubleValue).sum() / percentageUsages.size();
        if(avgPercentageUsage > 100)
        {
            avgPercentageUsage = 100;
        }

        PieChart chart = new PieChart();
        chart.setTitle("Latest average RAM usage for computers");
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Free RAM - "
                                + String.format("%.2f", 100 - avgPercentageUsage) + "%", 100 - avgPercentageUsage),
                        new PieChart.Data("Used RAM - "
                                + String.format("%.2f", avgPercentageUsage) + "%", avgPercentageUsage));
        chart.setData(pieChartData);

        return chart;
    }

    private void GenerateLatestAvgNumOfLoggedUsersForComputers(List<Computer> computers)
    {
        List<Integer> loggedUsersNumList = new ArrayList<>();

        for (Computer computer : computers)
        {
            int latestNumOfUsersForComputer = LogsGetter.GetLatestNumberOfLoggedUsersForComputer(computer);
            loggedUsersNumList.add(latestNumOfUsersForComputer);
        }

        double avgLoggedUsersNum = loggedUsersNumList.stream()
                .mapToInt(Integer::intValue).sum() / (double) computers.size();

        Label numOfLoggedUsersLabel = new Label();
        numOfLoggedUsersLabel.setText("Average number of logged users: " + String.format("%.2f", avgLoggedUsersNum));

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(numOfLoggedUsersLabel);
        generalStatsVBox.getChildren().add(hBox);
    }

    // ---  LogsManager CALLBACKS  -------------------------------------------------------------------------------------

    public void Callback_LogsManager_ComputerDisconnected(Computer computer)
    {
        String usernameAndHostToRemove = computer.GetSshConfig().GetUsername() + "@" + computer.GetHost();
        connectedComputers.removeIf(c -> c.UsernameAndHost.get().equals(usernameAndHostToRemove));
    }

    public void Callback_LogsManager_StartedWork(List<Computer> selectedComputers)
    {
        connectedComputers.clear();

        _logsManagerIsNotWorking = false;
        startOrStopGatheringLogsButton.setText("Stop Gathering Logs");
        startOrStopGatheringLogsButton.setOnAction(event -> _logsManager.StopWork());

        for (Computer selectedComputer : selectedComputers)
        {
            connectedComputers.add(new ConnectedComputerEntry()
            {{
                UsernameAndHost = new SimpleStringProperty(
                        selectedComputer.GetSshConfig().GetUsername() + "@" + selectedComputer.GetHost());
            }});
        }
        connectedComputersTableView.refresh();
    }

    public void Callback_LogsManager_StoppedWork()
    {
        CleanUpGuiComponents();
    }

    public void Callback_LogsManager_InternetConnectionLost()
    {
        CleanUpGuiComponents();

        Utilities.ShowErrorDialog("LogsManager stopped work. Connection with Internet lost..");
    }

    public void Callback_LogsManager_StartGatheringLogsFailed()
    {
        CleanUpGuiComponents();

        Utilities.ShowErrorDialog("LogsManager stopped work. Fatal error occurred.");
    }

    public void Callback_LogsManager_StoppedWork_NothingToDo()
    {
        CleanUpGuiComponents();

        Utilities.ShowInfoDialog("LogsManager stopped work. No connected computers.");
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public void RefreshStatsChoiceBox()
    {
        List<String> classroomNames = _computersAndSshConfigsManager.GetAvailableClassrooms();

        statsScopeObservableList.clear();
        statsScopeObservableList.add(AllComputersString);
        statsScopeObservableList.add(SelectedComputersString);
        statsScopeObservableList.addAll(classroomNames);

        statsScopeChoiceBox.setItems(statsScopeObservableList);
        statsScopeChoiceBox.getSelectionModel().select(0);
    }

    private void CleanUpGuiComponents()
    {
        connectedComputers.clear();

        _logsManagerIsNotWorking = true;
        startOrStopGatheringLogsButton.setText("Start Gathering Logs");
        startOrStopGatheringLogsButton.setOnAction(event -> _logsManager.StartWork());
    }

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
            computerItemsListView.refresh();
        }
        else if(changeEvent.SshConfig != null && changeEvent.ChangeType == ChangeEventType.ADDED)
        {
            SshConfigItem sshConfigItemToAdd = new SshConfigItem()
            {{
                DisplayedName = changeEvent.SshConfig.GetName();
                Username = changeEvent.SshConfig.GetUsername();
            }};

            sshConfigItemsObservableList.add(sshConfigItemToAdd);
            sshConfigItemsListView.refresh();
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

    public void ClearInitListViewOfGatheredComputers()
    {
        connectedComputers.clear();
    }

    public boolean IsLogsManagerWorking()
    {
        return _logsManager.IsWorking();
    }

    public boolean IsEditionAllowed()
    {
        return _logsManagerIsNotWorking;
    }

    public boolean IsRemovingAllowed()
    {
        return _logsManagerIsNotWorking;
    }

    public void RefreshComputersListView()
    {
        computerItemsListView.refresh();
    }

    public void RefreshSshConfigsListView()
    {
        sshConfigItemsListView.refresh();
    }
}
