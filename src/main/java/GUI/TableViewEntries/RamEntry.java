package GUI.TableViewEntries;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class RamEntry extends LogEntry
{
    public SimpleLongProperty Total;
    public SimpleLongProperty Used;
    public SimpleLongProperty Free;
    public SimpleLongProperty Shared;
    public SimpleStringProperty Buffers;
    public SimpleStringProperty Cached;
    public SimpleStringProperty BuffersCached;
    public SimpleStringProperty Available;

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

    public String getBuffers()
    {
        return Buffers.get();
    }

    public SimpleStringProperty buffersProperty()
    {
        return Buffers;
    }

    public void setBuffers(String buffers)
    {
        this.Buffers.set(buffers);
    }

    public String getCached()
    {
        return Cached.get();
    }

    public SimpleStringProperty cachedProperty()
    {
        return Cached;
    }

    public void setCached(String cached)
    {
        this.Cached.set(cached);
    }

    public String getBuffersCached()
    {
        return BuffersCached.get();
    }

    public SimpleStringProperty buffersCachedProperty()
    {
        return BuffersCached;
    }

    public void setBuffersCached(String buffersCached)
    {
        this.BuffersCached.set(buffersCached);
    }

    public String getAvailable()
    {
        return Available.get();
    }

    public SimpleStringProperty availableProperty()
    {
        return Available;
    }

    public void setAvailable(String available)
    {
        this.Available.set(available);
    }
}
