package Healthcheck.LogsManagement;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.CacheDatabaseManager;
import Healthcheck.DatabaseManagement.MainDatabaseManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.*;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import javafx.util.Pair;
import org.hibernate.Session;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsGetter
{
    // ---  GETTING LATEST LOGS  ---------------------------------------------------------------------------------------

    public static Map<Computer, List<LogBase>> GetLatestGivenTypeLogsForComputers(
            List<Computer> computers, IPreference iPreference)
    {
        Map<Computer, List<LogBase>> groupedLogs = new HashMap<>();
        for (Computer computer : computers)
        {
            groupedLogs.put(computer, GetLatestGivenTypeLogsForComputer(computer, iPreference));
        }

        return groupedLogs;
    }

    public static List<LogBase> GetLatestGivenTypeLogsForComputer(Computer computer, IPreference preference)
    {
        List<LogBase> logsFromCacheDb = GetLatestGivenTypeLogsForComputerFromCacheDb(computer, preference);
        if(logsFromCacheDb.isEmpty())
        {
            List<LogBase> logsFromMainDb = GetLatestGivenTypeLogsForComputerFromMainDb(computer, preference);
            if(logsFromMainDb.isEmpty() == false)
            {
                CacheLogsSaver.CacheGivenTypeLogsForComputer(logsFromMainDb, preference);
            }

            return logsFromMainDb;
        }

        return logsFromCacheDb;
    }

    public static List<LogBase> GetLatestGivenTypeLogsForComputerFromCacheDb(Computer computer, IPreference preference)
    {
        String cacheLogClassName = preference.GetClassName().replace("Log", "CacheLog");
        String attemptErrorMessage = "Attempt of getting latest cache logs from " + cacheLogClassName + " for '"
                + computer.GetUsernameAndHost() + "' failed.";

        String hql = "from " + cacheLogClassName + " t where t.ComputerId = :computerId";
        Session session = CacheDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computerId", computer.GetId());

        List<CacheLogBase> receivedLogs = CacheDatabaseManager.ExecuteSelectQueryWithRetryPolicy(
                session, query, "LogsGetter", attemptErrorMessage);
        session.close();

        return receivedLogs.stream().map(l -> l.ToLog(computer)).collect(Collectors.toList());
    }

    public static List<LogBase> GetLatestGivenTypeLogsForComputerFromMainDb(Computer computer, IPreference preference)
    {
        String attemptErrorMessage = "Attempt of getting latest cache logs from " + preference.GetClassName() + " for '"
                + computer.GetUsernameAndHost() + "' failed.";

        String hql = "from " + preference.GetClassName() + " t where t.Timestamp = " +
                "(select max(Timestamp) from " + preference.GetClassName() + " tt where tt.Computer = :computer)";
        Session session = MainDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computer", computer);

        List<LogBase> receivedLogs = MainDatabaseManager.ExecuteSelectQueryWithRetryPolicy(
                session, query, "LogsGetter", attemptErrorMessage);
        session.close();

        return receivedLogs;
    }

    public static List<LogBase> GetCertainTypeLogsForClassroom(
            String classroom, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        String attemptErrorMessage = "Attempt of getting logs from "
               + preference.GetClassName() + " for '" + classroom + "' classroom failed.";
        String hql = "from " + preference.GetClassName() + " t" +
                " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                " and t.Computer.Classroom = '" + classroom + "'";

        Session session = MainDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        List<LogBase> receivedLogs = MainDatabaseManager.ExecuteSelectQueryWithRetryPolicy(
                session, query, "LogsGetter", attemptErrorMessage);
        session.close();

        return receivedLogs;
    }

    public static Map<Computer, List<LogBase>> GetLatestGivenTypeGroupedByComputerLogsForClassroom(
            String classroom, IPreference preference, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        List<Computer> computersInClassroom = computersAndSshConfigsManager.GetComputersForClassroom(classroom);
        return GetLatestGivenTypeLogsForComputers(computersInClassroom, preference);
    }

    // --- OTHER GETTING LOGS  -----------------------------------------------------------------------------------------

    public static List<LogBase> GetGivenTypeLogsForComputer(
            Computer computer, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        String attemptErrorMessage = "Attempt of getting logs from "
                + preference.GetClassName() + " for '" + computer.GetUsernameAndHost() + "' failed.";
        String hql = "from " + preference.GetClassName() + " t" +
                " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                " and t.Computer = :computer";

        Session session = MainDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computer", computer);

        List<LogBase> receivedLogs = MainDatabaseManager.ExecuteSelectQueryWithRetryPolicy(
                session, query, "LogsGetter", attemptErrorMessage);
        session.close();

        return receivedLogs;
    }

    // --- SWAP LOGS  --------------------------------------------------------------------------------------------------

    public static List<Triplet<Timestamp, Long, Long>> GetSwapTimestampUsedFreeTripletList(List<SwapLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Triplet<Timestamp, Long, Long>(
                s.Timestamp, s.SwapInfo.Used, s.SwapInfo.Free)).collect(Collectors.toList());
    }

    public static List<SwapLog> GetLatestSwapLogsForComputer(Computer computer)
    {
        return LogsGetter.GetLatestGivenTypeLogsForComputer(computer, Preferences.SwapInfoPreference)
                .stream().map(l -> (SwapLog) l).collect(Collectors.toList());
    }

    // --- RAM LOGS  ---------------------------------------------------------------------------------------------------

    public static List<Triplet<Timestamp, Long, Long>> GetRamTimestampUsedFreeTripletList(List<RamLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Triplet<Timestamp, Long, Long>(
                s.Timestamp, s.RamInfo.Used, s.RamInfo.Free)).collect(Collectors.toList());
    }

    public static List<Quartet<Timestamp, Long, Long, Long>> GetRamTimestampUsedFreeBuffersCachedQuartetList(List<RamLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Quartet<Timestamp, Long, Long, Long>(
                s.Timestamp, s.RamInfo.Used, s.RamInfo.Free, s.RamInfo.BuffersCached)).collect(Collectors.toList());
    }

    public static List<RamLog> GetLatestRamLogsForComputer(Computer computer)
    {
        return LogsGetter.GetLatestGivenTypeLogsForComputer(computer, Preferences.RamInfoPreference)
                .stream().map(l -> (RamLog) l).collect(Collectors.toList());
    }

    // --- USERS LOGS  -------------------------------------------------------------------------------------------------

    public static List<Pair<Timestamp, Integer>> GetUsersTimestampNumOfLogged(List<UserLog> usersLogs)
    {
        return usersLogs.stream().collect(Collectors.groupingBy(u -> u.Timestamp))
                .entrySet().stream().map(u -> new Pair<Timestamp, Integer>(u.getKey(),
                        u.getValue().get(0).UserInfo == null
                                || u.getValue().get(0).UserInfo.User == null ? 0 : u.getValue().size()))
                .collect(Collectors.toList());
    }

    public static int GetLatestNumberOfLoggedUsersForComputer(Computer computer)
    {
        return LogsGetter.GetLatestGivenTypeLogsForComputer(computer, Preferences.UsersInfoPreference).size();
    }

    // --- DISKS LOGS  -------------------------------------------------------------------------------------------------

    public static Map<String, List<DiskLog>> GroupDisksLogsByFileSystem(List<DiskLog> disksLogs)
    {
        return disksLogs.stream().collect(Collectors.groupingBy(d -> d.DiskInfo.FileSystem));
    }

    public static List<Pair<Timestamp, Long>> GetDisksAvailableForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).stream()
                .map(l -> new Pair<Timestamp, Long>(l.Timestamp, l.DiskInfo.Available))
                .collect(Collectors.toList());
    }

    public static List<Pair<Timestamp, Long>> GetDisksUsedForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).stream()
                .map(l -> new Pair<Timestamp, Long>(l.Timestamp, l.DiskInfo.Used))
                .collect(Collectors.toList());
    }

    // --- CPU LOGS  ---------------------------------------------------------------------------------------------------

    public static List<CpuLog> GetLatestCpuLogsForComputer(Computer computer)
    {
        return LogsGetter.GetLatestGivenTypeLogsForComputer(computer, Preferences.CpusInfoPreference)
                .stream().map(l -> (CpuLog) l).collect(Collectors.toList());
    }

    public static List<Pair<Timestamp, Double>> GetAggregatedCpuUtilsForTimestampsFromCpuLogs(List<CpuLog> cpuLogs)
    {
        var aggregatedCpuUtilsForTimestamps = new ArrayList<Pair<Timestamp, Double>>();

        var logsGroupedByTimestampAndCpuName = GetGroupedCpuLogsByTimestampAndCpuName(cpuLogs);
        for (Timestamp timestamp : logsGroupedByTimestampAndCpuName.keySet())
        {
            var aggregatedCpuUtilForCpuNames
                    = GetComputedCpuUtilsForCpuNames(logsGroupedByTimestampAndCpuName.get(timestamp));

            double aggregatedCpuUtilForTimestamp = 0;
            for (Pair<String, Double> aggregatedCpuUtilForCpuName : aggregatedCpuUtilForCpuNames)
            {
                aggregatedCpuUtilForTimestamp += aggregatedCpuUtilForCpuName.getValue();
            }

            aggregatedCpuUtilsForTimestamps.add(new Pair<Timestamp, Double>(timestamp, aggregatedCpuUtilForTimestamp));
        }
        return aggregatedCpuUtilsForTimestamps;
    }

    private static Map<Timestamp, Map<String, List<CpuLog>>> GetGroupedCpuLogsByTimestampAndCpuName(List<CpuLog> cpuLogs)
    {
        return cpuLogs.stream().collect(
                Collectors.groupingBy(c -> c.Timestamp, Collectors.groupingBy(c -> c.CpuInfo.CpuName)));
    }

    private static List<Pair<String, Double>> GetComputedCpuUtilsForCpuNames(
            Map<String, List<CpuLog>> cpuLogsGroupedByCpuName)
    {
        Map<String, Double> cpuNameCpuUtilMap = cpuLogsGroupedByCpuName.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> GetComputedCpuUtil(e.getValue().get(0), e.getValue().get(1))
        ));

        return cpuNameCpuUtilMap.entrySet().stream()
                .map(e -> new Pair<String, Double>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static double GetComputedCpuUtil(CpuLog cpuLogFirstBatch, CpuLog cpuLogSecondBatch)
    {
        long cpuLogIdleFirstBatch = cpuLogFirstBatch.CpuInfo.Idle + cpuLogFirstBatch.CpuInfo.Iowait;
        long cpuLogNonIdleFirstBatch = cpuLogFirstBatch.CpuInfo.User + cpuLogFirstBatch.CpuInfo.Nice
                + cpuLogFirstBatch.CpuInfo.System + cpuLogFirstBatch.CpuInfo.Irq
                + cpuLogFirstBatch.CpuInfo.Softirq + cpuLogFirstBatch.CpuInfo.Steal;
        long cpuLogTotalFirstBatch = cpuLogIdleFirstBatch + cpuLogNonIdleFirstBatch;

        long cpuLogIdleSecondBatch = cpuLogSecondBatch.CpuInfo.Idle + cpuLogSecondBatch.CpuInfo.Iowait;
        long cpuLogNonIdleSecondBatch = cpuLogSecondBatch.CpuInfo.User + cpuLogSecondBatch.CpuInfo.Nice
                + cpuLogSecondBatch.CpuInfo.System + cpuLogSecondBatch.CpuInfo.Irq
                + cpuLogSecondBatch.CpuInfo.Softirq + cpuLogSecondBatch.CpuInfo.Steal;
        long cpuLogTotalSecondBatch = cpuLogIdleSecondBatch + cpuLogNonIdleSecondBatch;

        long totalDifference = cpuLogTotalSecondBatch - cpuLogTotalFirstBatch;
        long idleDifference = cpuLogIdleSecondBatch - cpuLogIdleFirstBatch;

        return (totalDifference - idleDifference) / (double) totalDifference * 100;
    }
}
