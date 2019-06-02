package Healthcheck;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application
{
    public static void main(String[] args)
    {
//        System.out.println(Utilities.ExtractPreferenceName("Healthcheck.Preferences.CpuInfoPreference"));
        System.out.println(Utilities.GetClassNameForPreferenceName("Ram Info"));

        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        Parent root = null;

        try
        {
            root = FXMLLoader.load(getClass().getResource("/fxml/Test.fxml"));
        }
        catch (IOException e)
        {
            e.printStackTrace(System.out);
        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Hello World Application");
        primaryStage.show();
    }
}
