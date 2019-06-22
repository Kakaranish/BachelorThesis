package Healthcheck;

import Healthcheck.Entities.Preference;
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
//        Preference preference = Utilities.GetPreferenceFromClassName("Healthcheck.Preferences.UsersInfoPreference");
////        System.out.println(Utilities.ExtractPreferenceName("Healthcheck.Preferences.CpuInfoPreference"));
//        System.out.println(Utilities.GetClassNameForPreferenceName("Ram Info"));

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
        primaryStage.setResizable(false);
        primaryStage.getScene().getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

        primaryStage.setTitle("Hello World Application");
        primaryStage.show();
    }
}
