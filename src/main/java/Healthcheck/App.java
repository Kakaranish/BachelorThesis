package Healthcheck;

import GUI.Controllers.ComputerInfoController;
import GUI.Controllers.TestController;
import Healthcheck.Entities.Preference;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    public void Foo()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Test.fxml"));

            TestController testController = new TestController();
            fxmlLoader.setController(testController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setOnCloseRequest(testController::OnCloseAction);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Test.fxml"));

            TestController testController = new TestController();
            fxmlLoader.setController(testController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/computer-info.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setOnCloseRequest(testController::OnCloseAction);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle("Healthcheck application");

            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
