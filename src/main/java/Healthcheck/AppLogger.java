package Healthcheck;

import javafx.collections.ObservableList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppLogger
{
    private static SimpleDateFormat _formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static AppLogger _appLogger = new AppLogger();
    private final static int _maxLogsNum = 100;

    private ObservableList<AppLogEntry> _targetObservableList;

    private AppLogger()
    {
    }

    public static void SetTargetObservableList(ObservableList<AppLogEntry> targetObservableList)
    {
        _appLogger._targetObservableList = targetObservableList;
    }

    public static void Log(String message)
    {
        if(_appLogger._targetObservableList == null)
        {
            System.out.println("[ERROR] AppLogger: Target ObservableList is not set.");
            return;
        }

        if(_appLogger._targetObservableList.size() == _maxLogsNum)
        {
            _appLogger._targetObservableList.remove(_maxLogsNum-1);
        }

        _appLogger._targetObservableList.add(new AppLogEntry()
        {{
            DateTime = _formatter.format(new Date());
            Message = message;
        }});
    }
}
