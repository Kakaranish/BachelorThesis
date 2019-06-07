package Healthcheck.Entities;

import Healthcheck.Preferences.IPreference;
import Healthcheck.Utilities;
import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Computers")
public class Computer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(nullable = false, unique = true)
    public String DisplayedName;

    @Column(nullable = false, unique = true)
    public String Host;

    @Column(nullable = false)
    public String Classroom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SSHConfiguration_Id", referencedColumnName = "Id", nullable = false)
    public SSHConfiguration SSHConfiguration;

    @Column(nullable = false)
    public Duration MaintainPeriod;

    @Column(nullable = false)
    public Duration RequestInterval;

    @Column(nullable = false)
    public Duration LogExpiration;

    @Column(nullable = false)
    public Timestamp LastMaintenance;

    public boolean IsSelected;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Computer_Preference",
            inverseJoinColumns = { @JoinColumn(name = "Preference_Id", referencedColumnName = "Id") },
            joinColumns = { @JoinColumn(name = "Computer_Id", referencedColumnName = "Id") }
    )
    public List<Preference> Preferences = new ArrayList<>();

    public Computer()
    {
    }

    public Computer(
            String displayedName,
            String host,
            String classroom,
            SSHConfiguration sshConfiguration,
            Duration requestInterval,
            Duration maintainPeriod,
            Duration logExpiration,
            boolean isSelected)
    {
        DisplayedName = displayedName;
        Host = host;
        Classroom = classroom;
        SSHConfiguration = sshConfiguration;
        RequestInterval = requestInterval;
        MaintainPeriod = maintainPeriod;
        LogExpiration = logExpiration;
        LastMaintenance = new Timestamp(System.currentTimeMillis());
        IsSelected = isSelected;
    }

    // Copy constructor
    public Computer(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
        SSHConfiguration = computer.SSHConfiguration != null ? new SSHConfiguration(computer.SSHConfiguration) : null;
        MaintainPeriod = computer.MaintainPeriod;
        RequestInterval = computer.RequestInterval;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;

        // TODO: Check if it's enough
        Preferences = computer.Preferences;
    }

    public void CopyFrom(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
        SSHConfiguration.CopyFrom(computer.SSHConfiguration);
        RequestInterval = computer.RequestInterval;
        MaintainPeriod = computer.MaintainPeriod;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;

        // TODO: Check if it's enough
        Preferences = computer.Preferences;
    }

    public void SetPreferences(List<Preference> preferences)
    {
        Preferences = preferences;
        for (Preference preference : preferences)
        {
            preference.AddComputer(this);
        }
    }

    public boolean HasSetRequiredFields()
    {
        return  DisplayedName != null &&
                Host != null &&
                Classroom != null &&
                SSHConfiguration != null &&
                RequestInterval != null &&
                MaintainPeriod != null &&
                LogExpiration != null &&
                LastMaintenance != null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        Computer other = (Computer) obj;
        return  this.Id == other.Id &&
                Utilities.AreEqual(this.DisplayedName, other.DisplayedName) &&
                Utilities.AreEqual(this.Host, other.Host) &&
                Utilities.AreEqual(this.Classroom, other.Classroom) &&
                Utilities.AreEqual(this.SSHConfiguration, other.SSHConfiguration) &&
                this.IsSelected == other.IsSelected &&
                Utilities.AreEqual(this.MaintainPeriod, other.MaintainPeriod) &&
                Utilities.AreEqual(this.LogExpiration, other.LogExpiration) &&
                Utilities.AreEqual(this.RequestInterval, other.RequestInterval) &&
                Utilities.AreEqual(this.LastMaintenance, other.LastMaintenance) &&
                (this.Preferences == other.Preferences ||
                        (this.Preferences.containsAll(other.Preferences) &&
                                other.Preferences.containsAll(this.Preferences)));
    }

    public boolean HasPreferenceWithGivenClassName(String preferenceClassName)
    {
        List<Preference> results = Preferences.stream()
                .filter(p -> p.ClassName.equals(preferenceClassName)).collect(Collectors.toList());

        return !results.isEmpty();
    }

    public List<IPreference> GetIPreferences()
    {
        List<IPreference> iPreferences = new ArrayList<>();
        for (Preference preference : Preferences)
        {
            iPreferences.add(Healthcheck.Preferences.Preferences.PreferenceClassNameMap.get(preference.ClassName));
        }

        return iPreferences;
    }
}