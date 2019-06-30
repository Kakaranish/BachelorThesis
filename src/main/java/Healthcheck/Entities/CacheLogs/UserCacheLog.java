package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Entities.Logs.SwapLog;
import Healthcheck.Entities.Logs.UserLog;
import Healthcheck.Models.Info.UserInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UsersCacheLogs")
public class UserCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public UserInfo UserInfo;

    private UserCacheLog()
    {
    }

    public UserCacheLog(UserLog userLog)
    {
        super(userLog);

        UserInfo = userLog.UserInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new UserLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new UserLog(this, computer);
    }
}
