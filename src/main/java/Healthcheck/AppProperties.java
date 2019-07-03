package Healthcheck;

import java.io.FileInputStream;
import java.util.Properties;

public class AppProperties
{
    private static String _appPropertiesFileName = "app.properties";
    private static final AppProperties _appProperties = new AppProperties();

    public final Properties Properties;

    private AppProperties()
    {
        Properties = new Properties();
        try
        {
            String path = "./" + _appPropertiesFileName;
            FileInputStream fileInputStream = new FileInputStream(path);
            Properties.load(fileInputStream);
            fileInputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static AppProperties GetInstance()
    {
        return _appProperties;
    }
}
