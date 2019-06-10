package Healthcheck;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshAuthMethod;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigScope;

import java.time.Duration;

public class Main
{
    public static void AddComputerWithLocalConfig(String displayedName, String host)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();

        SshConfig sshConfig = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                "AddComputerWithLocalConfig_username",
                "scBkrr5+CFCcqA8kTNBknw=="
        );
        Computer computerToAdd = new Computer(
            displayedName,
            host,
            "B-1-12",
            sshConfig,
            Duration.ofSeconds(30),
            Duration.ofSeconds(5),
            Duration.ofSeconds(60),
            true
        );
        computerToAdd.SetComputersAndSshConfigsManager(computersAndSshConfigsManager);
        computerToAdd.AddToDb();
    }

    public static void AddComputerWithGlobalConfig(String displayedName, String host, String sshConfigName)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        SshConfig globalSshConfig = computersAndSshConfigsManager.GetGlobalSshConfigByName(sshConfigName);

        Computer computerToAdd = new Computer(
                displayedName,
                host,
                "B-1-12",
                globalSshConfig,
                Duration.ofSeconds(30),
                Duration.ofSeconds(5),
                Duration.ofSeconds(60),
                true
        );

        computerToAdd.SetComputersAndSshConfigsManager(computersAndSshConfigsManager);
        computerToAdd.AddToDb();
    }

    public static void UpdateComputer_LocalToLocal(String displayedName, String newPassword)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        Computer computerToUpdate = computersAndSshConfigsManager.GetComputerByDisplayedName(displayedName);

        SshConfig sshConfig = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                newPassword,
                "scBkrr5+CFCcqA8kTNBknw=="
        );

        computerToUpdate.SetSshConfig(sshConfig);
        computerToUpdate.UpdateInDb();
    }

    public static void UpdateComputer_GlobalToGlobal(String displayedName, String newGlobalSshConfigName)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        Computer computerToUpdate = computersAndSshConfigsManager.GetComputerByDisplayedName(displayedName);
        SshConfig newGlobalConfig = computersAndSshConfigsManager.GetGlobalSshConfigByName(newGlobalSshConfigName);

        computerToUpdate.SetSshConfig(newGlobalConfig);
        computerToUpdate.UpdateInDb();
    }

    public static void main(String[] args)
    {
//        AddComputerWithLocalConfig("OVH-LOCAL", "666.69.1");
//        AddComputerWithGlobalConfig("OVH-LULZ2", "666.69.69", "configuration2");
//        UpdateComputer_LocalToLocal("OVH-LOCAL", "newPass1");
        UpdateComputer_GlobalToGlobal("OVH-LULZ2", "displayedName");
    }
}