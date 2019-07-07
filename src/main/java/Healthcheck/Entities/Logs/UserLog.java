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
import javax.persistence.Transient;
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

    @Transient
    public static UserLog CreateEmptyUserLog(Computer computer, Timestamp timestamp)
    {
        return new UserLog(computer, Healthcheck.Models.Info.UserInfo.GetEmptyUserInfo(), timestamp);
    }

    @Override
    public LogEntry ToEntry()
    {
        return new UserEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));

            if(UserInfo != null)
            {
                FromWhere = new SimpleStringProperty(UserInfo.FromWhere);
                Idle = new SimpleStringProperty(UserInfo.Idle);
                JCPU = new SimpleStringProperty(UserInfo.JCPU);
                PCPU = new SimpleStringProperty(UserInfo.PCPU);
                LoginAt = new SimpleStringProperty(UserInfo.LoginAt);
                TTY = new SimpleStringProperty(UserInfo.TTY);
                User = new SimpleStringProperty(UserInfo.User);
                What = new SimpleStringProperty(UserInfo.What);
            }
            else
            {
                FromWhere = new SimpleStringProperty("");
                Idle = new SimpleStringProperty("");
                JCPU = new SimpleStringProperty("");
                PCPU = new SimpleStringProperty("");
                LoginAt = new SimpleStringProperty("");
                TTY = new SimpleStringProperty("");
                User = new SimpleStringProperty("");
                What = new SimpleStringProperty("");
            }
        }};
    }

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new UserCacheLog(this);
    }
}
