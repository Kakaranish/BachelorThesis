package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Entities.Logs.CpuLog;
import javax.persistence.Embeddable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class CpuInfo implements IInfo
{
    public double Last1MinuteAvgCpuUtil;
    public double Last5MinutesAvgCpuUtil;
    public double Last15MinutesAvgCpuUtil;
    public int ExecutingKernelSchedulingEntitiesNum;
    public int ExistingKernelSchedulingEntitiesNum;
    public int RecentlyCreatedProcessPID;

    /*
        commandExecutionResult looks like:
        2.01 19.84 12.88 1/69 17449

        Meaning of numbers:
        The first three fields in this file are load average figures giving the number of jobs in the run queue
        (state R) or waiting for disk I/O (state D) averaged over 1, 5, and 15 minutes.
        They are the same as the load average numbers given by uptime(1) and other programs.

        The fourth field consists of two numbers separated by a slash (/).
        The first of these is the number of currently executing kernel scheduling entities (processes, threads);
        this will be less than or equal to the number of CPUs. The value after the slash is the number of kernel
        scheduling entities that currently exist on the system.

        The fifth field is the PID of the process that was most recently created on the system
    */

    private CpuInfo()
    {
    }

    public CpuInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Last1MinuteAvgCpuUtil = Double.parseDouble(commandExecutionResultSplit[0]);
        Last5MinutesAvgCpuUtil = Double.parseDouble(commandExecutionResultSplit[1]);
        Last15MinutesAvgCpuUtil = Double.parseDouble(commandExecutionResultSplit[2]);

        String[] kernelSchedulingEntitiesSplit = commandExecutionResultSplit[3].split("/");
        ExecutingKernelSchedulingEntitiesNum = Integer.parseInt(kernelSchedulingEntitiesSplit[0]);
        ExistingKernelSchedulingEntitiesNum= Integer.parseInt(kernelSchedulingEntitiesSplit[1]);

        RecentlyCreatedProcessPID = Integer.parseInt(commandExecutionResultSplit[4]);
    }

    public List<LogBaseEntity> ToLogList(Computer computer, Timestamp timestamp)
    {
        List<LogBaseEntity> logList = new ArrayList<>();
        logList.add(new CpuLog(computer, this, timestamp));

        return logList;
    }
}
