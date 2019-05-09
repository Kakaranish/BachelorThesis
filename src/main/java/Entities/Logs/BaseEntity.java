package Entities.Logs;

import Entities.ComputerEntity;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public class BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Timestamp Timestamp;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Host")
    public ComputerEntity ComputerEntity;

    protected BaseEntity()
    {
    }

    public BaseEntity(ComputerEntity computerEntity)
    {
        Id = null;
        ComputerEntity = computerEntity;
        Timestamp = new Timestamp(System.currentTimeMillis());
    }

    public BaseEntity(ComputerEntity computerEntity, Timestamp timestamp)
    {
        Id = null;
        ComputerEntity = computerEntity;
        Timestamp = timestamp;
    }
}
