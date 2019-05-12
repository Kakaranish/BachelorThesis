import Preferences.IPreference;
import Preferences.Preferences;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;

public class LogsMaintainer extends Thread
{
    private class ComputerAndTimeToMaintainPair
    {
        public Computer Computer;
        public long TimeToMaintain;

        public ComputerAndTimeToMaintainPair(Computer computer, long timeToMaintain)
        {
            Computer = computer;
            TimeToMaintain = timeToMaintain;
        }
    }
    private ComputerManager _computerManager;
    private boolean _isMaintaining;


    public LogsMaintainer(ComputerManager computerManager)
    {
        _computerManager = computerManager;
    }

    public void StartMaintainingLogs() throws InterruptedException
    {
        this.run();
    }

    public void run()
    {
        _isMaintaining = true;

        while(_isMaintaining)
        {
            ArrayDeque<Computer> computersToMaintain = new ArrayDeque<>();
            ComputerAndTimeToMaintainPair computerWithLowestTimeToMaintain = null;

            for (Computer computer : _computerManager.GetSelectedComputers())
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

                    System.out.println("[INFO] '"
                            + computerToMaintain.ComputerEntity.Host + "' was maintained.");
                }
            }
            else
            {
                System.out.println("[INFO] Waiting for next maintenence " +
                        Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds() + "s");
                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                MaintainComputer(computerWithLowestTimeToMaintain.Computer);
                System.out.println("[INFO] '"
                        + computerWithLowestTimeToMaintain.Computer.ComputerEntity.Host + "' was maintained.");
            }
        }
    }

    public void MaintainComputer(Computer computer)
    {
        if (computer.Preferences == null || computer.Preferences.isEmpty())
        {
            return;
        }

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
            throw new DatabaseException("Unable to maintain computer logs.");
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



    // TODO: Consider what todo while sleep (preferred interruption)
    public void StopMaintainingLogs()
    {
        _isMaintaining = false;
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
            throw new DatabaseException("Unable te remove all logs associated with computer");
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
