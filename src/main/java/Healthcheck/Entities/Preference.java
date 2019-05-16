package Healthcheck.Entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Preferences")
public class Preference
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(unique = true, nullable = false)
    public String ClassName;

    @ManyToMany(mappedBy = "Preferences")
    public List<ComputerEntity> ComputerEntities;

    private Preference()
    {
    }

    public Preference(String className)
    {
        Id = null;
        ClassName = className;
    }
}
