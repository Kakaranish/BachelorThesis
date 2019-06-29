package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Logs.LogBaseEntity;
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

    public UserCacheLog(UserLog userLog)
    {
        super(userLog);

        UserInfo = userLog.UserInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager cpComputersAndSshConfigsManager)
    {
        return new UserLog(cpComputersAndSshConfigsManager.GetComputerById(ComputerId), UserInfo);
    }
}
