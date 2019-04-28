package Entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ComputersPreferences", uniqueConstraints = {@UniqueConstraint(columnNames = {"PreferenceId", "ComputerHost"})})

public class ComputerPreference implements Serializable
{
    @Id
    @ManyToOne
    @JoinColumn(name = "ComputerHost", referencedColumnName = "Host")
    public Computer Computer;

    @Id
    @ManyToOne
    @JoinColumn(name = "PreferenceId", referencedColumnName = "Id")
    public Preference Preference;

    public ComputerPreference()
    {
    }

    public ComputerPreference(Computer computer, Preference preference)
    {
        Computer = computer;
        Preference = preference;
    }
}
