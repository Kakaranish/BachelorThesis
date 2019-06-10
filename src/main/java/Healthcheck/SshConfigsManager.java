package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigScope;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SshConfigsManager
{
    private List<SshConfig> _globalSshConfigs;

    public SshConfigsManager()
    {
        _globalSshConfigs = GetGlobalSshConfigsFromDb();
    }

    private List<SshConfig> GetGlobalSshConfigsFromDb()
    {
        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
                "Attempt of getting global ssh configurations from db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();

        String hql = "from SshConfig c where c.Scope = :ConfigScope";
        Query query = session.createQuery(hql);
        query.setParameter("ConfigScope", SshConfigScope.GLOBAL);
        List<SshConfig> globalSSHConfigurations
                = DatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, attemptErrorMessage);

        session.close();

        if(globalSSHConfigurations != null)
        {
            return globalSSHConfigurations;
        }
        else
        {
            throw new DatabaseException("Unable to get global ssh configurations from db.");
        }
    }

    public SshConfig GetGlobalSshConfigByName(String name)
    {
        List<SshConfig> results = _globalSshConfigs.stream()
                .filter(c -> c.GetName().equals(name)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

}
