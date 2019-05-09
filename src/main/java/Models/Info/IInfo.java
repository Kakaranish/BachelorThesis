package Models.Info;

import Entities.ComputerEntity;
import Entities.Logs.BaseEntity;
import java.sql.Timestamp;
import java.util.List;

public interface IInfo
{
    List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp);
}
