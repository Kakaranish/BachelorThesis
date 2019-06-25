package GUI.TableViewEntries;

import javafx.beans.property.SimpleStringProperty;

public abstract class LogEntry
{
    public SimpleStringProperty Datetime;

    public String getDatetime()
    {
        return Datetime.get();
    }

    public SimpleStringProperty datetimeProperty()
    {
        return Datetime;
    }

    public void setDatetime(String datetime)
    {
        this.Datetime.set(datetime);
    }
}
