package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class LogsMaintainer extends Thread
{
    private class ComputerAndTimeToMaintainPair
    {
        public Healthcheck.Computer Computer;
        public long TimeToMaintain;

        public ComputerAndTimeToMaintainPair(Computer computer, long timeToMaintain)
        {
            Computer = computer;
            TimeToMaintain = timeToMaintain;
        }
    }

    private ComputerManager _computerManager;

    private List<Computer> _gatheredComputers;
    private volatile boolean _isMaintaining = false;

    public LogsMaintainer(ComputerManager computerManager)
    {
        _computerManager = computerManager;

        _gatheredComputers = new ArrayList<>();
    }

    public void StopMaintainingLogsForSingleComputer(Computer computer) throws LogsException
    {
        if(_isMaintaining == false)
        {
            String host = computer.ComputerEntity.Host;
            throw new LogsException("[FATAL ERROR] Unable to stop maintaining logs for '" + host + "'. No maintainer is working.");
        }

        if(_gatheredComputers.contains(computer) == false)
        {
            String host = computer.ComputerEntity.Host;
            throw new LogsException("[FATAL ERROR] Unable to stop maintaining logs for '" + host + "'. Host isn't maintained.");
        }

        _gatheredComputers.remove(computer);
    }

    public void StartMaintainingLogs(List<Computer> gatheredComputers) throws LogsException
    {
        if(_isMaintaining == true)
        {
            throw new LogsException("[FATAL ERROR] Unable to start maintaining logs. Other maintainer currently is working.");
        }

        System.out.println("[INFO] Logs maintainer started work.");

        _isMaintaining = true;
        _gatheredComputers = gatheredComputers;

        this.start();
    }

    public void StopMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("[FATAL ERROR] Unable to stop maintaining logs. No maintainer is working.");
        }

        System.out.println("[INFO] Logs maintainer stopped work.");

        _isMaintaining = false;
        _gatheredComputers = null;

        this.interrupt();
    }

    public void run()
    {
        _isMaintaining = true;

        while(_isMaintaining)
        {
            ArrayDeque<Computer> computersToMaintain = new ArrayDeque<>();
            ComputerAndTimeToMaintainPair computerWithLowestTimeToMaintain = null;

            for (Computer computer : Utilities.GetComputersWithSetPreferences(_gatheredComputers))
            {
                long computerTimeToMaintenance = GetComputerTimeToMaintenance(computer);

                if( computerWithLowestTimeToMaintain == null ||
                        computerTimeToMaintenance < computerWithLowestTimeToMaintain.TimeToMaintain)
                {
                    computerWithLowestTimeToMaintain = new ComputerAndTimeToMaintainPair(computer, computerTimeToMaintenance);;
                }

                if(IsComputerReadyForMaintenance(computerTimeToMaintenance))
                {
                    computersToMaintain.add(computer);
                }
            }

            if (computersToMaintain.isEmpty() == false)
            {
                while(computersToMaintain.isEmpty() == false)
                {
                    Computer computerToMaintain = computersToMaintain.remove();

                    MaintainComputer(computerToMaintain);

                    String host = computerToMaintain.ComputerEntity.Host;
                    System.out.println("[INFO] '" + host + "' was maintained.");
                }
            }
            else
            {
                long timeToNextMaintain = Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds();
                System.out.println("[INFO] Next maintenance will be taken in " + timeToNextMaintain + "s");

                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException|IllegalArgumentException e)
                {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }

                MaintainComputer(computerWithLowestTimeToMaintain.Computer);
                String host = computerWithLowestTimeToMaintain.Computer.ComputerEntity.Host;
                System.out.println("[INFO] '" + host+ "' was maintained.");
            }
        }
    }

    public void MaintainComputer(Computer computer)
    {
        long logExpiration = computer.ComputerEntity.LogExpiration.toMillis();
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();
            for (IPreference computerPreference : computer.Preferences)
            {
                Long now = System.currentTimeMillis();

                String hql = "delete from " + computerPreference.GetClassName() + " t "+
                        "where t.ComputerEntity = :computerEntity " +
                        "and (" + now  + " - t.Timestamp) > " + logExpiration;

                Query query = session.createQuery(hql);
                query.setParameter("computerEntity", computer.ComputerEntity);

                query.executeUpdate();
            }
            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            String host = computer.ComputerEntity.Host;
            throw new DatabaseException("[FATAL ERROR] Unable to maintain '" + host + "' logs.");
        }
        finally
        {
            session.close();
        }

        // Set computer last maintenance time to now
        Computer newComputer = new Computer(computer);
        newComputer.ComputerEntity.LastMaintenance = new Timestamp(System.currentTimeMillis());
        _computerManager.UpdateComputer(computer, newComputer.ComputerEntity);
    }

    public void RemoveAllLogsAssociatedWithComputers(Computer computer) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            for (IPreference computerPreference : Preferences.AllPreferencesList)
            {
                String hql = "delete from " +
                        computerPreference.GetClassName() +
                        " t where t.ComputerEntity = :computerEntity";
                Query query = session.createQuery(hql);
                query.setParameter("computerEntity", computer.ComputerEntity);

                query.executeUpdate();
            }

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("[FATAL ERROR] Unable te remove all logs associated with computer");
        }
        finally
        {
            session.close();
        }
    }

    public void RemoveAllLogsAssociatedWithComputerFromDb(Computer computer, Session session)
    {
        for (IPreference computerPreference : Preferences.AllPreferencesList)
        {
            String hql = "delete from " +
                    computerPreference.GetClassName() +
                    " t where t.ComputerEntity = :computerEntity";
            Query query = session.createQuery(hql);
            query.setParameter("computerEntity", computer.ComputerEntity);

            query.executeUpdate();
        }
    }

    private long GetComputerTimeToMaintenance(Computer computer)
    {
        long currentTime = System.currentTimeMillis();
        long lastMaintenanceTime = computer.ComputerEntity.LastMaintenance.getTime();
        long maintenancePeriod = computer.ComputerEntity.MaintainPeriod.toMillis();

        return lastMaintenanceTime + maintenancePeriod - currentTime;
    }

    private boolean IsComputerReadyForMaintenance(long computerTimeToMaintenance)
    {
        return computerTimeToMaintenance <= 0;
    }
}
