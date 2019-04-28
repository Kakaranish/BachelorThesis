package Entities.Logs;

import Entities.ComputerEntity;
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

    public UserLog(ComputerEntity computerEntity, UserInfo userInfo)
    {
        super(computerEntity);
        UserInfo = userInfo;
    }

    public UserLog(ComputerEntity computerEntity, UserInfo userInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        UserInfo = userInfo;
    }
}
