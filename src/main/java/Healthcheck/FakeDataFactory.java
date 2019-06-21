package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.SwapLog;
import Healthcheck.Entities.SshAuthMethod;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigScope;
import Healthcheck.Models.Info.CpuInfo;
import Healthcheck.Models.Info.SwapInfo;
import Healthcheck.Preferences.*;
import org.hibernate.Session;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakeDataFactory
{
    public static Computer AddComputerWithLocalSshConfig(String displayedName, String host)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        SshConfig sshConfig = new SshConfig(
                null,
                SshConfigScope.LOCAL,
                22,
                SshAuthMethod.PASSWORD,
                displayedName + "-user",
                "scBkrr5+CFCcqA8kTNBknw=="
        );
        Computer computer = new Computer(
                displayedName,
                host,
                "B-1-12",
                sshConfig,
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

        computer.SetComputersAndSshConfigsManager(computersAndSshConfigsManager);
        computer.SetPreferences(Utilities.ConvertListOfIPreferencesToPreferences(iPreferences));
        computer.AddToDb();

        return computer;
    }

    public static Computer AddComputerWithGlobalSshConfig(String displayedName, String host, String sshConfigName)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        SshConfig sshConfig = computersAndSshConfigsManager.GetGlobalSshConfigByName(sshConfigName);
        Computer computer = new Computer(
                displayedName,
                host,
                "B-1-12",
                sshConfig,
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

        computer.SetComputersAndSshConfigsManager(computersAndSshConfigsManager);
        computer.SetPreferences(Utilities.ConvertListOfIPreferencesToPreferences(iPreferences));
        computer.AddToDb();

        return computer;
    }

    public static SshConfig AddGlobalSshConfig(String sshConfigName)
    {
        ComputersAndSshConfigsManager computersAndSshConfigsManager = new ComputersAndSshConfigsManager();
        SshConfig sshConfig = new SshConfig(
                null,
                SshConfigScope.GLOBAL,
                22,
                SshAuthMethod.PASSWORD,
                sshConfigName+ "-user",
                "scBkrr5+CFCcqA8kTNBknw=="
        );

        sshConfig.SetComputersAndSshConfigsManager(computersAndSshConfigsManager);
        sshConfig.AddToDb();

        return sshConfig;
    }

    public static void CreateCpuLogsForComputer(Computer computer, int gatheringsNum)
    {
        Instant now = Instant.now();
        List<CpuLog> logs = new ArrayList<>();
        for(int i = gatheringsNum; i > 0; --i)
        {
            double randomNum = 1 + Math.random() * (10 - 1);
            Timestamp timestamp = new Timestamp(now.minusMillis(i * 621).toEpochMilli());

            CpuInfo cpuInfo = new CpuInfo(String.valueOf(randomNum));
            CpuLog cpuLog = new CpuLog(computer,cpuInfo);
            cpuLog.Timestamp = timestamp;
            logs.add(cpuLog);
        }

        Session session = DatabaseManager.GetInstance().GetSession();

        for (CpuLog log : logs)
        {
            session.save(log);
        }

        session.close();
    }

    public static void CreateSwapLogsForComputer(Computer computer, int gatheringsNum)
    {
        Instant now = Instant.now();
        List<SwapLog> logs = new ArrayList<>();
        for(int i = gatheringsNum; i > 0; --i)
        {
            double randomNum = 1 + Math.random() * (10 - 1);
            Timestamp timestamp = new Timestamp(now.minusMillis(i * 621).toEpochMilli());

            for(int j = 0; j < 3; ++j)
            {
                int num1 = new Random().nextInt((300000 - 2000) + 1) + 2000;
                int num2 = new Random().nextInt((300000 - 2000) + 1) + 2000;
                int num3 = new Random().nextInt((300000 - 2000) + 1) + 2000;
                String command = "Swap:            " + num1 + "          " + num2 + "          " + num3;
                SwapInfo swapInfo = new SwapInfo(command);
                SwapLog swapLog = new SwapLog(computer, swapInfo);
                swapLog.Timestamp = timestamp;

                logs.add(swapLog);
            }
        }

        Session session = DatabaseManager.GetInstance().GetSession();

        for (SwapLog log : logs)
        {
            session.save(log);
        }

        session.close();
    }
}
