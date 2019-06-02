package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.*;
import Healthcheck.Preferences.*;
import javafx.stage.Stage;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;



public class Main extends javafx.application.Application
{

    // Adding 0 - Healthcheck.Preferences.NoPreferenceB
    public static void PopulatePreferences()
    {
        Session session = DatabaseManager.GetInstance().GetSession();;
        try
        {
            session.beginTransaction();

            ////////////////////////////////////////////
            // Populating

            String hql = "from Preference";
            Query query = session.createQuery(hql);
            List result = query.getResultList();

            if(result == null || result.isEmpty())
            {
                List<String> preferences = new ArrayList<String>(){{
                    add("Healthcheck.Preferences.CpuInfoPreference");
                    add("Healthcheck.Preferences.DisksInfoPreference");
                    add("Healthcheck.Preferences.ProcessesInfoPreference");
                    add("Healthcheck.Preferences.RamInfoPreference");
                    add("Healthcheck.Preferences.SwapInfoPreference");
                    add("Healthcheck.Preferences.UsersInfoPreference");
                }};

                for (String pref : preferences)
                {
                    Preference preference = new Preference(pref);
                    session.save(preference);
                }
            }
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        finally
        {
            session.close();
        }
    }

    public static void PopulateClassrooms()
    {
        Session session = DatabaseManager.GetInstance().GetSession();;
        try
        {
            session.beginTransaction();

            ////////////////////////////////////////////
            // Populating

            String hql = "from Classroom";
            Query query = session.createQuery(hql);
            List result = query.getResultList();

            if(result == null || result.isEmpty())
            {
                List<Classroom> classrooms = new ArrayList<Classroom>(){{
                    add(new Classroom("A-1-12", "A-1-12 classroom"));
                    add(new Classroom("D-2-03", "D-2-03 classroom"));
                    add(new Classroom("B-0-22", "B-0-22 classroom"));
                }};

                for (Classroom classroom : classrooms)
                {
                    session.save(classroom);
                }
            }

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        finally
        {
            session.close();
        }
    }

    public static void PopulateComputers()
    {
            ComputerManager computerManager = new ComputerManager();
            UsersManager usersManager = new UsersManager();
            User retrievedUser = usersManager.GetUserByDisplayedUsernameAndSSHUsername("root_ovh", "root");
            Classroom classroom = Utilities.GetClassroom("B-0-22");

            Computer computer = new Computer(new ComputerEntity(
                    "computer3",
                    "some_name",
                    "username",
                    "password",
                    "ssh",
                    22,
                    Duration.ofHours(2),
                    Duration.ofHours(2),
                    Duration.ofHours(2),
                    classroom,
                    true
            ), new ArrayList<IPreference>(){{
                add(new CpuInfoPreference());
                add(new DisksInfoPreference());
                add(new ProcessesInfoPreference());
                add(new RamInfoPreference());;
                add(new SwapInfoPreference());
                add(new UsersInfoPreference());
            }});

            computerManager.AddComputer(computer);
    }

    public static void PopulateUsers()
    {
        UsersManager usersManager = new UsersManager();
        try
        {
//            usersManager.AddUser(new User("root", "scBkrr5+CFCcqA8kTNBknw==", "ssh_key"));
//            usersManager.AddUser(new User("karol_wojczak", "password", "ssh_2137"));
//            usersManager.AddUser(new User("jan_pawel", "jp2gmd_pass", "ssh_2138"));
        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
    }

    public static void RunGatheringLogs()
    {
//            Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//            Healthcheck.LogsManagement.LogsManager logsManager = new Healthcheck.LogsManagement.LogsManager(computerManager);
//
//            logsManager.StartGatheringLogs();

            //            Healthcheck.LogsManagement.LogsManager logsManager = new Healthcheck.LogsManagement.LogsManager();
            //            Logs.Healthcheck.LogsManagement.ComputerLogger computerLogger = new Logs.Healthcheck.LogsManagement.ComputerLogger()
    }


    public static void GetSelectedComputers()
    {
        ComputerManager computerManager = new ComputerManager();
        List<Computer> selectedComputers = computerManager.GetSelectedComputers();
        int x = 10;
    }

//    public static void AddUser()
//    {
//        User user = new User(
//                "root_ovh",
//                "root1",
//                "scBkrr5+CFCcqA8kTNBknw==",
//                "ssh_keys"
//        );
//        Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//        Healthcheck.UsersManager usersManager = new Healthcheck.UsersManager();
//        usersManager.AddUser(user);
//    }


    public static void RemoveUser()
    {
//        Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//        Healthcheck.UsersManager usersManager = new Healthcheck.UsersManager();
//        User user = usersManager.GetUserByDisplayedUsernameAndSSHUsername("uuser4");
//        usersManager.RemoveUser(user, computerManager, false);
    }

//    public static void RunProgram()
//    {
//        Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//        Healthcheck.LogsManagement.LogsManager logsManager = new Healthcheck.LogsManagement.LogsManager();
//        logsManager.StartGatheringLogs(computerManager.GetSelectedComputers());
//        Healthcheck.LogsManagement.LogsMaintainer logsMaintainer = new Healthcheck.LogsManagement.LogsMaintainer(computerManager);
//
//        try
//        {
//            logsMaintainer.StopMaintainingLogs();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        try
//        {
//            logsMaintainer.StartMaintainingLogs();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        try
//        {
//            logsMaintainer.StartMaintainingLogs();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        try
//        {
//            logsMaintainer.StartMaintainingLogs();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        new Thread(new Runnable() {
//            public void run() {
//                try
//                {
//                    Thread.sleep(1000);
//                    System.out.println("XDD");
//                    try
//                    {
//                        logsMaintainer.StopMaintainingLogs();
//                }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//
//                    try
//                    {
//                        logsMaintainer.StopMaintainingLogs();
//                    }
//                    catch (Exception e)
//                    {
//                        System.out.println(e.getMessage());
//                        e.printStackTrace();
//                        System.err.println("CHUJ");
//                    }
//
//                }
//                catch (InterruptedException|RuntimeException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//        try
//        {
//            logsMaintainer.StopMaintainingLogs();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        try
//        {
//            Thread.sleep(3000);
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//
//        System.out.println("XD");
//
//    }


    public static void RunProgram2()
    {
//        ComputerManager computerManager = new ComputerManager();
//        LogsGatherer logsGatherer = new LogsGatherer();
//        LogsMaintainer logsMaintainer = new LogsMaintainer(computerManager);
//
//        logsGatherer.StartGatheringLogs(computerManager.GetSelectedComputers());
//        try
//        {
//            logsMaintainer.StartMaintainingLogs();
//        }
//        catch (LogsException e)
//        {
//            e.printStackTrace();
//        }
    }

//    public static void RunProgram3()
//    {
//        ComputerManager computerManager = new ComputerManager();
//        LogsManager logsManager = new LogsManager();
//        LogsMaintainer logsMaintainer = new LogsMaintainer(computerManager);
//        LogsGatherer logsGatherer = new LogsGatherer(logsManager);
//
//        try
//        {
//            logsManager.StartWork(logsGatherer, logsMaintainer, computerManager.GetSelectedComputers());
//        }
//        catch (LogsException e)
//        {
//            e.printStackTrace();
//        }
//        catch (NothingToDoException e)
//        {
//            e.printStackTrace();
//        }
//    }



    public static void AddUser()
    {
        UsersManager usersManager = new UsersManager();
        try
        {
            usersManager.AddUser(new User(
                    "test_user",
                    "test_user",
                    "scBkrr5+CFCcqA8kTNBknw==",
                    "ssh_key"));
        }
        catch (DatabaseException e)
        {
            e.printStackTrace();
        }
    }

    public static void UpdateUser()
    {
        ComputerManager computerManager = new ComputerManager();
        UsersManager usersManager = new UsersManager();
        User userToUpdate = usersManager.GetUserByDisplayedUsernameAndSSHUsername("test_user", "test_user");
        User newUser = new User(userToUpdate);
        newUser.SSH_Key = "newSSHKey";

        try
        {
            usersManager.UpdateUser(userToUpdate, newUser, computerManager);
        }
        catch (NothingToDoException e)
        {
            System.out.println("Nothing todo");
        }

        int x = 10;
    }

    public static void AddComputer()
    {
        ComputerManager computerManager = new ComputerManager();
        UsersManager usersManager = new UsersManager();
        User retrievedUser = usersManager.GetUserByDisplayedUsernameAndSSHUsername("test_user", "user_test2");
//        Classroom classroom = Utilities.GetClassroom("B-0-22");

        Computer computer = new Computer(new ComputerEntity(
                "computerXD",
                "computerName",
                null,
                "password",
                "ssh",
                22,
                Duration.ofSeconds(30),
                Duration.ofSeconds(5),
                Duration.ofSeconds(60),
                null,
                true
        ), new ArrayList<IPreference>(){{
            add(new CpuInfoPreference());
            add(new DisksInfoPreference());
            add(new UsersInfoPreference());
        }});

        computerManager.AddComputer(computer);
    }

    public static void UpdateComputer_AddUser()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer compToUpdate = computerManager.GetComputerByHost("145.239.81.14");
        UsersManager usersManager = new UsersManager();
        User user = usersManager.GetUserByDisplayedUsernameAndSSHUsername("root_ovh", "root");

        List preferences = new ArrayList<IPreference>(){{
            add(new CpuInfoPreference());
            add(new DisksInfoPreference());
            add(new ProcessesInfoPreference());
            add(new RamInfoPreference());
            add(new SwapInfoPreference());
            add(new UsersInfoPreference());
        }};
        Computer updatedComputer = new Computer(compToUpdate);
//        updatedComputer.ComputerEntity.User = user;
        updatedComputer.ComputerEntity.Preferences = Utilities.ConvertListOfIPreferencesToPreferences(preferences);
//        updatedComputer.ComputerEntity.User = null;
//        updatedComputer.ComputerEntity.Port = 22;
//        updatedComputer.ComputerEntity.SetConnectionDataFields("user", "password", "key");
        try
        {
            computerManager.UpdateComputer(compToUpdate, updatedComputer.ComputerEntity);
        }
        catch (NothingToDoException e)
        {
            System.out.println("Nothing to do.");
        }
        catch (DatabaseException e)
        {
            System.out.println("DatabaseException");
        }

        int x = 10;
    }

    public static void RemoveComputerWithLogs()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer compToRemove = computerManager.GetComputerByHost("145.239.81.14");
        computerManager.RemoveComputerWithLogs(compToRemove);

        int x = 10;
    }

    public static void RemoveUserFromComputers()
    {
        UsersManager usersManager = new UsersManager();
        ComputerManager computerManager = new ComputerManager();
        User userToRemove = usersManager.GetUserByDisplayedUsernameAndSSHUsername("test_user", "user_test2");
        usersManager.RemoveUser(userToRemove, computerManager);
    }

    public static void RunProgram4()
    {
        ComputerManager computerManager = new ComputerManager();
        LogsManager logsManager = new LogsManager(computerManager);

        try
        {
            logsManager.StartWork();
        }
        catch (LogsException e)
        {
            e.printStackTrace();
        }
        catch (NothingToDoException e)
        {
            e.printStackTrace();
        }

        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        List<Computer> selectedComputers = new ArrayList<>(){{
            add(computerManager.GetComputerByHost("145.239.81.14"));
            add(computerManager.GetComputerByHost("computerX"));
        }};
        try
        {
            logsManager.UpdateGatheredComputers(selectedComputers);
        }
        catch (LogsException e)
        {
            e.printStackTrace(System.out);
        }

        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        selectedComputers = new ArrayList<>(){{
            add(computerManager.GetComputerByHost("145.239.81.14"));
            add(computerManager.GetComputerByHost("51.68.142.57"));
        }};
        try
        {
            logsManager.UpdateGatheredComputers(selectedComputers);
        }
        catch (LogsException e)
        {
            e.printStackTrace(System.out);
        }

        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        try
        {
            logsManager.StopWork();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        RunProgram4();

        //        UpdateComputer_AddUser();
//        AddUser();
//        UpdateUser();
//        AddComputer();
//        RemoveComputerWithLogs();
//        RemoveUserFromComputers();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {

    }
}