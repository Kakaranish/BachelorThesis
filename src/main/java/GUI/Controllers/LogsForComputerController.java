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
import javafx.event.ActionEvent;
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
    private TableColumn<UserEntry, String> users_sat15Column;

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
        users_sat15Column.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("SAT15"));
        users_ttyColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("TTY"));
        users_userColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("User"));
        users_whatColumn.setCellValueFactory(new PropertyValueFactory<UserEntry, String>("What"));

        usersTableView.setItems(usersEntries);
    }

    private void GetUsersLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<UserEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("UsersInfoPreference"), from, now)
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
    private TableColumn<CpuEntry, Double> cpu_last1MinuteAvgCpuUtil;

    @FXML
    private TableColumn<CpuEntry, Double> cpu_last5MinutesAvgCpuUtilColumn;

    @FXML
    private TableColumn<CpuEntry, Double> cpu_last15MinutesAvgCpuUtilColumn;

    @FXML
    private TableColumn<CpuEntry, Integer> cpu_executingKernelSchedulingEntitiesNumColumn;

    @FXML
    private TableColumn<CpuEntry, Integer> cpu_existingKernelSchedulingEntitiesNumColumn;

    @FXML
    private TableColumn<CpuEntry, Integer> cpu_recentlyCreatedProcessPIDColumn;

    private void InitializeCpuLogsTableView()
    {
        cpu_datetimeColumn.setCellValueFactory(new PropertyValueFactory<CpuEntry, String>("Datetime"));
        cpu_last1MinuteAvgCpuUtil.
                setCellValueFactory(new PropertyValueFactory<CpuEntry, Double>("Last1MinuteAvgCpuUtil"));
        cpu_last5MinutesAvgCpuUtilColumn.
                setCellValueFactory(new PropertyValueFactory<CpuEntry, Double>("Last5MinutesAvgCpuUtil"));
        cpu_last15MinutesAvgCpuUtilColumn
                .setCellValueFactory(new PropertyValueFactory<CpuEntry, Double>("Last15MinutesAvgCpuUtil"));
        cpu_executingKernelSchedulingEntitiesNumColumn
                .setCellValueFactory(new PropertyValueFactory<CpuEntry, Integer>("ExecutingKernelSchedulingEntitiesNum"));
        cpu_existingKernelSchedulingEntitiesNumColumn
                .setCellValueFactory(new PropertyValueFactory<CpuEntry, Integer>("ExistingKernelSchedulingEntitiesNum"));
        cpu_recentlyCreatedProcessPIDColumn
                .setCellValueFactory(new PropertyValueFactory<CpuEntry, Integer>("RecentlyCreatedProcessPID"));

        cpuTableView.setItems(cpuEntries);
    }

    private void GetCpuLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<CpuEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("CpuInfoPreference"), from, now)
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

    private void InitializeRamLogsTableView()
    {
        ram_datetimeColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Datetime"));
        ram_totalColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Total"));
        ram_usedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Used"));
        ram_freeColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Free"));
        ram_sharedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Shared"));
        ram_buffersColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Buffers"));
        ram_cachedColumn.setCellValueFactory(new PropertyValueFactory<RamEntry, Long>("Cached"));

        ramTableView.setItems(ramEntries);
    }

    private void GetRamLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<RamEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("RamInfoPreference"), from, now)
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

    private void GetSwapLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<SwapEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("SwapInfoPreference"), from, now)
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

    private void GetDisksLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<DiskEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), from, now)
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

    private void GetProcessesLogsAndPopulateTableView(Timestamp from)
    {
        Timestamp now = new Timestamp(new Date().getTime());

        List<ProcessEntry> logEntries = LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("ProcessesInfoPreference"), from, now)
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
    private DatePicker datePicker;

    @FXML
    void GetLogs(ActionEvent event)
    {
        LocalDate from = datePicker.getValue();
        if(from == null)
        {
            return;
        }
        Timestamp fromTimestamp = Timestamp.valueOf(from.atStartOfDay());

        if(usersTab.isSelected())
        {
            GetUsersLogsAndPopulateTableView(fromTimestamp);
        }
        else if(cpuTab.isSelected())
        {
            GetCpuLogsAndPopulateTableView(fromTimestamp);
        }
        else if(ramTab.isSelected())
        {
            GetRamLogsAndPopulateTableView(fromTimestamp);
        }
        else if(swapTab.isSelected())
        {
            GetSwapLogsAndPopulateTableView(fromTimestamp);
        }
        else if(disksTab.isSelected())
        {
            GetDisksLogsAndPopulateTableView(fromTimestamp);
        }
        else if(processesTab.isSelected())
        {
            GetProcessesLogsAndPopulateTableView(fromTimestamp);
        }
    }

    @FXML
    void Clear(ActionEvent event)
    {
        datePicker.setValue(null);

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
    }

    // ---  REMOVE LOGS BUTTON  ----------------------------------------------------------------------------------------

    @FXML
    private Button removeCurrentTabLogsButton;

    private void InitializeRemoveCurrentTabLogsButton()
    {
        removeCurrentTabLogsButton.setOnAction(event ->
        {
            LocalDate fromLocalDate = datePicker.getValue();
            if(fromLocalDate == null)
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
                preference = Preferences.CpuInfoPreference;
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

            Timestamp from = Timestamp.valueOf(datePicker.getValue().atStartOfDay());
            String logType = preference.GetClassName().replace("Log", "");
            boolean response = Utilities.ShowYesNoDialog("Remove " + logType + " logs?",
                    "Do you want remove " + logType + " logs from db \nfrom "
                            + simpleDateFormat.format(from) + " to now?");
            if(response == false)
            {
                return;
            }

            Timestamp now = new Timestamp(new Date().getTime());
            try
            {
                LogsMaintainer.RemoveGivenTypeLogsForComputer(_computer, preference, from, now);

                Clear(null);
            }
            catch(Exception e)
            {
                Platform.runLater(() -> AppLogger.Log(LogType.WARNING, ModuleName,
                        "Removing " + logType + " logs from db failed."));
            }
        });
    }

    // ---  FIELDS  ----------------------------------------------------------------------------------------------------

    private Computer _computer;

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    public LogsForComputerController(Computer computer)
    {
        _computer = computer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeUsersLogsTableView();
        InitializeCpuLogsTableView();
        InitializeRamLogsTableView();
        InitializeSwapLogsTableView();
        InitializeDisksLogsTableView();
        InitializeProcessesLogsTableView();

        InitializeRemoveCurrentTabLogsButton();
    }
}
