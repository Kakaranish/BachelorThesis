package Entities.Logs;

import Entities.Computer;

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
    @JoinColumn(name = "ComputerId")
    public Entities.Computer Computer;

    public BaseEntity(Computer computer)
    {
        Id = null;
        Computer = computer;
        Timestamp = new Date();
    }

    public BaseEntity(Computer computer, Long id)
    {
        Id = id;
        Computer = computer;
        Timestamp = new Date();
    }

    public BaseEntity(Computer computer, Long id, Date timestamp)
    {
        Id = id;
        Computer = computer;
        Timestamp = timestamp;
    }
}
