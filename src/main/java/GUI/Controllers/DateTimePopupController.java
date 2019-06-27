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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DateTimePopupController implements Initializable
{
    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField timeTextField;

    @FXML
    private Button submitButton;

    private Computer _computer;
    private static final Pattern TimePattern = Pattern.compile("[0-9]{2}:[0-9]{2}");
    private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DateTimePopupController(Computer computer)
    {
        _computer = computer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitDatePicker();
        SetUpTimeValidator();
        InitSubmitButton();
    }

    private void InitDatePicker()
    {
        datePicker.setValue(LocalDate.now());
        datePicker.setEditable(false);
    }

    private void SetUpTimeValidator()
    {
        timeTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {

            if(TimePattern.matcher(newValue).matches())
            {
                timeTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
            }
            else
            {
                timeTextField.getStyleClass().add("validation-error");
            }
        });
    }

    private void InitSubmitButton()
    {
        submitButton.setOnAction(event ->
        {
            if(TimePattern.matcher(timeTextField.getText()).matches() == false)
            {
                Utilities.ShowErrorDialog("Provided time has wrong format.");
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
                    new StatsForComputerController(_computer, ConvertDateAndTimeToTimestamp());

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

    private Timestamp ConvertDateAndTimeToTimestamp()
    {
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyy hh:mm");
            String date = DateFormatter.format(datePicker.getValue());
            String time = timeTextField.getText();
            String dateAndTime = date + " " + time;

            Date parsedDate = dateFormat.parse(dateAndTime);
            return new Timestamp(parsedDate.getTime());
        }
        catch(Exception e)
        {
            // This block is never entered
        }
        return null;
    }

}
