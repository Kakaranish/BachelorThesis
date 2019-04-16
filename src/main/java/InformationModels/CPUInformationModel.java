package InformationModels;

public class CPUInformationModel implements IInformationModel
{
    public double CpuPercentage;

    /*
        commandExecutionResult looks like:
        1.43196%
    */

    public CPUInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        CpuPercentage = Double.parseDouble(commandExecutionResult);
    }
}
