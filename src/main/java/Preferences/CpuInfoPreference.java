package Preferences;

import Models.Info.CpuInfo;
import Models.Info.IInfo;

public class CpuInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "CpuLog";
    }

    public String GetCommandToExecute()
    {
        return "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new CpuInfo(commandExecutionResult);
    }
}
