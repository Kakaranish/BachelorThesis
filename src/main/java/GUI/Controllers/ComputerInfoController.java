package GUI.Controllers;

import Healthcheck.Computer;
import Healthcheck.ComputerManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Preference;
import Healthcheck.Entities.User;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.SSHConnectionManagement.SSHConnection;
import Healthcheck.SSHConnectionManagement.SSHConnectionException;
import Healthcheck.UsersManager;
import Healthcheck.Utilities;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ComputerInfoController implements Initializable
{
    @FXML
    private TextField displayedNameTextField;

    @FXML
    private TextField hostTextField;

    @FXML
    private TextField classroomTextField;

    @FXML
    private ChoiceBox<User> assignedUserChoiceBox;

    @FXML
    private TextField sshUsernameTextField;

    @FXML
    private PasswordField sshPasswordPasswordField;

    @FXML
    private TextField sshKeyTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField requestIntervalTextField;

    @FXML
    private TextField maintainPeriodTextField;

    @FXML
    private TextField logExpirationTextField;

    @FXML
    private GridPane preferencesGridPane;

    @FXML
    private CheckBox isSelectedCheckBox;

    private ObservableList assignedUserObservableList = FXCollections.observableArrayList();

    // -----------------------------------------------------------------------------------------------------------------

    private final static int preferencesGridColsNum = 2;

    private UsersManager _usersManager;
    private ComputerManager _computerManager;
    private ComputerEntity _computerEntity;
    private Computer _computer;

    private int indexOfSelectedUserBeforeChanges;
    private List<CheckBox> preferenceCheckboxes = new ArrayList<>();
    private List<CheckBox> selectedCheckboxesBeforeChanges = new ArrayList<>();

    // Flags
    private AtomicBoolean portIsIncorrect = new AtomicBoolean(false);
    private AtomicBoolean requestIntervalIsIncorrect = new AtomicBoolean(false);
    private AtomicBoolean maintainPeriodIsIncorrect = new AtomicBoolean(false);
    private AtomicBoolean logExpirationIsIncorrect = new AtomicBoolean(false);
    private boolean displayedNameIsIncorrect;
    private boolean hostIsIncorrect;

    private User emptyUser = new User(null, null, null, null);
    private boolean userFieldsDisabled;

    public ComputerInfoController(Computer computer, ComputerManager computerManager, UsersManager usersManager)
    {
        _computerManager = computerManager;
        _usersManager = usersManager;
        _computer = computer;
        _computerEntity = _computer.ComputerEntity;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  ACTIONS ASSOCIATED WITH BUTTONS  ---------------------------------------------------------------------------

    @FXML
    void SaveChanges(ActionEvent event)
    {
        String newDisplayedName = displayedNameTextField.getText();
        String newHost = hostTextField.getText();

        List<String> errors = ValidateBeforeSave(newDisplayedName, newHost);

        if(errors.size() > 0)
        {
            Utilities.ShowSaveErrorDialog(errors);
        }
        else
        {
            ComputerEntity computerEntityAfterChanges = GetChangedComputerEntity();
            try
            {
                _computerManager.UpdateComputer(_computer, computerEntityAfterChanges);
            }
            catch(NothingToDoException e)
            {
                Utilities.ShowInfoDialog("Nothing has changed.");
                return;
            }
            catch(DatabaseException e)
            {
                Utilities.ShowErrorDialog("Saving computer has failed because of database connection");
            }

            Utilities.ShowInfoDialog("Saving computer has succeed.");

            indexOfSelectedUserBeforeChanges = assignedUserChoiceBox.getSelectionModel().getSelectedIndex();
            selectedCheckboxesBeforeChanges.clear();
            selectedCheckboxesBeforeChanges = GetListOfSelectedPreferenceCheckboxes();
        }
    }

    @FXML
    void DiscardChanges(ActionEvent event)
    {
        boolean response = Utilities.ShowYesNoDialog("Discard changes?", "Do you want to discard changes?");
        if(response == true)
        {
            isSelectedCheckBox.setSelected(_computerEntity.IsSelected);
            displayedNameTextField.setText(_computerEntity.DisplayedName);
            hostTextField.setText(_computerEntity.Host);
            classroomTextField.setText(_computerEntity.Classroom.Name);

            SetAssignedUser(indexOfSelectedUserBeforeChanges);

            if(_computerEntity.HasSetUser() == false)
            {
                String decryptedPassword = null;
                try
                {
                    decryptedPassword = Encrypter.GetInstance().Decrypt(_computerEntity.GetEncryptedPassword());
                }
                catch (EncrypterException e)
                {
                    // This exception will be never caught
                }

                sshUsernameTextField.setText(_computerEntity.GetUsername());
                sshPasswordPasswordField.setText(decryptedPassword);
                sshKeyTextField.setText(_computerEntity.GetSSHKey());
            }
            else
            {
                sshUsernameTextField.setDisable(true);
                sshPasswordPasswordField.setDisable(true);
                sshKeyTextField.setDisable(true);
            }

            logExpirationTextField.setText(String.valueOf(_computerEntity.LogExpiration.toSeconds()));
            maintainPeriodTextField.setText(String.valueOf(_computerEntity.MaintainPeriod.toSeconds()));
            portTextField.setText(String.valueOf(_computerEntity.Port));
            requestIntervalTextField.setText(String.valueOf(_computerEntity.RequestInterval.toSeconds()));

            SetPreferencesCheckBoxesAsBeforeChanges();

            ResetFlags();
        }
    }

    @FXML
    void TestConnection(ActionEvent event)
    {
        if(portIsIncorrect.get())
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nPort must be integer.");
            return;
        }

        int newPort = Integer.parseInt(portTextField.getText());
        String newHost = hostTextField.getText();
        String newSSHUsername;
        String decryptedNewSSHPassowrd = null;

        if(assignedUserChoiceBox.getSelectionModel().getSelectedIndex() == 0)
        {
            newSSHUsername = sshUsernameTextField.getText();
            decryptedNewSSHPassowrd = sshPasswordPasswordField.getText();
        }
        else
        {
            User newAssignedUser = assignedUserChoiceBox.getSelectionModel().getSelectedItem();
            newSSHUsername = newAssignedUser.SSH_Username;
            try
            {
                decryptedNewSSHPassowrd = Encrypter.GetInstance().Decrypt(newAssignedUser.SSH_EncryptedPassword);
            }
            catch (EncrypterException e)
            {
                // TODO: Solve this situation
            }
        }
        SSHConnection sshConnection = new SSHConnection();
        try
        {
            sshConnection.OpenConnection(
                    newHost,
                    newSSHUsername,
                    decryptedNewSSHPassowrd,
                    newPort,
                    Utilities.SSHTimeout
            );

            Utilities.ShowInfoDialog("Connection with computer can be established.");
        }
        catch (SSHConnectionException e)
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitGUIComponentsFromComputerEntityContent();
        SetUpIntegerValidators();
        SetUpEmptinessValidators();
    }

    private void InitGUIComponentsFromComputerEntityContent()
    {
        isSelectedCheckBox.setSelected(_computerEntity.IsSelected);
        displayedNameTextField.setText(_computerEntity.DisplayedName);
        hostTextField.setText(_computerEntity.Host);
        classroomTextField.setText(_computerEntity.Classroom.Name);

        PopulateAssignedUserChoiceBox();
        SetAssignedUser(indexOfSelectedUserBeforeChanges);

        if(_computerEntity.HasSetUser() == false)
        {
            String decryptedPassword = null;
            try
            {
                decryptedPassword = Encrypter.GetInstance().Decrypt(_computerEntity.GetEncryptedPassword());
            }
            catch (EncrypterException e)
            {
                // This situation will never happen because
                // it's checked before running computer settings window if password can be decrypted
            }

            sshUsernameTextField.setText(_computerEntity.GetUsername());
            sshPasswordPasswordField.setText(decryptedPassword);
            sshKeyTextField.setText(_computerEntity.GetSSHKey());
        }
        else
        {
            sshUsernameTextField.setDisable(true);
            sshPasswordPasswordField.setDisable(true);
            sshKeyTextField.setDisable(true);
        }

        logExpirationTextField.setText(String.valueOf(_computerEntity.LogExpiration.toSeconds()));
        maintainPeriodTextField.setText(String.valueOf(_computerEntity.MaintainPeriod.toSeconds()));
        portTextField.setText(String.valueOf(_computerEntity.Port));
        requestIntervalTextField.setText(String.valueOf(_computerEntity.RequestInterval.toSeconds()));

        PopulatePreferencesCheckboxes();
        SetPreferencesCheckBoxesAsBeforeChanges();
    }

    private ComputerEntity GetChangedComputerEntity()
    {
        Duration maintenancePeriodDuration = Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(maintainPeriodTextField.getText()));
        Duration requestIntervalDuration = Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(requestIntervalTextField.getText()));
        Duration logExpirationDuration = Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(logExpirationTextField.getText()));

        String encryptedPassword = null;
        try
        {
            encryptedPassword = Encrypter.GetInstance().Encrypt(sshPasswordPasswordField.getText());
        }
        catch (EncrypterException e)
        {
            // TODO: Handle case when encryption fails
            e.printStackTrace();
        }

        ComputerEntity changedComputerEntity;
        if(assignedUserChoiceBox.getSelectionModel().getSelectedIndex() == 0)
        {
            changedComputerEntity = new ComputerEntity(
                    hostTextField.getText(),
                    displayedNameTextField.getText(),
                    sshUsernameTextField.getText(),
                    encryptedPassword,
                    sshKeyTextField.getText(),
                    Integer.parseInt(portTextField.getText()),
                    maintenancePeriodDuration,
                    requestIntervalDuration,
                    logExpirationDuration,
                    Utilities.GetClassroom(classroomTextField.getText()),
                    isSelectedCheckBox.isSelected()
            );
        }
        else
        {
            User user = assignedUserChoiceBox.getSelectionModel().getSelectedItem();
            changedComputerEntity = new ComputerEntity(
                    hostTextField.getText(),
                    displayedNameTextField.getText(),
                    user,
                    Integer.parseInt(portTextField.getText()),
                    maintenancePeriodDuration,
                    requestIntervalDuration,
                    logExpirationDuration,
                    Utilities.GetClassroom(classroomTextField.getText()),
                    isSelectedCheckBox.isSelected()
            );
        }

        changedComputerEntity.CopyId(_computerEntity);
        changedComputerEntity.CopyLastMaintenance(_computerEntity);
        changedComputerEntity.Preferences = GetListOfSelectedPreferences();

        return changedComputerEntity;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  ASSIGNED USER CHOICEBOX  -----------------------------------------------------------------------------------

    class UserConverter extends StringConverter<User>
    {
        @Override
        public String toString(User user)
        {
            if(user.equals(emptyUser))
            {
                return "";
            }
            else
            {
                return user.DisplayedUsername + " - " + user.SSH_Username;
            }
        }

        @Override
        public User fromString(String string)
        {
            if(string.equals(""))
            {
                return emptyUser;
            }

            String[] splitUserString = string.split(" - ");
            User user = _usersManager.GetUserByDisplayedUsernameAndSSHUsername(
                    splitUserString[0],
                    splitUserString[1]
            );

            return user;
        }
    }

    private void PopulateAssignedUserChoiceBox()
    {
        User currAssignedUser = _computerEntity.User;
        assignedUserObservableList.add(emptyUser);
        int selectedUserIndex = 0;

        int i=1;
        for (User user : _usersManager.GetUsers())
        {
            assignedUserObservableList.add(user);
            if(Utilities.AreEqual(currAssignedUser, user))
            {
                selectedUserIndex = i;
            }

            ++i;
        }

        indexOfSelectedUserBeforeChanges = selectedUserIndex;

        assignedUserChoiceBox.setItems(assignedUserObservableList);
        assignedUserChoiceBox.setConverter(new UserConverter());
        assignedUserChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
            {
                if(newValue.intValue() != 0 && userFieldsDisabled == false)
                {
                    DisableSSHFields();
                }
                else if(newValue.intValue() == 0 && userFieldsDisabled == true)
                {
                    EnableSSHFields();
                }
            }
        });
    }

    private void SetAssignedUser(int selectedUserIndex)
    {
        userFieldsDisabled = selectedUserIndex == 0 ? false : true;
        assignedUserChoiceBox.getSelectionModel().select(selectedUserIndex);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  PREFERENCES CHECKBOXES  ------------------------------------------------------------------------------------

    private void PopulatePreferencesCheckboxes()
    {
        int gridRowsNum = (int) Math.ceil((double) Utilities.AvailablePreferences.size() / preferencesGridColsNum);

        for (int i = 0; i < Utilities.AvailablePreferences.size(); ++i)
        {
            CheckBox checkBox = new CheckBox();
            checkBox.setText(Utilities.ExtractPreferenceName(Utilities.AvailablePreferences.get(i).ClassName));

            if(_computerEntity.HasPreferenceWithGivenClassName(Utilities.AvailablePreferences.get(i).ClassName))
            {
                selectedCheckboxesBeforeChanges.add(checkBox);
            }

            preferenceCheckboxes.add(checkBox);
            preferencesGridPane.add(checkBox, i / gridRowsNum, i % gridRowsNum);
        }
    }

    private void SetPreferencesCheckBoxesAsBeforeChanges()
    {
        for (CheckBox preferenceCheckbox : preferenceCheckboxes)
        {
            preferenceCheckbox.setSelected(false);
        }

        for (CheckBox selectedCheckBox : selectedCheckboxesBeforeChanges)
        {
            selectedCheckBox.setSelected(true);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  VALIDATION IF VALUE OF TEXTFIELDS IS INTEGER  --------------------------------------------------------------

    private void SetUpIntegerValidators()
    {
        SetUpIntegerValidator(portTextField, portIsIncorrect);
        SetUpIntegerValidator(requestIntervalTextField, requestIntervalIsIncorrect);
        SetUpIntegerValidator(maintainPeriodTextField, maintainPeriodIsIncorrect);
        SetUpIntegerValidator(logExpirationTextField, logExpirationIsIncorrect);
    }

    private void SetUpIntegerValidator(TextField textField, AtomicBoolean flag)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                ValidateIfInteger(textField, flag)
        );
    }

    private void ValidateIfInteger(TextField textField, AtomicBoolean flag)
    {
        try
        {
            Integer.parseInt(textField.getText());

            if(flag.get() == true)
            {
                textField.getStyleClass().removeAll("validation-error");
                flag.set(false);
            }
        }
        catch (NumberFormatException e)
        {
            if(flag.get() == false)
            {
                textField.getStyleClass().add("validation-error");
                flag.set(true);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  VALIDATION IF VALUE OF TEXTFIELDS IS NOT EMPTY  ------------------------------------------------------------

    private void SetUpEmptinessValidators()
    {
        SetUpEmptinessRuntimeValidator(displayedNameTextField);
        SetUpEmptinessRuntimeValidator(hostTextField);
        SetUpEmptinessRuntimeValidator(classroomTextField);
        SetUpEmptinessRuntimeValidator(sshUsernameTextField);
        SetUpEmptinessRuntimeValidator(sshPasswordPasswordField);
        SetUpEmptinessRuntimeValidator(sshKeyTextField);
    }

    private void SetUpEmptinessRuntimeValidator(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                ValidateIfIsEmpty(textField, oldValue, newValue));
    }

    private void ValidateIfIsEmpty(TextField textField, String oldValue, String newValue)
    {
        if(!oldValue.trim().equals("") && newValue.trim().equals(""))
        {
            textField.getStyleClass().add("validation-error");
        }
        else if(oldValue.trim().equals("") && !newValue.trim().equals(""))
        {
            textField.getStyleClass().remove("validation-error");
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // ---  VALIDATION WHEN SAVE BUTTON IS CLICKED  --------------------------------------------------------------------

    private List<String> ValidateBeforeSave(String newDisplayedName, String newHost)
    {
        List<String> errors = new ArrayList<>();

        String displayedNameError = ValidateDisplayedName(newDisplayedName);
        if(displayedNameError != null)
        {
            errors.add(displayedNameError);
        }

        String hostError = ValidateHost(newHost);
        if(hostError != null)
        {
            errors.add(hostError);
        }

        if(portIsIncorrect.get())
        {
            errors.add("Port must be integer.");
        }

        if(requestIntervalIsIncorrect.get())
        {
            errors.add("Request interval must be integer.");
        }

        if(maintainPeriodIsIncorrect.get())
        {
            errors.add("Maintenance period must be integer.");
        }

        if(logExpirationIsIncorrect.get())
        {
            errors.add("Log expiration value must be integer.");
        }

        return errors;
    }

    private String ValidateDisplayedName(String newDisplayedName)
    {
        if(newDisplayedName.trim().equals(""))
        {
            if(displayedNameIsIncorrect == false)
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                displayedNameIsIncorrect = true;
            }

            return "Displayed name cannot be empty.";
        }

        boolean otherComputerHasSameDisplayedName =
                _computerManager.ComputerWithGivenDisplayedNameExists(newDisplayedName);

        if(otherComputerHasSameDisplayedName)
        {
            if(_computerEntity.DisplayedName.equals(newDisplayedName))
            {
                if(displayedNameIsIncorrect)
                {
                    displayedNameTextField.getStyleClass().remove("validation-error");
                    displayedNameIsIncorrect = false;
                }
            }
            else
            {
                if(displayedNameIsIncorrect == false)
                {
                    displayedNameTextField.getStyleClass().add("validation-error");
                    displayedNameIsIncorrect = true;

                    return "Other computer has same displayed name.";
                }
            }
        }
        else
        {
            if(displayedNameIsIncorrect)
            {
                displayedNameTextField.getStyleClass().remove("validation-error");
            }
        }

        return null;
    }

    private String ValidateHost(String newHost)
    {
        if(newHost.trim().equals(""))
        {
            if(hostIsIncorrect == false)
            {
                hostTextField.getStyleClass().add("validation-error");
                hostIsIncorrect = true;
            }

            return "Host cannot be empty.";
        }

        boolean otherComputerHasSameHost = _computerManager.ComputerWithGivenHostExists(newHost);

        if(otherComputerHasSameHost)
        {
            if(_computerEntity.Host.equals(newHost))
            {
                if(hostIsIncorrect)
                {
                    hostTextField.getStyleClass().remove("validation-error");
                    hostIsIncorrect = false;
                }
            }
            else
            {
                if(hostIsIncorrect == false)
                {
                    hostTextField.getStyleClass().add("validation-error");
                    hostIsIncorrect= true;

                    return "Other computer has same hostTextField.";
                }
            }
        }
        else
        {
            if(hostIsIncorrect)
            {
                hostTextField.getStyleClass().remove("validation-error");
            }
        }

        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  MISC  -----------------------------------------------------------------------------------------------------

    private void ResetFlags()
    {
        portIsIncorrect = new AtomicBoolean(false);
        requestIntervalIsIncorrect = new AtomicBoolean(false);
        maintainPeriodIsIncorrect = new AtomicBoolean(false);
        logExpirationIsIncorrect = new AtomicBoolean(false);
        displayedNameIsIncorrect = false;
        hostIsIncorrect = false;
    }

    private void EnableSSHFields()
    {
        userFieldsDisabled = false;
        sshUsernameTextField.setDisable(false);
        sshPasswordPasswordField.setDisable(false);
        sshKeyTextField.setDisable(false);

        if(sshUsernameTextField.getText().trim().equals(""))
        {
            sshUsernameTextField.getStyleClass().add("validation-error");
        }

        if(sshPasswordPasswordField.getText().trim().equals(""))
        {
            sshPasswordPasswordField.getStyleClass().add("validation-error");
        }

        if(sshKeyTextField.getText().trim().equals(""))
        {
            sshKeyTextField.getStyleClass().add("validation-error");
        }
    }

    private void DisableSSHFields()
    {
        userFieldsDisabled = true;
        sshUsernameTextField.setDisable(true);
        sshPasswordPasswordField.setDisable(true);
        sshKeyTextField.setDisable(true);

        sshUsernameTextField.getStyleClass().removeAll("validation-error");
        sshPasswordPasswordField.getStyleClass().removeAll("validation-error");
        sshKeyTextField.getStyleClass().removeAll("validation-error");
    }

    private List<Preference> GetListOfSelectedPreferences()
    {
        List<Preference> preferences = new ArrayList<>();
        List<CheckBox> selectedCheckBoxes = GetListOfSelectedPreferenceCheckboxes();

        for (CheckBox selectedCheckBox : selectedCheckBoxes)
        {
            String preferenceClassName = Utilities.GetClassNameForPreferenceName(selectedCheckBox.getText());
            Preference preference = Utilities.GetPreferenceFromClassName(preferenceClassName);
            preferences.add(preference);
        }

        return preferences;
    }

    private List<CheckBox> GetListOfSelectedPreferenceCheckboxes()
    {
        List<CheckBox> selectedCheckBoxes = preferenceCheckboxes.stream()
                .filter(cb -> cb.isSelected() == true).collect(Collectors.toList());

        return selectedCheckBoxes;
    }
}
