package GUI.ListItems;

import javafx.beans.property.SimpleStringProperty;

public class ConnectedComputerEntry
{
    public SimpleStringProperty UsernameAndHost;

    public String getUsernameAndHost()
    {
        return UsernameAndHost.get();
    }

    public SimpleStringProperty usernameAndHostProperty()
    {
        return UsernameAndHost;
    }

    public void setUsernameAndHost(String usernameAndHost)
    {
        this.UsernameAndHost.set(usernameAndHost);
    }
}
