package Entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ComputersPreferences", uniqueConstraints = {@UniqueConstraint(columnNames = {"PreferenceId", "ComputerHost"})})

public class ComputerEntityPreference implements Serializable
{
    @Id
    @ManyToOne
    @JoinColumn(name = "ComputerHost", referencedColumnName = "Host")
    public ComputerEntity ComputerEntity;

    @Id
    @ManyToOne
    @JoinColumn(name = "PreferenceId", referencedColumnName = "Id")
    public Preference Preference;

    public ComputerEntityPreference()
    {
    }

    public ComputerEntityPreference(ComputerEntity computerEntity, Preference preference)
    {
        ComputerEntity = computerEntity;
        Preference = preference;
    }
}
