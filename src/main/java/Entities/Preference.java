package Entities;

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
}
