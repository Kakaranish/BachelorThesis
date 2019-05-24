package Healthcheck.Entities;

import javax.persistence.*;

@Entity
@Table(name = "Preferences")
public class Preference
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(unique = true, nullable = false)
    public String ClassName;

    private Preference()
    {
    }

    public Preference(String className)
    {
        Id = null;
        ClassName = className;
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
