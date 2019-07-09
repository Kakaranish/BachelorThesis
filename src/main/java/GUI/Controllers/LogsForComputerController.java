package GUI.Controllers;

import GUI.TableViewEntries.*;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBase;
import Healthcheck.LogsManagement.LogsGetter;
import Healthcheck.LogsManagement.LogsMaintainer;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import Healthcheck.Utilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LogsForComputerController implements Initializable
{
    public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss");
    public final static String ModuleName = "LogsForComputerController";

    // ---  USERS TABLEVIEW  -------------------------------------------------------------------------------------------

    @FXML
    private TableView<UserEntry> usersTableView;

    private ObservableList<UserEntry> usersEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<UserEntry, String> users_datetimeColumn;

    @FXML
    private TableColumn<UserEntry, String> users_fromWhereColumn;

    @FXML
    private TableColumn<UserEntry, String> users_idleColumn;

    @FXML
    private TableColumn<UserEntry, String> users_jcpuColumn;

    @FXML
    private TableColumn<UserEntry, String> users_pcpuColumn;

    @FXML
    private TableColumn<UserEntry, String> users_loginAtColumn;

    @FXML
    private TableColumn<UserEntry, String> users_ttyColumn;

    @FXML
    private TableColumn<UserEntry, String> users_userColumn;

    @FXML
    private TableColumn<UserEntry, String> users_whatColumn;

    private void InitializeUsersLogsTableView()
    {
        users_datetimeColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("Datetime"));
        users_fromWhereColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("FromWhere"));
        users_idleColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("Idle"));
        users_jcpuColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("JCPU"));
        users_pcpuColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("PCPU"));
        users_loginAtColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("LoginAt"));
        users_ttyColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("TTY"));
        users_userColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("User"));
        users_whatColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("What"));

        usersTableView.setItems(usersEntries);
    }

    private void GetUsersLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<UserEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("UsersInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (UserEntry) u).collect(Collectors.toList());

        usersEntries.clear();
        usersEntries.addAll(logEntries);
        usersTableView.refresh();

    }

    // ---  CPU TABLEVIEW  ---------------------------------------------------------------------------------------------

    @FXML
    private TableView<CpuEntry> cpuTableView;

    private ObservableList<CpuEntry> cpuEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<CpuEntry, String> cpu_datetimeColumn;

    @FXML
    private TableColumn<CpuEntry, Boolean> cpu_firstBatchColumn;

    @FXML
    private TableColumn<CpuEntry, String> cpu_cpuNameColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_userColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_niceColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_systemColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_idleColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_iowaitColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_irqColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_softIrqColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_stealColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_guestColumn;

    @FXML
    private TableColumn<CpuEntry, Long> cpu_guestNiceColumn;

    private void InitializeCpuLogsTableView()
    {
        cpu_datetimeColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, String>("Datetime"));
        cpu_firstBatchColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Boolean>("FirstBatch"));
        cpu_cpuNameColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, String>("CpuName"));
        cpu_userColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("User"));
        cpu_niceColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Nice"));
        cpu_systemColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("System"));
        cpu_idleColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Idle"));
        cpu_iowaitColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Iowait"));
        cpu_irqColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Irq"));
        cpu_softIrqColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Softirq"));
        cpu_stealColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Steal"));
        cpu_guestColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("Guest"));
        cpu_guestNiceColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, Long>("GuestNice"));

        cpuTableView.setItems(cpuEntries);
    }

    private void GetCpuLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<CpuEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("CpusInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (CpuEntry) u).collect(Collectors.toList());

        cpuEntries.clear();
        cpuEntries.addAll(logEntries);
        cpuTableView.refresh();
    }

    // ---  RAM TABLEVIEW  ---------------------------------------------------------------------------------------------

    @FXML
    private TableView<RamEntry> ramTableView;

    private ObservableList<RamEntry> ramEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<RamEntry, Long> ram_datetimeColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_totalColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_usedColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_freeColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_sharedColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_buffersColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_cachedColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_bufferscachedColumn;

    @FXML
    private TableColumn<RamEntry, Long> ram_availableColumn;

    private void InitializeRamLogsTableView()
    {
        ram_datetimeColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Datetime"));
        ram_totalColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Total"));
        ram_usedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Used"));
        ram_freeColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Free"));
        ram_sharedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Shared"));
        ram_buffersColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Buffers"));
        ram_cachedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Cached"));
        ram_bufferscachedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("BuffersCached"));
        ram_availableColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Available"));

        ramTableView.setItems(ramEntries);
    }

    private void GetRamLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<RamEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("RamInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (RamEntry) u).collect(Collectors.toList());

        ramEntries.clear();
        ramEntries.addAll(logEntries);
        ramTableView.refresh();
    }

    // ---  SWAP TABLEVIEW  --------------------------------------------------------------------------------------------

    @FXML
    private TableView<SwapEntry> swapTableView;

    private ObservableList<SwapEntry> swapEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<SwapEntry, String> swap_datetimeColumn;

    @FXML
    private TableColumn<SwapEntry, Long> swap_totalColumn;

    @FXML
    private TableColumn<SwapEntry, Long> swap_usedColumn;

    @FXML
    private TableColumn<SwapEntry, Long> swap_freeColumn;

    private void InitializeSwapLogsTableView()
    {
        swap_datetimeColumn.setCellValueFactory(new PropertyValueFactory<SwapEntry, String>("Datetime"));
        swap_totalColumn.setCellValueFactory(new PropertyValueFactory<SwapEntry, Long>("Total"));
        swap_usedColumn.setCellValueFactory(new PropertyValueFactory<SwapEntry, Long>("Used"));
        swap_freeColumn.setCellValueFactory(new PropertyValueFactory<SwapEntry, Long>("Free"));

        swapTableView.setItems(swapEntries);
    }

    private void GetSwapLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<SwapEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("SwapInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (SwapEntry) u).collect(Collectors.toList());

        swapEntries.clear();
        swapEntries.addAll(logEntries);
        swapTableView.refresh();
    }

    // ---  DISKS TABLEVIEW  -------------------------------------------------------------------------------------------

    @FXML
    private TableView<DiskEntry> disksTableView;

    private ObservableList<DiskEntry> disksEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<DiskEntry, String> disks_datetimeColumn;

    @FXML
    private TableColumn<DiskEntry, String> disks_filesystemColumn;

    @FXML
    private TableColumn<DiskEntry, Long> disk_blocksNumberColumn;

    @FXML
    private TableColumn<DiskEntry, Long> disks_usedColumn;

    @FXML
    private TableColumn<DiskEntry, Long> disks_availableColumn;

    @FXML
    private TableColumn<DiskEntry, Integer> disks_usePercentage;

    @FXML
    private TableColumn<DiskEntry, String> disks_mountedOnColumn;

    private void InitializeDisksLogsTableView()
    {
        disks_datetimeColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, String>("Datetime"));
        disks_filesystemColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, String>("FileSystem"));
        disk_blocksNumberColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, Long>("BlocksNumber"));
        disks_usedColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, Long>("Used"));
        disks_availableColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, Long>("Available"));
        disks_usePercentage.setCellValueFactory(new PropertyValueFactory<DiskEntry, Integer>("UsePercentage"));
        disks_mountedOnColumn.setCellValueFactory(new PropertyValueFactory<DiskEntry, String>("MountedOn"));

        disksTableView.setItems(disksEntries);
    }

    private void GetDisksLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<DiskEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (DiskEntry) u).collect(Collectors.toList());

        disksEntries.clear();
        disksEntries.addAll(logEntries);
        disksTableView.refresh();
    }

    // ---  PROCESSES TABLEVIEW  ---------------------------------------------------------------------------------------

    @FXML
    private TableView<ProcessEntry> processesTableView;

    private ObservableList<ProcessEntry> processesEntries = FXCollections.observableArrayList();

    @FXML
    private TableColumn<ProcessEntry, String> processes_datetimeColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_userColumn;

    @FXML
    private TableColumn<ProcessEntry, Long> processes_pidColumn;

    @FXML
    private TableColumn<ProcessEntry, Double> processes_cpuPercentageColumn;

    @FXML
    private TableColumn<ProcessEntry, Double> processes_memoryPercentageColumn;

    @FXML
    private TableColumn<ProcessEntry, Long> processes_vszColumn;

    @FXML
    private TableColumn<ProcessEntry, Long> processes_rssColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_ttyColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_statColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_startColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_timeColumn;

    @FXML
    private TableColumn<ProcessEntry, String> processes_commandColumn;

    private void InitializeProcessesLogsTableView()
    {
        processes_datetimeColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("Datetime"));
        processes_userColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("User"));
        processes_pidColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, Long>("PID"));
        processes_cpuPercentageColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, Double>("CPU_Percentage"));
        processes_memoryPercentageColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, Double>("Memory_Percentage"));
        processes_vszColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, Long>("VSZ"));
        processes_rssColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, Long>("RSS"));
        processes_ttyColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("TTY"));
        processes_statColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("Stat"));
        processes_startColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("Start"));
        processes_timeColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("Time"));
        processes_commandColumn.setCellValueFactory(new PropertyValueFactory<ProcessEntry, String>("Command"));

        processesTableView.setItems(processesEntries);
    }

    private void GetProcessesLogsAndPopulateTableView(Timestamp from, Timestamp to)
    {
        List<ProcessEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("ProcessesInfoPreference"), from, to)
                .stream().map(LogBase::ToEntry).map(u -> (ProcessEntry) u).collect(Collectors.toList());

        processesEntries.clear();
        processesEntries.addAll(logEntries);
        processesTableView.refresh();
    }

    // ---  TABS  ------------------------------------------------------------------------------------------------------

    @FXML
    private Tab usersTab;

    @FXML
    private Tab cpuTab;

    @FXML
    private Tab ramTab;

    @FXML
    private Tab swapTab;

    @FXML
    private Tab disksTab;

    @FXML
    private Tab processesTab;

    // ---  OTHER FXML COMPONENTS  -------------------------------------------------------------------------------------

    @FXML
    private Button getLogsButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button removeCurrentTabLogsButton;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private TextField fromTimeTextField;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextField toTimeTextField;

    private Computer _computer;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeUsersLogsTableView();
        InitializeCpuLogsTableView();
        InitializeRamLogsTableView();
        InitializeSwapLogsTableView();
        InitializeDisksLogsTableView();
        InitializeProcessesLogsTableView();

        InitializeDatePickers();
        SetUpTimeValidators();

        InitializeGetLogsButton();
        InitializeRemoveCurrentTabLogsButton();
        InitializeClearButton();
    }

    private void InitializeDatePickers()
    {
        fromDatePicker.setValue(LocalDate.now());
        fromDatePicker.setEditable(false);

        toDatePicker.setValue(LocalDate.now());
        toDatePicker.setEditable(false);
    }

    private void SetUpTimeValidators()
    {
        Utilities.SetUpTimeValidator(fromTimeTextField);
        Utilities.SetUpTimeValidator(toTimeTextField);
    }

    private void InitializeGetLogsButton()
    {
        getLogsButton.setOnAction(event ->
        {
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            String fromTime = fromTimeTextField.getText();
            String toTime = toTimeTextField.getText();

            if(fromDate == null || toDate == null
                    || Utilities.TimeMatchesToTimePattern(fromTime, Utilities.TimePattern) == false
                    || Utilities.TimeMatchesToTimePattern(toTime, Utilities.TimePattern) == false)
            {
                return;
            }

            Timestamp fromTimestamp = Utilities.ConvertDateAndTimeToTimestamp(Utilities.DateFormat, fromDate, fromTime);
            Timestamp toTimestamp = Utilities.ConvertDateAndTimeToTimestamp(Utilities.DateFormat, toDate, toTime);

            GetUsersLogsAndPopulateTableView(fromTimestamp, toTimestamp);
            GetCpuLogsAndPopulateTableView(fromTimestamp, toTimestamp);
            GetRamLogsAndPopulateTableView(fromTimestamp, toTimestamp);
            GetSwapLogsAndPopulateTableView(fromTimestamp, toTimestamp);
            GetDisksLogsAndPopulateTableView(fromTimestamp, toTimestamp);
            GetProcessesLogsAndPopulateTableView(fromTimestamp, toTimestamp);
        });
    }

    private void InitializeClearButton()
    {
        clearButton.setOnAction(event ->
        {
            usersEntries.clear();
            cpuEntries.clear();
            ramEntries.clear();
            swapEntries.clear();
            disksEntries.clear();
            processesEntries.clear();

            usersTableView.refresh();
            cpuTableView.refresh();
            ramTableView.refresh();
            swapTableView.refresh();
            disksTableView.refresh();
            processesTableView.refresh();
        });
    }

    private void InitializeRemoveCurrentTabLogsButton()
    {
        removeCurrentTabLogsButton.setOnAction(event ->
        {
            LocalDate fromLocalDate = fromDatePicker.getValue();
            LocalDate toLocalDate = toDatePicker.getValue();

            if(fromLocalDate == null || toLocalDate == null)
            {
                return;
            }

            IPreference preference = null;
            if(usersTab.isSelected())
            {
                preference = Preferences.UsersInfoPreference;
            }
            else if(cpuTab.isSelected())
            {
                preference = Preferences.CpusInfoPreference;
            }
            else if(ramTab.isSelected())
            {
                preference = Preferences.RamInfoPreference;
            }
            else if(swapTab.isSelected())
            {
                preference = Preferences.SwapInfoPreference;
            }
            else if(disksTab.isSelected())
            {
                preference = Preferences.DisksInfoPreference;
            }
            else if(processesTab.isSelected())
            {
                preference = Preferences.ProcessesInfoPreference;
            }

            Timestamp from = Utilities.ConvertDateAndTimeToTimestamp(
                    simpleDateFormat, fromLocalDate, fromTimeTextField.getText());
            Timestamp to = Utilities.ConvertDateAndTimeToTimestamp(
                    simpleDateFormat, toLocalDate, toTimeTextField.getText());

            String logType = preference.GetClassName().replace("Log", "");
            boolean response = Utilities.ShowYesNoDialog("Remove " + logType + " logs?",
                    "Do you want remove " + logType + " logs from db \n" +
                            "from " + simpleDateFormat.format(from) +
                            "to " + simpleDateFormat.format(to) + "?");
            if(response == false)
            {
                return;
            }

            Timestamp now = new Timestamp(new Date().getTime());
            try
            {
                LogsMaintainer.RemoveGivenTypeLogsForComputer(_computer, preference, from, now);

                clearButton.fire();
            }
            catch(Exception e)
            {
                Platform.runLater(() -> AppLogger.Log(LogType.WARNING, ModuleName,
                        "Removing " + logType + " logs from db failed."));
            }
        });
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public LogsForComputerController(Computer computer)
    {
        _computer = computer;
    }
}
