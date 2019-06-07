package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.UserInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "UsersLogs")
public class UserLog extends BaseEntity
{
    @Embedded
    public UserInfo UserInfo;

    private UserLog()
    {
    }

    public UserLog(Computer computer, UserInfo userInfo)
    {
        super(computer);
        UserInfo = userInfo;
    }

    public UserLog(Computer computer, UserInfo userInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        UserInfo = userInfo;
    }
}
