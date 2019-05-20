package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Classroom;
import Healthcheck.Entities.Preference;
import Healthcheck.Preferences.IPreference;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utilities
{
    public static final int GetLogsUsingSSHNumOfRetries = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("GetLogsUsingSSHNumOfRetries"));

    public static final int GetLogsUsingSSHCooldown = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("GetLogsUsingSSHCooldown"));

    public static final int SSHTimeout = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("SSHTimeout"));

    public static final int LogSaveNumOfRetries = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("LogSaveNumOfRetries"));

    public static final int LogSaveRetryCooldown = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("LogSaveRetryCooldown"));

    public static final int GatheringStartDelay = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("GatheringStartDelay"));

    public static final List<Preference> AvailablePreferences = GetAvailablePreferencesFromDb();

    public static final List<Classroom> AvailableClassrooms = GetAvailableClassroomsFromDb();

    public static final Classroom GetClassroom(String classroomName)
    {
        List<Classroom> results = AvailableClassrooms.stream()
                .filter(c -> c.Name.equals(classroomName)).collect(Collectors.toList());

        return results.isEmpty()? null : results.get(0);
    }

    public static List<Computer> GetComputersWithSetPreferences(List<Computer> computers)
    {
        List<Computer> results = computers.stream()
                .filter(c -> c.Preferences.isEmpty() == false).collect(Collectors.toList());

        return results;
    }

    public static List<IPreference> ConvertListOfPreferencesToIPreferences(List<Preference> preferences)
    {
        if(preferences == null)
        {
            return null;
        }

        List<IPreference> iPreferences = new ArrayList<>();
        for (Preference preference : preferences)
        {
            iPreferences.add(ConvertPreferenceEntityToIPreference(preference));
        }

        return iPreferences;
    }

    public static IPreference ConvertPreferenceEntityToIPreference(Preference preference)
    {
        IPreference iPreference = null;
        String iPreferenceClassName = preference.ClassName;

        try
        {
            Class iPreferenceClass = Class.forName(iPreferenceClassName);
            iPreference = (IPreference) iPreferenceClass.getConstructor().newInstance();
        }
        catch (Exception e) // No matter what kind of exception will be thrown
        {
        }

        return iPreference;
    }

    public static List<Preference> ConvertListOfIPreferencesToPreferences(List<IPreference> iPreferences)
    {
        if(iPreferences == null)
        {
            return null;
        }

        List<Preference> preferences = new ArrayList<>();
        for (IPreference iPreference : iPreferences)
        {
            preferences.add(ConvertIPreferenceToEntityPreference(iPreference));
        }

        return preferences;
    }

    public static Preference ConvertIPreferenceToEntityPreference(IPreference iPreference)
    {
        String className = iPreference.getClass().getName();
        List<Preference> preferences =
                AvailablePreferences.stream().filter(p -> p.ClassName.equals(className)).collect(Collectors.toList());

        Preference preference = preferences.get(0);
        return preference;
    }

    private static List<Preference> GetAvailablePreferencesFromDb()
    {
        String hql = "from Preference";
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            Query query = session.createQuery(hql);
            List<Preference> preferences = query.getResultList();

            return preferences;
        }
        catch (PersistenceException e)
        {
            return null;
        }
        finally
        {
            session.close();
        }
    }

    private static List<Classroom> GetAvailableClassroomsFromDb() throws DatabaseException
    {
        String hql = "from Classroom";
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            Query query = session.createQuery(hql);
            List<Classroom> classrooms = query.getResultList();

            return classrooms;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get available preferences.");
        }
        finally
        {
            session.close();
        }
    }
}
