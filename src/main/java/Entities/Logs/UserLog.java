package Entities.Logs;

import Entities.Computer;
import Models.Info.UserInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "UsersLogs")
public class UserLog extends BaseEntity
{
    @Embedded
    public UserInfo UserInfo;

    public UserLog(Computer computer, UserInfo userInfo)
    {
        super(computer);
        UserInfo = userInfo;
    }

    public UserLog(Computer computer, UserInfo userInfo, Date timestamp)
    {
        super(computer, timestamp);
        UserInfo = userInfo;
    }
}
