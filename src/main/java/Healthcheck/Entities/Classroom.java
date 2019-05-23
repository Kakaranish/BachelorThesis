package Healthcheck.Entities;

import Healthcheck.Utilities;

import javax.persistence.*;

@Entity
@Table(name = "Classrooms")
public class Classroom
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(unique = true, nullable = false)
    public String Name;

    public String Description;

    private Classroom()
    {
    }

    public Classroom(String name, String description)
    {
        Id = null;
        Name = name;
        Description = description;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        Classroom other = (Classroom) obj;

        boolean succeed = this.Id == other.Id &&
                Utilities.AreEqual(this.Name, other.Name) &&
                Utilities.AreEqual(this.Description, other.Description);

        return succeed;
    }
}

