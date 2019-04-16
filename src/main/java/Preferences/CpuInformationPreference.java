package Preferences;

import InformationModels.CPUInformationModel;
import InformationModels.IInformationModel;

public class CpuInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage \"%\"}'";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new CPUInformationModel(commandExecutionResult);
    }
}
