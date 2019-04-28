import Entities.Computer;
import Entities.ComputerPreference;
import Entities.Preference;
import Preferences.IPreference;
import org.hibernate.Session;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsManager
{
    public final int NumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("NumOfRetries"));
    public final int Cooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("Cooldown"));


    private List<ComputerManager> _computerManagers;
    private List<Preference> _availablePreferences;


    public LogsManager()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        _availablePreferences = GetAvailablePreferencesFromDb();
        _computerManagers = GetComputerManagersFromDb();

        // Create&Run MaintainManager

        // Run logs gathering for all computers
        for (ComputerManager computerManager : _computerManagers)
        {
            computerManager.StartGatheringLogs();
        }

        try
        {
            Thread.sleep(10000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        _computerManagers.get(0).StopGatheringLogs();
    }

    private List<Preference> GetAvailablePreferencesFromDb()
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        String hql = "from Preference";
        Query query = session.createQuery(hql);
        List<Preference> preferences = query.getResultList();

        session.close();
        return preferences;
    }

    private List<ComputerManager> GetComputerManagersFromDb()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        List<ComputerManager> computerManagers = new ArrayList<>();

        Map<Computer, List<ComputerPreference>> grouped =
                LoadComputerPreferencesFromDb().stream().collect(Collectors.groupingBy(cp -> cp.Computer));
        for (Map.Entry<Computer, List<ComputerPreference>> computerListEntry : grouped.entrySet())
        {
            Computer computer = computerListEntry.getKey();
            List<IPreference> preferences = new ArrayList<>();
            for (ComputerPreference computerPreference : computerListEntry.getValue())
            {
                Preference preferenceEntity = computerPreference.Preference;
                IPreference preference = ConvertPreferenceEntityToIPreference(preferenceEntity);
                preferences.add(preference);
            }

            computerManagers.add(new ComputerManager(this, computer, preferences));
        }

        return computerManagers;
    }

    private List<ComputerPreference> LoadComputerPreferencesFromDb()
    {
        Session session = null;
        session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        String hql = "from ComputerPreference compPref";
        Query query = session.createQuery(hql);
        List<ComputerPreference> preferences = query.getResultList();

        session.close();
        return  preferences;
    }

    private IPreference ConvertPreferenceEntityToIPreference(Preference preference)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException
    {
        String iPreferenceClassName = preference.ClassName;
        Class iPreferenceClass = Class.forName(iPreferenceClassName);
        IPreference iPreference = (IPreference) iPreferenceClass.getConstructor().newInstance();
        return iPreference;
    }

    public void GatheringStoppedCallback(ComputerManager computerManager)
    {
        // Placeholder
        System.out.println("Stopped");
    }

    public void GatheringSSHConnectionErrorCallback(ComputerManager computerManager)
    {
        System.out.println("Unable to connect with host.");
        _computerManagers.remove(computerManager);
    }
}
