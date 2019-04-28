import Entities.ComputerEntity;
import Entities.ComputerEntityPreference;
import Entities.Preference;
import Preferences.IPreference;
import org.hibernate.Session;

import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComputerManager
{
    private LogsManager _logsManager;
    private List<Computer> _loadedComputers;
    private List<Preference> _availablePreferences;
    private List<Computer> _selectedComputers;

    public ComputerManager()
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException
    {
        _availablePreferences = GetAvailablePreferencesFromDb();
        _loadedComputers = GetComputersFromDb();
        _selectedComputers = new ArrayList<>(_loadedComputers);
    }

    // TODO: Add executing in new thread
    public void StartGatheringDataForSelectedComputers()
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException
    {
        _logsManager = new LogsManager(_selectedComputers);
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

    private List<Computer> GetComputersFromDb()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        List<Computer> computers = new ArrayList<>();

        Map<ComputerEntity, List<ComputerEntityPreference>> grouped =
                LoadComputerPreferencesFromDb().stream().collect(Collectors.groupingBy(cp -> cp.ComputerEntity));
        for (Map.Entry<ComputerEntity, List<ComputerEntityPreference>> computerListEntry : grouped.entrySet())
        {
            ComputerEntity computerEntity = computerListEntry.getKey();
            List<IPreference> preferences = new ArrayList<>();
            for (ComputerEntityPreference computerEntityPreference : computerListEntry.getValue())
            {
                Preference preferenceEntity = computerEntityPreference.Preference;
                IPreference preference = ConvertPreferenceEntityToIPreference(preferenceEntity);
                preferences.add(preference);
            }

            computers.add(new Computer(computerEntity, preferences));
        }

        return computers;
    }

    private List<ComputerEntityPreference> LoadComputerPreferencesFromDb()
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        String hql = "from ComputerEntityPreference compPref";
        Query query = session.createQuery(hql);
        List preferences = query.getResultList();

        session.close();
        return  preferences;
    }

    private IPreference ConvertPreferenceEntityToIPreference(Preference preference)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        String iPreferenceClassName = preference.ClassName;
        Class iPreferenceClass = Class.forName(iPreferenceClassName);
        IPreference iPreference = (IPreference) iPreferenceClass.getConstructor().newInstance();
        return iPreference;
    }
}
