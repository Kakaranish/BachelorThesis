package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBase;
import java.sql.Timestamp;
import java.util.List;

public interface IInfo
{
    List<LogBase> ToLogList(Computer computer, Timestamp timestamp);
}
