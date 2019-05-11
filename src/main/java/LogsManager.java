import Entities.Classroom;
import Entities.ComputerEntity;
import Entities.ComputerEntityPreference;
import Entities.Logs.BaseEntity;
import Preferences.IPreference;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsManager
{
    public final int NumOfRetries = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("NumOfRetries"));
    public final int Cooldown = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("Cooldown"));

    private ComputerManager _computerManager;
    private LogsMaintainer _logsMaintainer;


    public LogsManager(ComputerManager computerManager)
    {
        _computerManager = computerManager;

        //TODO: Remove instantiating LogsManager from there
        _logsMaintainer = new LogsMaintainer(computerManager);
    }

    public void StartGatheringLogs()
    {
        List<Computer> selectedComputers = _computerManager.GetSelectedComputers();

        // Prepare computer loggers
        List<ComputerLogger> selectedComputersLoggers = new ArrayList<>();
        for (Computer selectedComputer : selectedComputers)
        {
            selectedComputersLoggers.add(new ComputerLogger(this, selectedComputer));
        }

        // Run computer loggers
        for (ComputerLogger computerLogger : selectedComputersLoggers)
        {
            computerLogger.StartGatheringLogs();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------  GETTING LOGS  ---------------------------------------------------

    public static Map<ComputerEntity, List<BaseEntity>> GetLogsGroupedByComputer(List<BaseEntity> logs)
    {
        Map<ComputerEntity, List<BaseEntity>> grouped =
                logs.stream().collect(Collectors.groupingBy(l -> l.ComputerEntity));

        return grouped;
    }

    public List<BaseEntity> GetCertainTypeLogsForSingleComputer(
            Computer computer, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            String hql = "from " + preference.GetClassName() + " t" +
                    " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                    " and t.ComputerEntity = :computerEntity";
            Query query = session.createQuery(hql);
            query.setParameter("computerEntity", computer.ComputerEntity);

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

    public List<BaseEntity> GetCertainTypeLogsForClassroom(
            Classroom classroom, IPreference preference, Timestamp fromDate, Timestamp toDate)
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        try
        {
            String hql = "from " + preference.GetClassName() + " t" +
                    " where t.Timestamp > " + fromDate.getTime() + " and t.Timestamp < " + toDate.getTime() +
                    " and t.ComputerEntity.Classroom = :classroom";
            Query query = session.createQuery(hql);
            query.setParameter("classroom", classroom);

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

    public List<BaseEntity> GetCertainTypeLogsForAllComputers(
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

    public List<BaseEntity> GetCertainTypeLogsForSelectedComputers(
            IPreference preference, Timestamp fromDate, Timestamp toDate, ComputerManager computerManager)
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            List<BaseEntity> logsList = new ArrayList<>();

            List<Computer> selectedComputers = computerManager.GetSelectedComputers();
            for (Computer selectedComputer : selectedComputers)
            {
                String hql = "from " + preference.GetClassName() +" t" +
                        " where t.Timestamp > " + fromDate.getTime() + " and  t.Timestamp < " + toDate.getTime() +
                        " and t.ComputerEntity = :computerEntity";
                Query query = session.createQuery(hql);
                query.setParameter("computerEntity", selectedComputer.ComputerEntity);

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

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------  CALLBACKS  ---------------------------------------------------

    public void Callback_UnableToDecryptPassword(String host)
    {
        System.out.println("[FATAL ERROR] '" + host + "': Connection failed. Unable to decrypt password.");
    }

    public void Callback_SSHConnectionAttemptFailed(String host)
    {
        System.out.println("[ERROR] '" + host + "': Attempt of SSH connection failed.");
    }

    public void Callback_UnableToConnectAfterRetries(String host)
    {
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed. Max num of retries reached.");
    }

    public void Callback_LogGathered(String host)
    {
        System.out.println("[INFO] '" + host + "': Logs have been gathered.");
    }

    public void Callback_DatabaseConnectionAttemptFailed(String host)
    {
        System.out.println("[ERROR] '" + host + "': attempt of connection with database failed.");
    }

    public void Callback_DatabaseTransactionFailed(String host)
    {
        System.out.println("[FATAL ERROR] '" + host + "': Database transaction failed.");
    }

    public void Callback_ThreadInterrupted(String host)
    {
        System.out.println("[FATAL ERROR] '" + host + "': Thread has been interrupted.");
    }

    public void Callback_LogGatheringStopped(String host)
    {
        System.out.println("[INFO] '" + host + "': Logs gathering has been stopped.");
    }
}
