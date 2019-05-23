package Healthcheck.Models.Info;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Logs.BaseEntity;
import java.sql.Timestamp;
import java.util.List;

public interface IInfo
{
    List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp);
}
