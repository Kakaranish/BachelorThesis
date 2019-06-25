package Healthcheck.AppLogging;

import javafx.beans.property.SimpleStringProperty;

public class AppLoggerEntry
{
    public SimpleStringProperty DateTime;
    public SimpleStringProperty LogType;
    public SimpleStringProperty Content;

    public String getDateTime()
    {
        return DateTime.get();
    }

    public SimpleStringProperty dateTimeProperty()
    {
        return DateTime;
    }

    public void setDateTime(String dateTime)
    {
        this.DateTime.set(dateTime);
    }

    public String getLogType()
    {
        return LogType.get();
    }

    public SimpleStringProperty logTypeProperty()
    {
        return LogType;
    }

    public void setLogType(String logType)
    {
        this.LogType.set(logType);
    }

    public String getContent()
    {
        return Content.get();
    }

    public SimpleStringProperty contentProperty()
    {
        return Content;
    }

    public void setContent(String content)
    {
        this.Content.set(content);
    }
}
