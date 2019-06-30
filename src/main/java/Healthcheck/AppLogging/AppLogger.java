package Healthcheck.AppLogging;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppLogger
{
    private static SimpleDateFormat _formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static AppLogger _appLogger = new AppLogger();
    private final static int _maxLogsNum = 100;

    private ObservableList<AppLoggerEntry> _targetObservableList;
    private TableView<AppLoggerEntry> _targetTableView;
    private boolean _enabledOutputToConsole;

    private AppLogger()
    {
    }

    public static void SetTargetObservableList(ObservableList<AppLoggerEntry> targetObservableList)
    {
        _appLogger._targetObservableList = targetObservableList;
    }

    public static void SetTargetTableView(TableView<AppLoggerEntry> targetTableView)
    {
        _appLogger._targetTableView= targetTableView;
    }

    public static void SetEnabledOutputToConsole(boolean enabled)
    {
        _appLogger._enabledOutputToConsole = enabled;
    }

    public static void Log(LogType logType, String content)
    {
        Date now = new Date();

        if(_appLogger._targetObservableList != null)
        {
            if(_appLogger._targetObservableList.size() == _maxLogsNum)
            {
                _appLogger._targetObservableList.remove(_maxLogsNum-1);
            }

            _appLogger._targetObservableList.add(new AppLoggerEntry()
            {{
                DateTime = new SimpleStringProperty(_formatter.format(now));
                LogType = new SimpleStringProperty(logType.name());
                Content = new SimpleStringProperty(content);
            }});

            if(_appLogger._targetTableView != null)
            {
                _appLogger._targetTableView.refresh();
            }
        }

        if(_appLogger._enabledOutputToConsole)
        {
            System.out.println(_formatter.format(now) + "\t[" + logType.name() + "] " + content);
        }
    }

    public static void Log(LogType logType, String moduleName, String content)
    {
        Date now = new Date();

        if(_appLogger._targetObservableList != null)
        {
            if(_appLogger._targetObservableList.size() == _maxLogsNum)
            {
                _appLogger._targetObservableList.remove(0);
            }

            _appLogger._targetObservableList.add(new AppLoggerEntry()
            {{
                DateTime = new SimpleStringProperty(_formatter.format(now));
                LogType = new SimpleStringProperty(logType.name());
                Content = new SimpleStringProperty(moduleName + ": " + content);
            }});

            if(_appLogger._targetTableView != null)
            {
                _appLogger._targetTableView.refresh();
            }
        }

        if(_appLogger._enabledOutputToConsole)
        {
            System.out.println(_formatter.format(now) + "\t[" + logType.name() + "] " + moduleName + ": " + content);
        }
    }
}
