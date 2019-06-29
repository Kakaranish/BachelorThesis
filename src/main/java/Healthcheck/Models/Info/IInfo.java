package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import java.sql.Timestamp;
import java.util.List;

public interface IInfo
{
    List<LogBaseEntity> ToLogList(Computer computer, Timestamp timestamp);
}
