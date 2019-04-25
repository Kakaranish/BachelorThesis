package Entities;

import javax.persistence.*;

@Entity
@Table(name = "Computers")
public class Computer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComputerId")
    public Long Id;

    public String Username;
    public String Host;
    public String Password;

    /*
        TODO: To add:
        - MaintainPeriod
        - RequestInterval
        - LogExpiration
     */

    public Computer()
    {
    }

    public Computer(Long id, String username, String host, String password)
    {
        Id = id;
        Username = username;
        Host = host;
        Password = password;
    }
}
