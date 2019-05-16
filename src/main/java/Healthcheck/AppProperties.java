package Healthcheck;

import java.io.FileInputStream;
import java.net.URL;
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
            URL path = AppProperties.class.getClassLoader().getResource(_appPropertiesFileName);
            Properties.load(new FileInputStream(path.getPath()));
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
