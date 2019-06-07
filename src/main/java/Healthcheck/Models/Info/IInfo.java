package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.BaseEntity;
import java.sql.Timestamp;
import java.util.List;

public interface IInfo
{
    List<BaseEntity> ToLogList(Computer computer, Timestamp timestamp);
}
