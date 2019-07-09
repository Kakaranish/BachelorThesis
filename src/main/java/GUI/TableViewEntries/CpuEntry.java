package GUI.TableViewEntries;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class CpuEntry extends LogEntry
{
    public SimpleStringProperty CpuName;
    public SimpleLongProperty User;
    public SimpleLongProperty Nice;
    public SimpleLongProperty System;
    public SimpleLongProperty Idle;
    public SimpleLongProperty Iowait;
    public SimpleLongProperty Irq;
    public SimpleLongProperty Softirq;
    public SimpleLongProperty Steal;
    public SimpleLongProperty Quest;
    public SimpleLongProperty QuestNice;
    public SimpleBooleanProperty FirstBatch;

    public long getUser()
    {
        return User.get();
    }

    public SimpleLongProperty userProperty()
    {
        return User;
    }

    public void setUser(long user)
    {
        this.User.set(user);
    }

    public long getNice()
    {
        return Nice.get();
    }

    public SimpleLongProperty niceProperty()
    {
        return Nice;
    }

    public void setNice(long nice)
    {
        this.Nice.set(nice);
    }

    public long getSystem()
    {
        return System.get();
    }

    public SimpleLongProperty systemProperty()
    {
        return System;
    }

    public void setSystem(long system)
    {
        this.System.set(system);
    }

    public long getIdle()
    {
        return Idle.get();
    }

    public SimpleLongProperty idleProperty()
    {
        return Idle;
    }

    public void setIdle(long idle)
    {
        this.Idle.set(idle);
    }

    public long getIowait()
    {
        return Iowait.get();
    }

    public SimpleLongProperty iowaitProperty()
    {
        return Iowait;
    }

    public void setIowait(long iowait)
    {
        this.Iowait.set(iowait);
    }

    public long getIrq()
    {
        return Irq.get();
    }

    public SimpleLongProperty irqProperty()
    {
        return Irq;
    }

    public void setIrq(long irq)
    {
        this.Irq.set(irq);
    }

    public long getSoftirq()
    {
        return Softirq.get();
    }

    public SimpleLongProperty softirqProperty()
    {
        return Softirq;
    }

    public void setSoftirq(long softirq)
    {
        this.Softirq.set(softirq);
    }

    public long getSteal()
    {
        return Steal.get();
    }

    public SimpleLongProperty stealProperty()
    {
        return Steal;
    }

    public void setSteal(long steal)
    {
        this.Steal.set(steal);
    }

    public long getQuest()
    {
        return Quest.get();
    }

    public SimpleLongProperty questProperty()
    {
        return Quest;
    }

    public void setQuest(long quest)
    {
        this.Quest.set(quest);
    }

    public long getQuestNice()
    {
        return QuestNice.get();
    }

    public SimpleLongProperty questNiceProperty()
    {
        return QuestNice;
    }

    public void setQuestNice(long questNice)
    {
        this.QuestNice.set(questNice);
    }

    public String getCpuName()
    {
        return CpuName.get();
    }

    public SimpleStringProperty cpuNameProperty()
    {
        return CpuName;
    }

    public void setCpuName(String cpuName)
    {
        this.CpuName.set(cpuName);
    }

    public boolean isFirstBatch()
    {
        return FirstBatch.get();
    }

    public SimpleBooleanProperty firstBatchProperty()
    {
        return FirstBatch;
    }

    public void setFirstBatch(boolean firstBatch)
    {
        this.FirstBatch.set(firstBatch);
    }
}
