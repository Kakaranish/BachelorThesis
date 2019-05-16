package Healthcheck.Entities;

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
}

