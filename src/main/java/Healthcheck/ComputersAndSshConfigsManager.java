package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.MainDatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComputersAndSshConfigsManager
{
    public final static String ModuleName = "ComputersAndSshConfigsManager";

    private List<Computer> _computers;
    private List<SshConfig> _sshConfigs;

    // ---  CONSTRUCTOR & INIT METHODS ---------------------------------------------------------------------------------

    public ComputersAndSshConfigsManager()
    {
        _sshConfigs = GetSshConfigsFromDb();
        SetReferenceToThisObjectInSshConfigs(_sshConfigs);
        _computers = GetComputersFromSshConfigs();
        SetReferenceToThisObjectInComputers(_computers);
    }

    private List<SshConfig> GetSshConfigsFromDb()
    {
        String attemptErrorMessage = "Attempt of getting ssh configs from db failed.";

        Session session = MainDatabaseManager.GetInstance().GetSession();
        String hql = "from SshConfig c";
        Query query = session.createQuery(hql);
        List<SshConfig> sshConfigs
                = MainDatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, ModuleName, attemptErrorMessage);
        session.close();

        if(sshConfigs != null)
        {
            return sshConfigs;
        }
        else
        {
            throw new DatabaseException("Unable to get ssh configs from db.");
        }
    }

    private void SetReferenceToThisObjectInSshConfigs(List<SshConfig> sshConfigs)
    {
        for (SshConfig sshConfig : sshConfigs)
        {
            sshConfig.SetComputersAndSshConfigsManager(this);
        }
    }

    private List<Computer> GetComputersFromSshConfigs()
    {
        List<Computer> computers = new ArrayList<>();
        for (SshConfig sshConfig : _sshConfigs)
        {
            for (Computer computer : sshConfig.GetComputers())
            {
                computers.add(computer);
            }
        }
        return computers;
    }

    private void SetReferenceToThisObjectInComputers(List<Computer> computers)
    {
        for (Computer computer: computers)
        {
            computer.SetComputersAndSshConfigsManager(this);
        }
    }

    // ---  CALLBACKS  -------------------------------------------------------------------------------------------------

    public void AddedComputer(Computer computer)
    {
        _computers.add(computer);
        computer.SetComputersAndSshConfigsManager(this);
    }

    public void SshConfigInComputerChangedFromLocalToGlobal(SshConfig localSshConfigToRemove)
    {
        _sshConfigs.remove(localSshConfigToRemove);
    }

    public void SshConfigInComputerChangedFromGlobalToLocal(SshConfig localSshConfigToAdd)
    {
        _sshConfigs.add(localSshConfigToAdd);
    }

    public void RemovedComputer(Computer computer)
    {
        _computers.remove(computer);
    }

    public void AddedSshConfig(SshConfig sshConfig)
    {
        _sshConfigs.add(sshConfig);
        sshConfig.SetComputersAndSshConfigsManager(this);
    }

    public void RemovedSshConfig(SshConfig sshConfig)
    {
        _sshConfigs.remove(sshConfig);
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public boolean ComputerWithDisplayedNameExists(String displayedName)
    {
        return GetComputerByDisplayedName(displayedName) != null;
    }

    public boolean OtherComputerWithDisplayedNameExists(Computer computer, String displayedName)
    {

        List<Computer> results = _computers.stream()
                .filter(c -> c.GetDisplayedName().equals(displayedName) && c != computer).collect(Collectors.toList());

        return results.isEmpty() == false;
    }

    public boolean ComputerWithHostExists(String host)
    {
        return GetComputerByHost(host) != null;
    }

    public boolean OtherComputerWithHostExists(Computer computer, String host)
    {

        List<Computer> results = _computers.stream()
                .filter(c -> c.GetHost().equals(host) && c != computer).collect(Collectors.toList());

        return results.isEmpty() == false;
    }

    public boolean SshConfigWithNameExists(String name)
    {
        return GetGlobalSshConfigByName(name) != null;
    }

    public boolean OtherGlobalSshConfigWithNameExists(SshConfig sshConfig, String name)
    {
        List<SshConfig> results = _sshConfigs.stream()
                .filter(s -> s.GetName() != null && s.GetName().equals(name) && s != sshConfig).collect(Collectors.toList());

        return results.isEmpty() == false;
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

    public SshConfig GetSshConfigById(int id)
    {
        List<SshConfig> results = _sshConfigs.stream()
                .filter(c -> c.GetId() == id).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public SshConfig GetGlobalSshConfigByName(String name)
    {
        List<SshConfig> results = _sshConfigs.stream()
                .filter(c -> c.GetName() != null && c.GetName() .equals(name)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public List<Computer> GetSelectedComputers()
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.IsSelected() == true).collect(Collectors.toList());
        return results;
    }

    public Computer GetComputerById(int id)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.GetId() == id).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public Computer GetComputerByHost(String host)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.GetHost().equals(host)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public Computer GetComputerByDisplayedName(String displayedName)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.GetDisplayedName().equals(displayedName)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public List<Computer> GetComputers()
    {
        return _computers;
    }

    public List<SshConfig> GetSshConfigs()
    {
        return _sshConfigs;
    }

    public List<SshConfig> GetGlobalSshConfigs()
    {
        return _sshConfigs.stream().filter(s -> s.HasGlobalScope()).collect(Collectors.toList());
    }

    public List<Computer> GetComputersForClassroom(String classroom)
    {
        return _computers.stream().filter(c -> c.GetClassroom().equals(classroom)).collect(Collectors.toList());
    }
}
