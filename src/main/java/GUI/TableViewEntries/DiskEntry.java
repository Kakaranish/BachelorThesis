package GUI.TableViewEntries;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class DiskEntry extends LogEntry
{
    public SimpleStringProperty FileSystem;
    public SimpleLongProperty BlocksNumber;
    public SimpleLongProperty Used;
    public SimpleLongProperty Available;
    public SimpleIntegerProperty UsePercentage;
    public SimpleStringProperty MountedOn;

    public String getFileSystem()
    {
        return FileSystem.get();
    }

    public SimpleStringProperty fileSystemProperty()
    {
        return FileSystem;
    }

    public void setFileSystem(String fileSystem)
    {
        this.FileSystem.set(fileSystem);
    }

    public long getBlocksNumber()
    {
        return BlocksNumber.get();
    }

    public SimpleLongProperty blocksNumberProperty()
    {
        return BlocksNumber;
    }

    public void setBlocksNumber(long blocksNumber)
    {
        this.BlocksNumber.set(blocksNumber);
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

    public long getAvailable()
    {
        return Available.get();
    }

    public SimpleLongProperty availableProperty()
    {
        return Available;
    }

    public void setAvailable(long available)
    {
        this.Available.set(available);
    }

    public int getUsePercentage()
    {
        return UsePercentage.get();
    }

    public SimpleIntegerProperty usePercentageProperty()
    {
        return UsePercentage;
    }

    public void setUsePercentage(int usePercentage)
    {
        this.UsePercentage.set(usePercentage);
    }

    public String getMountedOn()
    {
        return MountedOn.get();
    }

    public SimpleStringProperty mountedOnProperty()
    {
        return MountedOn;
    }

    public void setMountedOn(String mountedOn)
    {
        this.MountedOn.set(mountedOn);
    }
}
