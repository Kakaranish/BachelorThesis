package Models.Info;

import Entities.Computer;
import Entities.Logs.BaseEntity;
import java.util.Date;
import java.util.List;

public interface IInfo
{
    List<BaseEntity> ToLogList(Computer computer, Date timestamp);
}
