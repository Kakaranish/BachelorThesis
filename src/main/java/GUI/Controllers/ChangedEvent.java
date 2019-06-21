package GUI.Controllers;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;

enum ChangeEventType
{
    ADDED,

    UPDATED,

    REMOVED
}

public class ChangedEvent
{
    public ChangeEventType ChangeType;

    public Computer Computer;

    public SshConfig SshConfig;
}
