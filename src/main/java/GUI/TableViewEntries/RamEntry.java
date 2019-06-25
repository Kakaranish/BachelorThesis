package GUI.TableViewEntries;

import javafx.beans.property.SimpleLongProperty;

public class RamEntry extends LogEntry
{
    public SimpleLongProperty Total;
    public SimpleLongProperty Used;
    public SimpleLongProperty Free;
    public SimpleLongProperty Shared;
    public SimpleLongProperty Buffers;
    public SimpleLongProperty Cached;

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

    public long getShared()
    {
        return Shared.get();
    }

    public SimpleLongProperty sharedProperty()
    {
        return Shared;
    }

    public void setShared(long shared)
    {
        this.Shared.set(shared);
    }

    public long getBuffers()
    {
        return Buffers.get();
    }

    public SimpleLongProperty buffersProperty()
    {
        return Buffers;
    }

    public void setBuffers(long buffers)
    {
        this.Buffers.set(buffers);
    }

    public long getCached()
    {
        return Cached.get();
    }

    public SimpleLongProperty cachedProperty()
    {
        return Cached;
    }

    public void setCached(long cached)
    {
        this.Cached.set(cached);
    }
}
