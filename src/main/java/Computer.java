import Entities.ComputerEntity;
import Preferences.IPreference;

import java.util.List;

public class Computer
{
    public ComputerEntity ComputerEntity; // Needed encapsulation of computer class
    public List<IPreference> ComputerPreferences;

    public Computer(Entities.ComputerEntity computerEntity, List<IPreference> computerPreferences)
    {
        ComputerEntity = computerEntity;
        ComputerPreferences = computerPreferences;
    }
}
