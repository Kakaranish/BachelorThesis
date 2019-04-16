package InformationModels;

public class CPUInformationModel implements IInformationModel
{
    public String TotalCpu_Percentage;

    /*
        commandExecutionResult looks like:
        1.43196%
    */

    public CPUInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        TotalCpu_Percentage = commandExecutionResult;
    }
}
