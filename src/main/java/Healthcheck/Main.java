package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshAuthMethod;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigScope;
import Healthcheck.Preferences.*;
import org.hibernate.Session;

import javax.management.remote.JMXConnectorFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void SshConfig_AddToDb_Local()
    {
        SshConfig sshConfiguration = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                "username",
                "scBkrr5+CFCcqA8kTNBknw=="
        );

        sshConfiguration.AddToDb();
    }

    public static void SshConfig_AddToDb_Global()
    {
        SshConfig sshConfiguration = new SshConfig(
                "displayedName2",
                SshConfigScope.GLOBAL,
                22,
                SshAuthMethod.PASSWORD,
                "username",
                "scBkrr5+CFCcqA8kTNBknw=="
        );

        sshConfiguration.AddToDb();
    }

    public static void SshConfig_RemoveConfig_Global()
    {
        SshConfigsManager sshConfigsManager = new SshConfigsManager();
        SshConfig sshConfig = sshConfigsManager.GetGlobalSshConfigByName("displayedName2");

        Session session = DatabaseManager.GetInstance().GetSession();
        sshConfig.RemoveFromDb(session);
        session.close();
    }

    public static void SshConfig_ExistsTest()
    {
        SshConfigsManager sshConfigsManager = new SshConfigsManager();
        SshConfig sshConfig = sshConfigsManager.GetGlobalSshConfigByName("displayedName");

        //        sshConfig.SetUsername("Papiez polak");
        sshConfig.UpdateInDb();
    }

    public static void Computer_AddToDb()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computerThatExists = computerManager.GetComputerByDisplayedName("OVH-XD");

        SshConfig sshConfiguration = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                "username",
                "scBkrr5+CFCcqA8kTNBknw=="
        );
        Computer computer = new Computer(
                "OVH-8",
                "JP2.GMD3",
                "B-1-12",
                sshConfiguration,
                Duration.ofSeconds(30),
                Duration.ofSeconds(5),
                Duration.ofSeconds(60),
                true
        );
        List<IPreference> iPreferences = new ArrayList<>(){{
            add(new CpuInfoPreference());
            add(new DisksInfoPreference());
            add(new ProcessesInfoPreference());
            add(new RamInfoPreference());
        }};

        computer.SetPreferences(Utilities.ConvertListOfIPreferencesToPreferences(iPreferences));
        computer.AddToDb();
    }

    public static void Computer_UpdateInDb_LocalToLocal()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computerThatExists = computerManager.GetComputerByDisplayedName("OVH-XD");

        computerThatExists.SshConfig.SetUsername("Zwolf");
        computerThatExists.UpdateInDb();
    }

    public static void Computer_UpdateInDb_LocalToGlobal()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computer = computerManager.GetComputerByDisplayedName("OVH-XD");

        SshConfigsManager sshConfigsManager = new SshConfigsManager();
        SshConfig globalConfig = sshConfigsManager.GetGlobalSshConfigByName("displayedName");

        computer.SetSshConfig(globalConfig);
        computer.UpdateInDb();
    }

    public static void Computer_UpdateInDb_GlobalToLocal()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computer = computerManager.GetComputerByDisplayedName("OVH-XD");

        SshConfig localConfig = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                "username",
                "scBkrr5+CFCcqA8kTNBknw=="
        );

        computer.SetSshConfig(localConfig);
        computer.UpdateInDb();
    }

    public static void Computer_UpdateInDb_GlobalToGlobal(String newGlobalConfigName)
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computer = computerManager.GetComputerByDisplayedName("OVH-XD");

        SshConfigsManager sshConfigsManager = new SshConfigsManager();
        SshConfig globalConfig = sshConfigsManager.GetGlobalSshConfigByName(newGlobalConfigName);

        computer.SetSshConfig(globalConfig);
        computer.UpdateInDb();
    }

    public static void Computer_UpdateInDb_GlobalUpdated()
    {
        ComputerManager computerManager = new ComputerManager();
        Computer computer = computerManager.GetComputerByDisplayedName("OVH-XD");

        SshConfigsManager sshConfigsManager = new SshConfigsManager();

        computer.UpdateInDb();
    }

    public static void main(String[] args)
    {
//        SshConfig_AddToDb_Local();
//        SshConfig_AddToDb_Global();
//        SshConfig_RemoveConfig_Global();
//        SshConfig_ExistsTest();

//        Computer_AddToDb();
//        Computer_UpdateInDb_LocalToLocal();
//        Computer_UpdateInDb_LocalToGlobal();
//        Computer_UpdateInDb_GlobalToLocal();
//        Computer_UpdateInDb_GlobalToGlobal("displayedName");
            Computer_UpdateInDb_GlobalUpdated();
    }
}