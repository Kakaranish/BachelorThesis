package Healthcheck.Models.Info;

import javax.persistence.Embeddable;

@Embeddable
public class CpuInfo
{
    public String CpuName;
    public long User;
    public long Nice;
    public long System;
    public long Idle;
    public long Iowait;
    public long Irq;
    public long Softirq;
    public long Steal;
    public long Quest;
    public long QuestNice;
    public boolean FirstBatch;

    /*
        commandExecutionResult looks like:
        cpu0 1921037 0 33796 66016825 13613 0 536 320 0 0

        commandExecution's column names in order:
        ----
        user
        nice
        system
        idle
        iowait
        irq
        softirq
        steal
        quest
        quest_nice
    */

    private CpuInfo()
    {
    }

    public CpuInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        String[] commandExecutionResultSplit = commandExecutionResult.split("\\s");

        CpuName = commandExecutionResultSplit[0];
        User = Long.parseLong(commandExecutionResultSplit[1]);
        Nice = Long.parseLong(commandExecutionResultSplit[2]);
        System = Long.parseLong(commandExecutionResultSplit[3]);
        Idle = Long.parseLong(commandExecutionResultSplit[4]);
        Iowait = Long.parseLong(commandExecutionResultSplit[5]);
        Irq = Long.parseLong(commandExecutionResultSplit[6]);
        Softirq = Long.parseLong(commandExecutionResultSplit[7]);
        Steal = Long.parseLong(commandExecutionResultSplit[8]);
        Quest = Long.parseLong(commandExecutionResultSplit[9]);
        QuestNice = Long.parseLong(commandExecutionResultSplit[10]);
    }
}
