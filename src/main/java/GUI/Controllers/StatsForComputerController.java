package GUI.Controllers;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.*;
import Healthcheck.LogsManagement.LogsGetter;
import Healthcheck.Preferences.Preferences;
import Healthcheck.Utilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.javatuples.Triplet;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatsForComputerController implements Initializable
{
    @FXML
    private VBox cpuVBox;

    @FXML
    private VBox disksVBox;

    @FXML
    private VBox ramVBox;

    @FXML
    private VBox swapVBox;

    @FXML
    private VBox usersVBox;

    public final static int WindowHeight = 720;
    public final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss");

    private Computer _computer;

    public StatsForComputerController(Computer computer)
    {
        _computer = computer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Timestamp now = new Timestamp(new Date().getTime());
        Timestamp from = new Timestamp(LocalDate.now().minusDays(2).toEpochDay());

        Map<String, List<DiskLog>> groupedDisksLogs = LogsGetter.GroupDisksLogsByFileSystem(
                LogsGetter.GetCertainTypeLogsForSingleComputer(
                _computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), from, now).stream()
                .map(l -> (DiskLog) l).collect(Collectors.toList())
        );

        CreateDisksChart(_computer, from, now);
        CreateCpuCharts(_computer, from, now);
        CreateRamChart(_computer, from, now);
        CreateSwapChart(_computer, from, now);
        CreateUsersChart(_computer, from, now);
    }

    private void CreateCpuCharts(Computer computer, Timestamp from, Timestamp now)
    {
        List<CpuLog> diskLogs = LogsGetter.GetCertainTypeLogsForSingleComputer(
                computer, Preferences.PreferenceNameMap.get("CpuInfoPreference"), from, now)
                .stream().map(l -> (CpuLog) l).collect(Collectors.toList());

        for(int i = 1; i <= 3; ++i)
        {
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Timestamp");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Percentage usage");

            final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
            lineChart.setMinHeight(WindowHeight / 2);
            lineChart.setLegendVisible(false);

            String title = null;
            switch (i)
            {
                case 1:
                    title = "Average CPU Util from 1m";
                    break;
                case 2:
                    title = "Average CPU Util from 5m";
                    break;
                case 3:
                    title = "Average CPU Util from 15m";
                    break;
            }
            lineChart.setTitle(title);

            List<Pair<Timestamp, Double>> pairsTimestampCpuUtilAvg = null;
            switch (i)
            {
                case 1:
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp1CpuUtilAvgList(diskLogs);
                    break;

                case 2:
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp5CpuUtilAvgList(diskLogs);
                    break;
                case 3:
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp15CpuUtilAvgList(diskLogs);
                    break;
            }

            XYChart.Series series = new XYChart.Series();
            for (Pair<Timestamp, Double> pairTimestampCpuUtilAvg : pairsTimestampCpuUtilAvg)
            {
                series.getData().add(new XYChart.Data(
                        simpleDateFormat.format(pairTimestampCpuUtilAvg.getKey()),
                        pairTimestampCpuUtilAvg.getValue())
                );
            }

            lineChart.getData().add(series);
            cpuVBox.getChildren().add(lineChart);
        }
    }

    public void CreateDisksChart(Computer computer, Timestamp from, Timestamp now)
    {
        List<DiskLog> diskLogs = LogsGetter.GetCertainTypeLogsForSingleComputer(
                computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), from, now)
                .stream().map(l -> (DiskLog) l).collect(Collectors.toList());
        var disksLogsGroupedByFileSystems = LogsGetter.GroupDisksLogsByFileSystem(diskLogs);

        for (String fileSystem : disksLogsGroupedByFileSystems.keySet())
        {
            var pairsTimestampAvailable = LogsGetter.GetDisksAvailableForFileSystem(
                    disksLogsGroupedByFileSystems,fileSystem);
            var pairsTimestampUsed = LogsGetter.GetDisksUsedForFileSystem(
                    disksLogsGroupedByFileSystems, fileSystem);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Timestamp");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Used");

            StackedBarChart stackedBarChart = new StackedBarChart(xAxis, yAxis);
            stackedBarChart.setTitle(fileSystem + " space");
            stackedBarChart.setMinHeight(WindowHeight / 2);
            stackedBarChart.setLegendVisible(false);

            XYChart.Series usedDataSeries = new XYChart.Series();
            for (Pair<Timestamp, Long> pairTimestampUsed : pairsTimestampUsed)
            {
                usedDataSeries.getData().add(new XYChart.Data(
                        simpleDateFormat.format(pairTimestampUsed.getKey()),
                        Utilities.KilobytesToMegabytes(pairTimestampUsed.getValue()))
                );
            }
            stackedBarChart.getData().add(usedDataSeries);

            XYChart.Series availableDataSeries = new XYChart.Series();
            for (Pair<Timestamp, Long> pairTimestampAvailable: pairsTimestampAvailable)
            {
                availableDataSeries.getData().add(new XYChart.Data(
                        simpleDateFormat.format(pairTimestampAvailable.getKey()),
                        Utilities.KilobytesToMegabytes(pairTimestampAvailable.getValue()))
                );
            }

            stackedBarChart.getData().add(availableDataSeries);
            disksVBox.getChildren().add(stackedBarChart);
        }
    }

    private void CreateRamChart(Computer computer, Timestamp from, Timestamp now)
    {
        List<RamLog> ramLogs = LogsGetter.GetCertainTypeLogsForSingleComputer(
                computer, Preferences.PreferenceNameMap.get("RamInfoPreference"),from, now)
                .stream().map(l -> (RamLog) l).collect(Collectors.toList());
        var tripletsTimestampUsedFree = LogsGetter.GetRamTimestampUsedFreeTripletList(ramLogs);


        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Timestamp");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Usage(KB)");

        StackedBarChart stackedBarChart = new StackedBarChart(xAxis, yAxis);
        stackedBarChart.setMinHeight(WindowHeight);
        stackedBarChart.setStyle("CHART_COLOR_1: #000FFF;");

        XYChart.Series usedDataSeries = new XYChart.Series();
        usedDataSeries.setName("Used");
        for (Triplet<Timestamp, Long, Long> tripletTimestampUsedFree : tripletsTimestampUsedFree)
        {
            usedDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(tripletTimestampUsedFree.getValue0()),
                    tripletTimestampUsedFree.getValue1())
            );
        }

        XYChart.Series freeDataSeries = new XYChart.Series();
        freeDataSeries.setName("Free");
        for (Triplet<Timestamp, Long, Long> tripletTimestampUsedFree : tripletsTimestampUsedFree)
        {
            freeDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(tripletTimestampUsedFree.getValue0()),
                    tripletTimestampUsedFree.getValue2())
            );
        }

        stackedBarChart.getData().add(freeDataSeries);
        stackedBarChart.getData().add(usedDataSeries);

        ramVBox.getChildren().add(stackedBarChart);
    }

    private void CreateSwapChart(Computer computer, Timestamp from, Timestamp now)
    {
        List<SwapLog> swapLogs = LogsGetter.GetCertainTypeLogsForSingleComputer(
                computer, Preferences.PreferenceNameMap.get("SwapInfoPreference"),from, now)
                .stream().map(l -> (SwapLog) l).collect(Collectors.toList());
        var tripletsTimestampUsedFree = LogsGetter.GetSwapTimestampUsedFreeTripletList(swapLogs);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Timestamp");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Usage(KB)");

        StackedBarChart stackedBarChart = new StackedBarChart(xAxis, yAxis);
        stackedBarChart.setMinHeight(WindowHeight);
        stackedBarChart.setStyle("CHART_COLOR_1: #000FFF;");

        XYChart.Series usedDataSeries = new XYChart.Series();
        usedDataSeries.setName("Used");
        for (Triplet<Timestamp, Long, Long> tripletTimestampUsedFree : tripletsTimestampUsedFree)
        {
            usedDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(tripletTimestampUsedFree.getValue0()),
                    tripletTimestampUsedFree.getValue1())
            );
        }

        XYChart.Series freeDataSeries = new XYChart.Series();
        freeDataSeries.setName("Free");
        for (Triplet<Timestamp, Long, Long> tripletTimestampUsedFree : tripletsTimestampUsedFree)
        {
            freeDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(tripletTimestampUsedFree.getValue0()),
                    tripletTimestampUsedFree.getValue2())
            );
        }

        stackedBarChart.getData().add(freeDataSeries);
        stackedBarChart.getData().add(usedDataSeries);

        swapVBox.getChildren().add(stackedBarChart);
    }

    private void CreateUsersChart(Computer computer, Timestamp from, Timestamp now)
    {
        List<UserLog> usersLogs = LogsGetter.GetCertainTypeLogsForSingleComputer(
                computer, Preferences.PreferenceNameMap.get("UsersInfoPreference"), from, now)
                .stream().map(l -> (UserLog) l).collect(Collectors.toList());
        List<Pair<Timestamp, Integer>> pairsTimestampLoggedUsersNum = LogsGetter.GetUsersTimestampNumOfLogged(usersLogs);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Datetime");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Num of Logged Users");

        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setTitle("Logged Users");
        lineChart.setMinHeight(WindowHeight);

        XYChart.Series usersSeries = new XYChart.Series();
        for (Pair<Timestamp, Integer> pairTimestampLoggedUsersNum: pairsTimestampLoggedUsersNum)
        {
            usersSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(pairTimestampLoggedUsersNum.getKey()),
                    pairTimestampLoggedUsersNum.getValue())
            );
        }

        lineChart.getData().add(usersSeries);
        usersVBox.getChildren().add(lineChart);
    }
}
