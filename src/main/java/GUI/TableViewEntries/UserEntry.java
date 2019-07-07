package GUI.TableViewEntries;

import javafx.beans.property.SimpleStringProperty;

public class UserEntry extends LogEntry
{
    public SimpleStringProperty FromWhere;
    public SimpleStringProperty Idle;
    public SimpleStringProperty JCPU;
    public SimpleStringProperty PCPU;
    public SimpleStringProperty LoginAt;
    public SimpleStringProperty TTY;
    public SimpleStringProperty User;
    public SimpleStringProperty What;

    public String getFromWhere()
    {
        return FromWhere.get();
    }

    public SimpleStringProperty fromWhereProperty()
    {
        return FromWhere;
    }

    public void setFromWhere(String fromWhere)
    {
        this.FromWhere.set(fromWhere);
    }

    public String getIdle()
    {
        return Idle.get();
    }

    public SimpleStringProperty idleProperty()
    {
        return Idle;
    }

    public void setIdle(String idle)
    {
        this.Idle.set(idle);
    }

    public String getJCPU()
    {
        return JCPU.get();
    }

    public SimpleStringProperty JCPUProperty()
    {
        return JCPU;
    }

    public void setJCPU(String JCPU)
    {
        this.JCPU.set(JCPU);
    }

    public String getPCPU()
    {
        return PCPU.get();
    }

    public SimpleStringProperty PCPUProperty()
    {
        return PCPU;
    }

    public void setPCPU(String PCPU)
    {
        this.PCPU.set(PCPU);
    }

    public String getLoginAt()
    {
        return LoginAt.get();
    }

    public SimpleStringProperty loginAtProperty()
    {
        return LoginAt;
    }

    public void setLoginAt(String loginAt)
    {
        this.LoginAt.set(loginAt);
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

    public String getWhat()
    {
        return What.get();
    }

    public SimpleStringProperty whatProperty()
    {
        return What;
    }

    public void setWhat(String what)
    {
        this.What.set(what);
    }
}
