package GUI.TableViewEntries;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CpuEntry extends LogEntry
{
    public SimpleDoubleProperty Last1MinuteAvgCpuUtil;
    public SimpleDoubleProperty Last5MinutesAvgCpuUtil;
    public SimpleDoubleProperty Last15MinutesAvgCpuUtil;
    public SimpleIntegerProperty ExecutingKernelSchedulingEntitiesNum;
    public SimpleIntegerProperty ExistingKernelSchedulingEntitiesNum;
    public SimpleIntegerProperty RecentlyCreatedProcessPID;

    public double getLast1MinuteAvgCpuUtil()
    {
        return Last1MinuteAvgCpuUtil.get();
    }

    public SimpleDoubleProperty last1MinuteAvgCpuUtilProperty()
    {
        return Last1MinuteAvgCpuUtil;
    }

    public void setLast1MinuteAvgCpuUtil(double last1MinuteAvgCpuUtil)
    {
        this.Last1MinuteAvgCpuUtil.set(last1MinuteAvgCpuUtil);
    }

    public double getLast5MinutesAvgCpuUtil()
    {
        return Last5MinutesAvgCpuUtil.get();
    }

    public SimpleDoubleProperty last5MinutesAvgCpuUtilProperty()
    {
        return Last5MinutesAvgCpuUtil;
    }

    public void setLast5MinutesAvgCpuUtil(double last5MinutesAvgCpuUtil)
    {
        this.Last5MinutesAvgCpuUtil.set(last5MinutesAvgCpuUtil);
    }

    public double getLast15MinutesAvgCpuUtil()
    {
        return Last15MinutesAvgCpuUtil.get();
    }

    public SimpleDoubleProperty last15MinutesAvgCpuUtilProperty()
    {
        return Last15MinutesAvgCpuUtil;
    }

    public void setLast15MinutesAvgCpuUtil(double last15MinutesAvgCpuUtil)
    {
        this.Last15MinutesAvgCpuUtil.set(last15MinutesAvgCpuUtil);
    }

    public int getExecutingKernelSchedulingEntitiesNum()
    {
        return ExecutingKernelSchedulingEntitiesNum.get();
    }

    public SimpleIntegerProperty executingKernelSchedulingEntitiesNumProperty()
    {
        return ExecutingKernelSchedulingEntitiesNum;
    }

    public void setExecutingKernelSchedulingEntitiesNum(int executingKernelSchedulingEntitiesNum)
    {
        this.ExecutingKernelSchedulingEntitiesNum.set(executingKernelSchedulingEntitiesNum);
    }

    public int getExistingKernelSchedulingEntitiesNum()
    {
        return ExistingKernelSchedulingEntitiesNum.get();
    }

    public SimpleIntegerProperty existingKernelSchedulingEntitiesNumProperty()
    {
        return ExistingKernelSchedulingEntitiesNum;
    }

    public void setExistingKernelSchedulingEntitiesNum(int existingKernelSchedulingEntitiesNum)
    {
        this.ExistingKernelSchedulingEntitiesNum.set(existingKernelSchedulingEntitiesNum);
    }

    public int getRecentlyCreatedProcessPID()
    {
        return RecentlyCreatedProcessPID.get();
    }

    public SimpleIntegerProperty recentlyCreatedProcessPIDProperty()
    {
        return RecentlyCreatedProcessPID;
    }

    public void setRecentlyCreatedProcessPID(int recentlyCreatedProcessPID)
    {
        this.RecentlyCreatedProcessPID.set(recentlyCreatedProcessPID);
    }
}
