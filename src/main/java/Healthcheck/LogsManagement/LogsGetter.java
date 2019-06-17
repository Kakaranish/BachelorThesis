package Healthcheck.LogsManagement;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Preferences.IPreference;
import org.hibernate.Session;
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
}
