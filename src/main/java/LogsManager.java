import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class LogsManager
{
    public final int NumOfRetries =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("NumOfRetries"));
    public final int Cooldown =
            Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("Cooldown"));

    private List<ComputerLogger> _computerLoggers;

    public LogsManager(List<Computer> computers)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        _computerLoggers = new ArrayList<>();
        for (Computer computer : computers)
        {
            _computerLoggers.add(new ComputerLogger(this, computer));
        }

        // Create&Run MaintainManager

        // Run logs gathering for all computerLoggers
        for (ComputerLogger computerLogger : _computerLoggers)
        {
            computerLogger.StartGatheringLogs();
        }

        try
        {
            Thread.sleep(10000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        _computerLoggers.get(0).StopGatheringLogs();
    }

    public void GatheringStoppedCallback(ComputerLogger computerLogger)
    {
        // Placeholder
        System.out.println("Stopped");
    }

    public void GatheringSSHConnectionErrorCallback(ComputerLogger computerLogger)
    {
        System.out.println("Unable to connect with host.");
        _computerLoggers.remove(computerLogger);
    }
}
