package Entities.Logs;

import Entities.ComputerEntity;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
public class BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    @Column(columnDefinition="DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date Timestamp;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Host")
    public ComputerEntity ComputerEntity;

    public BaseEntity(ComputerEntity computerEntity)
    {
        Id = null;
        ComputerEntity = computerEntity;
        Timestamp = new Date();
    }

    public BaseEntity(ComputerEntity computerEntity, Date timestamp)
    {
        Id = null;
        ComputerEntity = computerEntity;
        Timestamp = timestamp;
    }
}
