package GUI.TableViewEntries;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProcessEntry extends LogEntry
{
    public SimpleStringProperty User;
    public SimpleLongProperty PID;
    public SimpleDoubleProperty CPU_Percentage;
    public SimpleDoubleProperty Memory_Percentage;
    public SimpleLongProperty VSZ;
    public SimpleLongProperty RSS;
    public SimpleStringProperty TTY;
    public SimpleStringProperty Stat;
    public SimpleStringProperty Start;
    public SimpleStringProperty Time;
    public SimpleStringProperty Command;

    public String getUser()
    {
        return User.get();
    }

    public SimpleStringProperty userProperty()
    {
        return User;
    }

    public void setUser(String user)
    {
        this.User.set(user);
    }

    public long getPID()
    {
        return PID.get();
    }

    public SimpleLongProperty PIDProperty()
    {
        return PID;
    }

    public void setPID(long PID)
    {
        this.PID.set(PID);
    }

    public double getCPU_Percentage()
    {
        return CPU_Percentage.get();
    }

    public SimpleDoubleProperty CPU_PercentageProperty()
    {
        return CPU_Percentage;
    }

    public void setCPU_Percentage(double CPU_Percentage)
    {
        this.CPU_Percentage.set(CPU_Percentage);
    }

    public double getMemory_Percentage()
    {
        return Memory_Percentage.get();
    }

    public SimpleDoubleProperty memory_PercentageProperty()
    {
        return Memory_Percentage;
    }

    public void setMemory_Percentage(double memory_Percentage)
    {
        this.Memory_Percentage.set(memory_Percentage);
    }

    public long getVSZ()
    {
        return VSZ.get();
    }

    public SimpleLongProperty VSZProperty()
    {
        return VSZ;
    }

    public void setVSZ(long VSZ)
    {
        this.VSZ.set(VSZ);
    }

    public long getRSS()
    {
        return RSS.get();
    }

    public SimpleLongProperty RSSProperty()
    {
        return RSS;
    }

    public void setRSS(long RSS)
    {
        this.RSS.set(RSS);
    }

    public String getTTY()
    {
        return TTY.get();
    }

    public SimpleStringProperty TTYProperty()
    {
        return TTY;
    }

    public void setTTY(String TTY)
    {
        this.TTY.set(TTY);
    }

    public String getStat()
    {
        return Stat.get();
    }

    public SimpleStringProperty statProperty()
    {
        return Stat;
    }

    public void setStat(String stat)
    {
        this.Stat.set(stat);
    }

    public String getStart()
    {
        return Start.get();
    }

    public SimpleStringProperty startProperty()
    {
        return Start;
    }

    public void setStart(String start)
    {
        this.Start.set(start);
    }

    public String getTime()
    {
        return Time.get();
    }

    public SimpleStringProperty timeProperty()
    {
        return Time;
    }

    public void setTime(String time)
    {
        this.Time.set(time);
    }

    public String getCommand()
    {
        return Command.get();
    }

    public SimpleStringProperty commandProperty()
    {
        return Command;
    }

    public void setCommand(String command)
    {
        this.Command.set(command);
    }
}
