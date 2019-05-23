package Healthcheck.Entities.Logs;

import Healthcheck.Entities.ComputerEntity;
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

    public UserLog(ComputerEntity computerEntity, UserInfo userInfo)
    {
        super(computerEntity);
        UserInfo = userInfo;
    }

    public UserLog(ComputerEntity computerEntity, UserInfo userInfo, Timestamp timestamp)
    {
        super(computerEntity, timestamp);
        UserInfo = userInfo;
    }
}
