package Healthcheck;

import GUI.Controllers.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class Main extends javafx.application.Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        StartMainWindow();
    }

    private void StartMainWindow()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));

            MainWindowController mainWindowController = new MainWindowController();
            fxmlLoader.setController(mainWindowController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setOnCloseRequest(mainWindowController::OnCloseAction);
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
