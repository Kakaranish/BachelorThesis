package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.LogsGatherer;
import Healthcheck.LogsManagement.LogsMaintainer;
import Healthcheck.LogsManagement.LogsMaintainerException;
import Healthcheck.Preferences.*;
import org.hibernate.Session;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Application
{

    // Adding 0 - Healthcheck.Preferences.NoPreference
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
            User retrievedUser = usersManager.GetUser("root_ovh", "root");
            Classroom classroom = Utilities.GetClassroom("B-0-22");

            Computer computer = new Computer(new ComputerEntity(
                    "computer3",
                    "username",
                    "password",
                    "ssh",
                    2000,
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

    public static void UpdateComputer()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer compToUpdate = computerManager.GetComputer("51.68.142.57");
        UsersManager usersManager = new UsersManager();
        User user = usersManager.GetUser("root_ovh", "root");

        Computer updatedComputer = new Computer(compToUpdate);
        updatedComputer.ComputerEntity.MaintainPeriod = Duration.ofSeconds(30);
        updatedComputer.ComputerEntity.RequestInterval = Duration.ofSeconds(3);
        updatedComputer.ComputerEntity.LogExpiration = Duration.ofMinutes(1);
        computerManager.UpdateComputer(compToUpdate, updatedComputer.ComputerEntity);
        int x = 10;
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

    public static void UpdateUser()
    {
        ComputerManager computerManager = new ComputerManager();
        UsersManager usersManager = new UsersManager();
        User user = usersManager.GetUser("root_ovh1", "root");
        User userToUpdate = new User(user);
        userToUpdate.DisplayedUsername = null;
        usersManager.UpdateUser(user, userToUpdate, computerManager);
        int x = 10;
    }

    public static void RemoveUser()
    {
//        Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//        Healthcheck.UsersManager usersManager = new Healthcheck.UsersManager();
//        User user = usersManager.GetUser("uuser4");
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
        ComputerManager computerManager = new ComputerManager();
        LogsGatherer logsGatherer = new LogsGatherer();
        LogsMaintainer logsMaintainer = new LogsMaintainer(computerManager);

        logsGatherer.StartGatheringLogs(computerManager.GetSelectedComputers());
        try
        {
            logsMaintainer.StartMaintainingLogs();
        }
        catch (LogsMaintainerException e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        // TODO: Check if works in computer manager
//                AddUser();
//                UpdateUser();
        //        RemoveUser();

//        Session session = DatabaseManager.GetInstance().GetSession();
//        RunProgram();
        RunProgram2();
//        UpdateUser();
//        UpdateComputer();

//        RunProgram();
        long milis = new Timestamp(Duration.ofMinutes(1).toMillis()).getTime();
//        UpdateComputer();
//
//        PopulateComputers();
//        RunGatheringLogs();


        //                Session session =Healthcheck.DatabaseManagement.DatabaseManager.GetInstance().GetSession();

            //            UpdateComputer();
//            Healthcheck.ComputerManager computerManager = new Healthcheck.ComputerManager();
//            Healthcheck.LogsManagement.LogsMaintainer logsMaintainer = new Healthcheck.LogsManagement.LogsMaintainer(computerManager);
//            Healthcheck.Computer computer = computerManager.GetComputer("karol_wojczak_comp");
//            Healthcheck.LogsManagement.LogsManager logsManager = new Healthcheck.LogsManagement.LogsManager(computerManager);



            //            logsMaintainer.MaintainComputer(computer);
            //            logsManager.StartGatheringLogs();
            //            logsMaintainer.StartMaintainingLogs();

//            Healthcheck.UsersManager usersManager = new Healthcheck.UsersManager();
//            User user = usersManager.GetUser("jan_pawel");
//            User toUpdate = new User(user);
//            toUpdate.EncryptedPassword = "kremowka2137";
//            usersManager.UpdateUser(user, toUpdate, computerManager);



        //        GetSelectedComputers();
        //        RunGatheringLogs();

//            Healthcheck.UsersManager usersManager = new Healthcheck.UsersManager();
//            usersManager.AddUser(new User("karol_wojczak", "some_password", "ssh_2137"));
//
//            usersManager.UpdateUser(someUser, new User(someUser.DisplayedUsername, "some_password1", someUser.SSHKey));
//            int x = 10;
//            usersManager.UpdateUser(someUser, new User(someUser.DisplayedUsername, "some_password2", someUser.SSHKey));
//            int y = 10;

            //            Healthcheck.Computer computer = new Healthcheck.Computer(new ComputerEntity(
            //                    "wojtilak_host",
            //                    someUser,
            //                    2000,
            //                    22,
            //                    Duration.ofHours(2),
            //                    Duration.ofHours(2),
            //                    Duration.ofHours(2)
            //            ), new ArrayList<IPreference>() {{
            //                add(new DisksInfoPreference());
            //                add(new RamInfoPreference());
            //            }});

            //            User wojczak_user = usersManager.GetUser("root");
            //            User root_user = usersManager.GetUser("root");


            //            Healthcheck.Computer computerWithNoPreferences = new Healthcheck.Computer(new ComputerEntity(
            //                    "wojtilak_host2",
            //                    root_user,
            //                    2000,
            //                    22,
            //                    Duration.ofHours(2),
            //                    Duration.ofHours(2),
            //                    Duration.ofHours(2)
            //            ), null);


            //            Healthcheck.Computer wojtilak_computer = computerManager.GetComputer("wojtilak_host");

            //            User newUser = new User("papajak_watykaniak", "password", "ssh_key");
            //            usersManager.AddUser(newUser);
            //            User papajak_watykaniak = usersManager.GetUser("papajak_watykaniak");
            //            User new_papajak_watykaniak = new User(papajak_watykaniak);
            //            new_papajak_watykaniak.EncryptedPassword = "password5";

            //            usersManager.AddUser(newUser);
            //            Healthcheck.Computer someComputer = computerManager.GetComputer("some host8");
            //            usersManager.UpdateUser(papajak_watykaniak, new_papajak_watykaniak, computerManager);

            //            usersManager.RemoveUser(papajak_watykaniak, computerManager, true);
            //            computerManager.AssignUserToComputer(someComputer, papajak_watykaniak);


            //            someComputer.ComputerEntity.User = root_user;
            //            someComputer.ComputerPreferences.removeIf(t -> t instanceof DisksInfoPreference);


            List<IPreference> preferences = new ArrayList<IPreference>(){{
                //                add(new CpuInfoPreference());
                //                add(new SwapInfoPreference());
            }};

            //            try
            //            {
            //                computerManager.UpdateComputer(wojtilak_computer, someComputer);
            //            } catch (Healthcheck.DatabaseManagement.DatabaseException e)
            //            {
            //                e.printStackTrace();
            //            }


            //            computerManager.UpdateComputerPreferencesInDb(wojtilak_computer, preferences );
            //            computerManager.AddPreferencesInDb(wojtilak_computer, preferences);
            //            computerManager.RemoveComputerPreferences(wojtilak_computer);

            //            Healthcheck.LogsManagement.LogsMaintainer logsMaintainer = new Healthcheck.LogsManagement.LogsMaintainer();
            //            Healthcheck.Computer compWithLogs = computerManager.GetComputer("51.68.142.57");
            //            logsMaintainer.RemoveAllLogsAssociatedWithComputers(compWithLogs);



            int y = 10;

            //            computerManager.AssignUserToComputer(wojczak_user, wojtilak_computer);

            //            computerManager.AddComputer(computer);

            //            usersManager.RemoveUserWithoutAssociatedComputers(someUser, computerManager);
            //            usersManager.UpdateUserInDb(someUser, new User("papajak", "jp2gmd", someUser.SSHKey));
    }
}
