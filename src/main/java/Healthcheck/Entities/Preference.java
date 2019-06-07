package Healthcheck.Entities;

import javax.persistence.*;
import java.util.ArrayList;
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

    @ManyToMany(mappedBy = "Preferences", cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
    private List<Computer> _computers = new ArrayList<>();

    public Preference()
    {
    }

    public Preference(String className)
    {
        Id = null;
        ClassName = className;
    }

    public void AddComputer(Computer computer)
    {
        _computers.add(computer);
    }

    public void RemoveComputer(Computer computer)
    {
        _computers.remove(computer);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        Preference other = (Preference) obj;
        return  this.Id == other.Id &&
                this.ClassName.equals(other.ClassName);
    }
}
