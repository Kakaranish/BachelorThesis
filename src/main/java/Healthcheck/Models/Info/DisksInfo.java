package Healthcheck.Models.Info;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Entities.Logs.DiskLog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisksInfo implements IInfo
{
    public List<DiskInfo> DisksInfo;

    /*
        commandExecutionResults looks like:
        rootfs                                                   20G  1.1G   18G   6% /
        udev                                                     10M     0   10M   0% /dev
        tmpfs                                                   197M  208K  197M   1% /run
        /dev/disk/by-uuid/c65788b2-da92-479d-b809-748e47399832   20G  1.1G   18G   6% /
        tmpfs                                                   5.0M     0  5.0M   0% /run/lock
        tmpfs                                                   393M     0  393M   0% /run/shm

    */

    private DisksInfo()
    {
    }

    public DisksInfo(String commandExecutionResults)
    {
        DisksInfo = new ArrayList<DiskInfo>();
        List<String> commandExecutionResultsSplit =
                new ArrayList<String>(Arrays.asList(commandExecutionResults.split("\\n")));
        commandExecutionResultsSplit.remove(0);

        for (String result : commandExecutionResultsSplit)
        {
            DisksInfo.add(new DiskInfo(result));
        }
    }

    public List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        for (DiskInfo diskInfo: DisksInfo)
        {
            logList.add(new DiskLog(computerEntity, diskInfo, timestamp));
        }

        return logList;
    }
}
