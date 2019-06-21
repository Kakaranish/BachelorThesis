package GUI.Controllers;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;

enum ChangedEventType
{
    ADDED,

    UPDATED,

    REMOVED
}

public class ChangedEvent
{
    public ChangedEventType ChangeType;

    public Computer Computer;

    public SshConfig SshConfig;
}
