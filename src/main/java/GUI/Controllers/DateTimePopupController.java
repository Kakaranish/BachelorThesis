package GUI.Controllers;

import Healthcheck.Entities.Computer;
import Healthcheck.Utilities;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DateTimePopupController implements Initializable
{
    @FXML
    private DatePicker dateFromPicker;

    @FXML
    private DatePicker dateToPicker;

    @FXML
    private TextField timeFromTextField;

    @FXML
    private TextField timeToTextField;

    @FXML
    private Button submitButton;

    private Computer _computer;

    public DateTimePopupController(Computer computer)
    {
        _computer = computer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeDatePickers();
        SetUpTimeValidators();
        InitializeSubmitButton();
    }

    private void InitializeDatePickers()
    {
        dateFromPicker.setValue(LocalDate.now());
        dateFromPicker.setEditable(false);

        dateToPicker.setValue(LocalDate.now());
        dateToPicker.setEditable(false);
    }

    private void SetUpTimeValidators()
    {
        Utilities.SetUpTimeValidator(timeFromTextField);
        Utilities.SetUpTimeValidator(timeToTextField);
    }

    private void InitializeSubmitButton()
    {
        submitButton.setOnAction(event ->
        {
            if(Utilities.TimeMatchesToTimePattern(timeFromTextField.getText(), Utilities.TimePattern) == false ||
                    Utilities.TimeMatchesToTimePattern(timeToTextField.getText(), Utilities.TimePattern) == false)
            {
                Utilities.ShowErrorDialog("Provided time(s) has/have wrong format.");
                return;
            }

            GoToStatsController();
        });
    }

    private void GoToStatsController()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StatsForComputer.fxml"));

            StatsForComputerController statsForComputerController =
                    new StatsForComputerController(_computer,
                            Utilities.ConvertDateAndTimeToTimestamp(
                                    Utilities.DateFormat, dateFromPicker.getValue(), timeFromTextField.getText()),
                            Utilities.ConvertDateAndTimeToTimestamp(
                                    Utilities.DateFormat, dateToPicker.getValue(), timeToTextField.getText()));

            fxmlLoader.setController(statsForComputerController);

            final Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle(_computer.GetDisplayedName() + " - charts");

            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
