package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.UserEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.CacheLogs.UserCacheLog;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.UserInfo;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "UsersLogs")
public class UserLog extends LogBase
{
    @Embedded
    public UserInfo UserInfo;

    private UserLog()
    {
    }

    public UserLog(Computer computer, UserInfo userInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        UserInfo = userInfo;
    }

    public UserLog(UserCacheLog userCacheLog, Computer computer)
    {
        super(userCacheLog.LogId, computer, userCacheLog.Timestamp);

        UserInfo = userCacheLog.UserInfo;
    }

    public UserLog(UserCacheLog userCacheLog, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        super(userCacheLog.LogId, computersAndSshConfigsManager.GetComputerById(userCacheLog.ComputerId),
                userCacheLog.Timestamp);

        UserInfo = userCacheLog.UserInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new UserEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            FromWhere = new SimpleStringProperty(UserInfo.FromWhere);
            Idle = new SimpleStringProperty(UserInfo.Idle);
            JCPU = new SimpleStringProperty(UserInfo.JCPU);
            PCPU = new SimpleStringProperty(UserInfo.PCPU);
            SAT15 = new SimpleStringProperty(UserInfo.SAT15);
            TTY = new SimpleStringProperty(UserInfo.TTY);
            User = new SimpleStringProperty(UserInfo.User);
            What = new SimpleStringProperty(UserInfo.What);
        }};
    }

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new UserCacheLog(this);
    }
}
