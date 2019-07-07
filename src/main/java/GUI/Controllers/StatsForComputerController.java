package GUI.Controllers;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.*;
import Healthcheck.LogsManagement.LogsGetter;
import Healthcheck.Preferences.Preferences;
import Healthcheck.Utilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
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
    private Timestamp _from;
    private Timestamp _to;

    public StatsForComputerController(Computer computer, Timestamp from, Timestamp to)
    {
        _computer = computer;
        _from = from;
        _to = to; // AFRICA
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Map<String, List<DiskLog>> groupedDisksLogs = LogsGetter.GroupDisksLogsByFileSystem(
                LogsGetter.GetGivenTypeLogsForComputer(
                _computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), _from, _to).stream()
                .map(l -> (DiskLog) l).collect(Collectors.toList())
        );

        CreateDisksCharts(_computer, _from, _to);
        CreateCpuCharts(_computer, _from, _to);
        CreateRamCharts(_computer, _from, _to);
        CreateSwapCharts(_computer, _from, _to);
        CreateUsersCharts(_computer, _from, _to);
    }

    private void CreateCpuCharts(Computer computer, Timestamp from, Timestamp to)
    {
        HBox pieChartsHBox = new HBox();
        List<CpuLog> latestCpuLogs = LogsGetter.GetLatestGivenTypeLogsForComputer(
                computer, Preferences.CpuInfoPreference).stream().map(l -> (CpuLog) l).collect(Collectors.toList());

        if(latestCpuLogs.isEmpty())
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate.");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("No logs gathered.");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            cpuVBox.getChildren().add(vBox);
            return;
        }

        for(int i = 1; i <= 3; ++i)
        {
            String title = null;
            double latestCpuUtil = 0;
            switch (i)
            {
                case 1:
                    title = "Latest avg CPU util from 1m - " + simpleDateFormat.format(latestCpuLogs.get(0).Timestamp);
                    latestCpuUtil = latestCpuLogs.get(0).CpuInfo.Last1MinuteAvgCpuUtil;
                    break;
                case 2:
                    title = "Latest avg CPU util from 5m - " + simpleDateFormat.format(latestCpuLogs.get(0).Timestamp);
                    latestCpuUtil = latestCpuLogs.get(0).CpuInfo.Last5MinutesAvgCpuUtil;
                    break;
                case 3:
                    title = "Latest avg CPU util from 15m - " + simpleDateFormat.format(latestCpuLogs.get(0).Timestamp);
                    latestCpuUtil = latestCpuLogs.get(0).CpuInfo.Last15MinutesAvgCpuUtil;
                    break;
            }

            ObservableList<PieChart.Data> pieChartData =
                    FXCollections.observableArrayList(
                            new PieChart.Data("Free CPU - "
                                    + String.format("%.2f", 100 - latestCpuUtil) + "%", 100 - latestCpuUtil),
                            new PieChart.Data("Used CPU - "
                                    + String.format("%.2f", latestCpuUtil) + "%", latestCpuUtil));
            final PieChart chart = new PieChart(pieChartData);
            chart.setTitle(title);

            pieChartsHBox.getChildren().add(chart);
        }
        cpuVBox.getChildren().add(pieChartsHBox);

        List<CpuLog> cpuLogs = LogsGetter.GetGivenTypeLogsForComputer(
                computer, Preferences.PreferenceNameMap.get("CpuInfoPreference"), from, to)
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
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp1CpuUtilAvgList(cpuLogs);
                    break;

                case 2:
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp5CpuUtilAvgList(cpuLogs);
                    break;
                case 3:
                    pairsTimestampCpuUtilAvg = LogsGetter.GetCpuTimestamp15CpuUtilAvgList(cpuLogs);
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

    public void CreateDisksCharts(Computer computer, Timestamp from, Timestamp to)
    {
        List<DiskLog> diskLogs = LogsGetter.GetGivenTypeLogsForComputer(
                computer, Preferences.PreferenceNameMap.get("DisksInfoPreference"), from, to)
                .stream().map(l -> (DiskLog) l).collect(Collectors.toList());
        if(diskLogs.isEmpty())
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate.");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("No logs gathered.");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            disksVBox.getChildren().add(vBox);
            return;
        }

        var disksLogsGroupedByFileSystems = LogsGetter.GroupDisksLogsByFileSystem(diskLogs);

        for (String fileSystem : disksLogsGroupedByFileSystems.keySet())
        {
            List<Pair<Timestamp, Long>> pairsTimestampAvailable = LogsGetter.GetDisksAvailableForFileSystem(
                    disksLogsGroupedByFileSystems,fileSystem);
            List<Pair<Timestamp, Long>> pairsTimestampUsed = LogsGetter.GetDisksUsedForFileSystem(
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

    private void CreateRamCharts(Computer computer, Timestamp from, Timestamp to)
    {
        HBox pieChartHBox = new HBox();
        pieChartHBox.setAlignment(Pos.CENTER);

        List<RamLog> latestRamLogs = LogsGetter.GetLatestGivenTypeLogsForComputer(
                computer, Preferences.RamInfoPreference).stream().map(l -> (RamLog) l).collect(Collectors.toList());

        if(latestRamLogs.isEmpty())
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate.");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("No logs gathered.");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            ramVBox.getChildren().add(vBox);
            return;
        }

        long used = latestRamLogs.get(0).RamInfo.Used;
        long free = latestRamLogs.get(0).RamInfo.Free;
        Long buffersCached = latestRamLogs.get(0).RamInfo.BuffersCached;

        long total = free + used + (buffersCached != null ? buffersCached : 0);
        double freePercentage = (double) free / total * 100;
        double usedPercentage = (double) used / total * 100;
        double buffersCachedPercentage = (buffersCached != null ? (double) buffersCached / total : 0)* 100;

        var pieChartObservableList = FXCollections.observableArrayList(
                new PieChart.Data("Free Ram - "
                        + String.format("%.2f", freePercentage)+ "%", freePercentage),
                new PieChart.Data("Used Ram - "
                        + String.format("%.2f", usedPercentage) + "%", usedPercentage));
        if(buffersCached != null)
        {
            pieChartObservableList.add(new PieChart.Data("Buffers/Cached - "
                    + String.format("%.2f", buffersCachedPercentage) + "%", buffersCachedPercentage));
        }

        final PieChart chart = new PieChart(pieChartObservableList);
        chart.setTitle("Latest Ram usage - " + simpleDateFormat.format(latestRamLogs.get(0).Timestamp));
        chart.setMinSize(300, 300);

        pieChartHBox.getChildren().add(chart);
        ramVBox.getChildren().add(pieChartHBox);

        List<RamLog> ramLogs = LogsGetter.GetGivenTypeLogsForComputer(
                computer, Preferences.PreferenceNameMap.get("RamInfoPreference"),from, to)
                .stream().map(l -> (RamLog) l).collect(Collectors.toList());
        var quartetsTimestampUsedFreeBuffersCached = LogsGetter.GetRamTimestampUsedFreeBuffersCachedQuartetList(ramLogs);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Timestamp");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Usage(KB)");

        StackedBarChart stackedBarChart = new StackedBarChart(xAxis, yAxis);
        stackedBarChart.setMinHeight(WindowHeight);
        stackedBarChart.setLegendSide(Side.LEFT);

        XYChart.Series usedDataSeries = new XYChart.Series();
        usedDataSeries.setName("Used");
        for (Quartet<Timestamp, Long, Long, Long> quartetTimestampUsedFreeBuffersCached : quartetsTimestampUsedFreeBuffersCached)
        {
            usedDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(quartetTimestampUsedFreeBuffersCached.getValue0()),
                    quartetTimestampUsedFreeBuffersCached.getValue1())
            );
        }

        XYChart.Series freeDataSeries = new XYChart.Series();
        freeDataSeries.setName("Free");
        for (Quartet<Timestamp, Long, Long, Long> quartetTimestampUsedFreeBuffersCached : quartetsTimestampUsedFreeBuffersCached)
        {
            freeDataSeries.getData().add(new XYChart.Data(
                    simpleDateFormat.format(quartetTimestampUsedFreeBuffersCached.getValue0()),
                    quartetTimestampUsedFreeBuffersCached.getValue2())
            );
        }

        XYChart.Series buffersCachedDataSeries = new XYChart.Series();
        buffersCachedDataSeries.setName("Buffers/Cached");
        for (Quartet<Timestamp, Long, Long, Long> quartetTimestampUsedFreeBuffersCached : quartetsTimestampUsedFreeBuffersCached)
        {
            if(quartetTimestampUsedFreeBuffersCached.getValue3() != null)
            {
                buffersCachedDataSeries.getData().add(new XYChart.Data(
                        simpleDateFormat.format(quartetTimestampUsedFreeBuffersCached.getValue0()),
                        quartetTimestampUsedFreeBuffersCached.getValue3())
                );
            }
        }

        stackedBarChart.getData().add(freeDataSeries);
        stackedBarChart.getData().add(usedDataSeries);
        stackedBarChart.getData().add(buffersCachedDataSeries);

        ramVBox.getChildren().add(stackedBarChart);
    }

    private void CreateSwapCharts(Computer computer, Timestamp from, Timestamp to)
    {
        HBox pieChartHBox = new HBox();
        pieChartHBox.setAlignment(Pos.CENTER);
        List<SwapLog> latestSwapLogs = LogsGetter.GetLatestGivenTypeLogsForComputer(
                computer, Preferences.SwapInfoPreference).stream().map(l -> (SwapLog) l).collect(Collectors.toList());

        if(latestSwapLogs.isEmpty())
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate.");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("No logs gathered.");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            swapVBox.getChildren().add(vBox);
            return;
        }

        double used = latestSwapLogs.get(0).SwapInfo.Used;
        double free = latestSwapLogs.get(0).SwapInfo.Free;
        if(used == 0 && free == 0)
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("Total swap: 0");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            swapVBox.getChildren().add(vBox);
            return;
        }

        double freePercentage = free / (used + free) * 100;

        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Free Swap - "
                                + String.format("%.2f", freePercentage)+ "%", freePercentage),
                        new PieChart.Data("Used Swap - "
                                + String.format("%.2f", 100 - freePercentage) + "%", 100 - freePercentage));
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Latest Swap usage - " + simpleDateFormat.format(latestSwapLogs.get(0).Timestamp));

        pieChartHBox.getChildren().add(chart);
        swapVBox.getChildren().add(pieChartHBox);

        List<SwapLog> swapLogs = LogsGetter.GetGivenTypeLogsForComputer(
                computer, Preferences.PreferenceNameMap.get("SwapInfoPreference"),from, to)
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

    private void CreateUsersCharts(Computer computer, Timestamp from, Timestamp to)
    {
        List<UserLog> usersLogs = LogsGetter.GetGivenTypeLogsForComputer(
                computer, Preferences.UsersInfoPreference, from, to)
                .stream().map(l -> (UserLog) l).collect(Collectors.toList());

        if(usersLogs.isEmpty())
        {
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10, 0, 0, 0));

            Label noChartsLabel = new Label();
            noChartsLabel.setText("No charts to generate.");
            noChartsLabel.setFont(new Font(20));

            Label zeroTotalSwapLabel = new Label();
            zeroTotalSwapLabel.setText("No logs gathered.");

            vBox.getChildren().add(noChartsLabel);
            vBox.getChildren().add(zeroTotalSwapLabel);

            usersVBox.getChildren().add(vBox);
            return;
        }

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
