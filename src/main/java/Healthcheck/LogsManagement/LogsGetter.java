package Healthcheck.LogsManagement;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.*;
import Healthcheck.Preferences.IPreference;
import javafx.util.Pair;
import org.hibernate.Session;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsGetter
{
    public static Map<Computer, List<BaseEntity>> GetLogsGroupedByComputer(List<BaseEntity> logs)
    {
        Map<Computer, List<BaseEntity>> grouped =
                logs.stream().collect(Collectors.groupingBy(l -> l.Computer));

        return grouped;
    }

    public static List<BaseEntity> GetCertainTypeLogsForSingleComputer(
            Computer computer, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            String hql = "from " + preference.GetClassName() + " t" +
                    " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                    " and t.Computer = :computer";
            Query query = session.createQuery(hql);
            query.setParameter("computer", computer);

            List<BaseEntity> receivedLogs = query.getResultList();

            return receivedLogs;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable get certain type logs for single computer.");
        }
        finally
        {
            session.close();
        }
    }

    public static List<BaseEntity> GetCertainTypeLogsForClassroom(
            String classroom, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        try
        {
            String hql = "from " + preference.GetClassName() + " t" +
                    " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                    " and t.Computer.Classroom = '" + classroom + "'";
            Query query = session.createQuery(hql);

            List<BaseEntity> receivedLogs = query.getResultList();

            return receivedLogs;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get certain type logs for classroom.");
        }
        finally
        {
            session.close();
        }
    }

    public static List<BaseEntity> GetCertainTypeLogsForAllComputers(
            IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            String hql = "from " + preference.GetClassName() + " t" +
                    " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime();
            Query query = session.createQuery(hql);

            List<BaseEntity> receivedLogs = query.getResultList();

            return receivedLogs;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get certain type logs for all computers.");
        }
        finally
        {
            session.close();
        }
    }

    public static List<BaseEntity> GetCertainTypeLogsForSelectedComputers(
            IPreference preference, Timestamp fromDate, Timestamp toDate,
            ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            List<BaseEntity> logsList = new ArrayList<>();

            List<Computer> selectedComputers = computersAndSshConfigsManager.GetSelectedComputers();
            for (Computer selectedComputer : selectedComputers)
            {
                String hql = "from " + preference.GetClassName() +" t" +
                        " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                        " and t.Computer = :computerEntity";
                Query query = session.createQuery(hql);
                query.setParameter("computerEntity", selectedComputer);

                logsList.addAll(query.getResultList());
            }

            return logsList;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get certain type logs for selected computers.");
        }
        finally
        {
            session.close();
        }
    }

    // --- SWAP LOGS  --------------------------------------------------------------------------------------------------

    public static List<Quartet<Timestamp, Long, Long, Long>> GetSwapTimestampTotalUsedFreeQuartetList(List<SwapLog> swapLogs)
    {
        return swapLogs.stream().map(s -> new Quartet<Timestamp, Long, Long, Long>(s.Timestamp, s.SwapInfo.Total,
                s.SwapInfo.Used, s.SwapInfo.Free)).collect(Collectors.toList());
    }

    public static long GetSwapTotal(List<SwapLog> ramLogs)
    {
        return ramLogs.get(0).SwapInfo.Total;
    }

    public static List<Triplet<Timestamp, Long, Long>> GetSwapTimestampUsedFreeTripletList(List<SwapLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Triplet<Timestamp, Long, Long>(
                s.Timestamp, s.SwapInfo.Used, s.SwapInfo.Free)).collect(Collectors.toList());
    }

    // --- RAM LOGS  ---------------------------------------------------------------------------------------------------

    public static List<Quartet<Timestamp, Long, Long, Long>> GetRamTimestampTotalUsedFreeQuartetList(List<RamLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Quartet<Timestamp, Long, Long, Long>(
                s.Timestamp, s.RamInfo.Total, s.RamInfo.Used, s.RamInfo.Free)).collect(Collectors.toList());
    }

    public static long GetRamTotal(List<RamLog> ramLogs)
    {
        return ramLogs.get(0).RamInfo.Total;
    }

    public static List<Triplet<Timestamp, Long, Long>> GetRamTimestampUsedFreeTripletList(List<RamLog> ramLogs)
    {
        return ramLogs.stream().map(s -> new Triplet<Timestamp, Long, Long>(
                s.Timestamp, s.RamInfo.Used, s.RamInfo.Free)).collect(Collectors.toList());
    }

    // --- USERS LOGS  -------------------------------------------------------------------------------------------------

    public static List<Pair<Timestamp, Integer>> GetUsersTimestampNumOfLogged(List<UserLog> usersLogs)
    {
        return usersLogs.stream().collect(Collectors.groupingBy(u -> u.Timestamp))
                .entrySet().stream().map(u -> new Pair<Timestamp, Integer>(u.getKey(), u.getValue().size()))
                .collect(Collectors.toList());
    }

    // --- DISKS LOGS  -------------------------------------------------------------------------------------------------

    public static Map<String, List<DiskLog>> GroupDisksLogsByFileSystem(List<DiskLog> disksLogs)
    {
        return disksLogs.stream().collect(Collectors.groupingBy(d -> d.DiskInfo.FileSystem));
    }

    public static List<Pair<Timestamp, Double>> GetDisksFreePercentageForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).stream()
                .map(l -> new Pair<Timestamp, Double>(l.Timestamp, (double) l.DiskInfo.UsePercentage))
                .collect(Collectors.toList());
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

    public static long GetDisksBlockNumberForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).get(0).DiskInfo.BlocksNumber;
    }

    public static List<Pair<Timestamp, Double>> GetDisksPercentageUsageForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).stream().map(l -> new Pair<Timestamp, Double>(
                l.Timestamp, l.DiskInfo.Used / (double) l.DiskInfo.Available * 100)).collect(Collectors.toList());
    }

    public static long GetDiskAvailableSizeForFileSystem(
            Map<String, List<DiskLog>> groupedByFileSystemLogs, String fileSystem)
    {
        return groupedByFileSystemLogs.get(fileSystem).get(0).DiskInfo.Available;
    }


    public static List<Pair<Timestamp, Double>> GetCpuTimestamp1CpuUtilAvgList(List<CpuLog> cpuLogs)
    {
        return cpuLogs.stream().map(c -> new Pair<Timestamp, Double>(c.Timestamp,
                c.CpuInfo.Last1MinuteAvgCpuUtil)).collect(Collectors.toList());
    }

    public static List<Pair<Timestamp, Double>> GetCpuTimestamp5CpuUtilAvgList(List<CpuLog> cpuLogs)
    {
        return cpuLogs.stream().map(c -> new Pair<Timestamp, Double>(c.Timestamp,
                c.CpuInfo.Last5MinutesAvgCpuUtil)).collect(Collectors.toList());
    }

    public static List<Pair<Timestamp, Double>> GetCpuTimestamp15CpuUtilAvgList(List<CpuLog> cpuLogs)
    {
        return cpuLogs.stream().map(c -> new Pair<Timestamp, Double>(c.Timestamp,
                c.CpuInfo.Last15MinutesAvgCpuUtil)).collect(Collectors.toList());
    }
}
