package GUI.TableViewEntries;

import javafx.beans.property.SimpleLongProperty;

public class SwapEntry extends LogEntry
{
    public SimpleLongProperty Total;
    public SimpleLongProperty Used;
    public SimpleLongProperty Free;

    public long getTotal()
    {
        return Total.get();
    }

    public SimpleLongProperty totalProperty()
    {
        return Total;
    }

    public void setTotal(long total)
    {
        this.Total.set(total);
    }

    public long getUsed()
    {
        return Used.get();
    }

    public SimpleLongProperty usedProperty()
    {
        return Used;
    }

    public void setUsed(long used)
    {
        this.Used.set(used);
    }

    public long getFree()
    {
        return Free.get();
    }

    public SimpleLongProperty freeProperty()
    {
        return Free;
    }

    public void setFree(long free)
    {
        this.Free.set(free);
    }
}
