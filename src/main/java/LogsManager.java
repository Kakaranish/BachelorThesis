import Entities.Classroom;
import Entities.ComputerEntity;
import Entities.Logs.BaseEntity;
import Preferences.IPreference;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogsManager
{
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;

    public LogsManager(LogsGatherer logsGatherer, LogsMaintainer logsMaintainer)
    {
        _logsGatherer = logsGatherer;
        _logsMaintainer = logsMaintainer;
    }

    public void StartWork(List<Computer> selectedComputers)
    {
        // Get computers with which connection can be established
    }
}
