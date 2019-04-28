package Models.Info;

import Entities.ComputerEntity;
import Entities.Logs.BaseEntity;
import java.util.Date;
import java.util.List;

public interface IInfo
{
    List<BaseEntity> ToLogList(ComputerEntity computerEntity, Date timestamp);
}
