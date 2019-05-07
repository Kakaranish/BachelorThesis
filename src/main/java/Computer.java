import Entities.ComputerEntity;
import Preferences.IPreference;

import java.util.ArrayList;
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

    public Computer(Computer computer)
    {
        ComputerEntity = new ComputerEntity(computer.ComputerEntity);

        if(ComputerPreferences == null)
        {
            ComputerPreferences = new ArrayList<>();
        }

        for (IPreference computerPreference : computer.ComputerPreferences)
        {
            ComputerPreferences.add(computerPreference);
        }
    }
}
