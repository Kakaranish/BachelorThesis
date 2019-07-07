package Healthcheck;

import Healthcheck.DatabaseManagement.MainDatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Preference;
import Healthcheck.Preferences.IPreference;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Utilities
{
    public static final int SSHTimeout =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("SSHTimeout"));
    public static final int PersistNumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("PersistNumOfRetries"));
    public static final int PersistCooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("PersistCooldown"));
    public static final int UpdateNumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("UpdateNumOfRetries"));
    public static final int UpdateCooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("UpdateCooldown"));
    public static final int RemoveNumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("RemoveNumOfRetries"));
    public static final int RemoveCooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("RemoveCooldown"));
    public static final int SelectNumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("SelectNumOfRetries"));
    public static final int SelectCooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("SelectCooldown"));
    public static final int DeleteNumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DeleteNumOfRetries"));
    public static final int DeleteCooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DeleteCooldown"));
    public static final int GatheringStartDelay
            = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("GatheringStartDelay"));
    public static final boolean UseDefaultValues =
            Boolean.parseBoolean(AppProperties.GetInstance().Properties.getProperty("UseDefaultValues"));
    public static final int DefaultPort =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DefaultPort"));
    public static final int DefaultRequestInterval =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DefaultRequestInterval"));
    public static final int DefaultMaintainPeriod =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DefaultMaintainPeriod"));
    public static final int DefaultLogExpiration =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("DefaultLogExpiration"));
    public static final int MaxLogsNum =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("MaxLogsNum"));

    public static final List<Preference> AvailablePreferences = GetAvailablePreferencesFromDb();

    public static Map<String, List<Computer>> GetComputersGroupedByClassroom(List<Computer> computers)
    {
        Map<String, List<Computer>> groupedComputers =
                computers.stream().collect(Collectors.groupingBy(c -> c.GetClassroom()));

        return groupedComputers;
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
        catch (Exception e)
        {
            // This block will be never entered
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
            preferences.add(ConvertIPreferenceToPreference(iPreference));
        }

        return preferences;
    }

    public static Preference ConvertIPreferenceToPreference(IPreference iPreference)
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
        Session session = MainDatabaseManager.GetInstance().GetSession();

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

    public static boolean AreEqual(Object obj1, Object obj2)
    {
        return obj1 == obj2 || (obj1 == null ? obj2 == null : obj1.equals(obj2));
    }

    public static boolean ReferencesAreEqual(Object obj1, Object obj2)
    {
        return obj1 == obj2;
    }

    public static Duration ConvertSecondsToDurationInNanos(long seconds)
    {
        long nanos = Duration.ofSeconds(seconds).toNanos();
        Duration durationInNanos = Duration.ofNanos(nanos);

        return durationInNanos;
    }

    public static String ExtractPreferenceName(String preferenceClass)
    {
        String[] splitPreferenceClass = preferenceClass.split("\\.");
        String preferenceString = splitPreferenceClass[2];
        preferenceString = preferenceString.replace("Preference", "");
        preferenceString = preferenceString.replace("Info", " Info");

        return preferenceString;
    }

    public static String GetPreferenceNameFrom(String preferenceClassName)
    {
        String[] splitPreferenceClass = preferenceClassName.split("\\.");
        String preferenceString = splitPreferenceClass[2];
        preferenceString = preferenceString.replace("Preference", "");
        preferenceString = preferenceString.replace("Info", " Info");

        return preferenceString;
    }

    public static String GetClassNameForPreferenceName(String preferenceName)
    {
        String[] splitPreferenceName = preferenceName.split(" ");
        return "Healthcheck.Preferences." + splitPreferenceName[0] + splitPreferenceName[1] + "Preference";
    }

    public static Preference GetPreferenceFromClassName(String preferenceClassName)
    {
        List<Preference> preferences = AvailablePreferences.stream()
                .filter(p -> p.ClassName.equals(preferenceClassName)).collect(Collectors.toList());

        return preferences.isEmpty() ? null : preferences.get(0);
    }

    public static void ShowInfoDialog(String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void ShowErrorDialog(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error alert");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void ShowFatalErrorDialog(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fatal error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void ShowSaveErrorDialog(List<String> errors)
    {
        StringBuilder errorMessageBuilder = new StringBuilder();
        for(int i=0; i < errors.size(); ++i)
        {
            errorMessageBuilder.append(errors.get(i));
            errorMessageBuilder.append(i + 1 == errors.size()? "" : "\n");
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error alert");
        alert.setHeaderText("Some fields have been incorrectly filled.");
        alert.setContentText(errorMessageBuilder.toString());

        alert.showAndWait();
    }

    public static boolean ShowYesNoDialog(String title, String contextText)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(contextText);

        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == okButton ? true : false;
    }

    public static boolean InternetConnectionIsAvailable()
    {
        try
        {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();

            conn.connect();
            conn.getInputStream().close();

            return true;
        }
        catch(IOException e)
        {
            return false;
        }
    }

    public static boolean EmptyOrNull(String str)
    {
        return str == null || str.trim().equals("");
    }

    public static double KilobytesToMegabytes(long kilobytes)
    {
        return kilobytes/1024D;
    }
}
