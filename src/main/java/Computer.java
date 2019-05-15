import Entities.ComputerEntity;
import Preferences.IPreference;

import java.util.ArrayList;
import java.util.List;

public class Computer
{
    public ComputerEntity ComputerEntity; // Needed encapsulation of computer class
    public List<IPreference> Preferences;

    public Computer(Entities.ComputerEntity computerEntity, List<IPreference> computerPreferences)
    {
        ComputerEntity = computerEntity;
        Preferences = computerPreferences;
    }

    public Computer(Computer computer)
    {
        ComputerEntity = new ComputerEntity(computer.ComputerEntity);

        if(Preferences == null)
        {
            Preferences = new ArrayList<>();
        }

        for (IPreference computerPreference : computer.Preferences)
        {
            Preferences.add(computerPreference);
        }
    }

    public boolean IsComputerReadyForConnection()
    {
        return  ComputerEntity.GetUsername() != null &&
                ComputerEntity.GetEncryptedPassword()!= null &&
                ComputerEntity.GetSSHKey() != null;
    }
}
