package GUI.Controllers;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.SSHConnectionManagement.SSHConnection;
import Healthcheck.SSHConnectionManagement.SSHConnectionException;
import Healthcheck.Utilities;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ComputerInfoController implements Initializable
{
    @FXML
    private CheckBox isSelectedCheckBox;

    @FXML
    private TextField displayedNameTextField;

    @FXML
    private TextField hostTextField;

    @FXML
    private TextField classroomTextField;

    @FXML
    private RadioButton passwordAuthMethodRadioButton;

    @FXML
    private RadioButton privateKeyAuthMethodRadioButton;

    @FXML
    private ChoiceBox<SshConfig> sshConfigChoiceBox;

    @FXML
    private TextField sshUsernameTextField;

    @FXML
    private PasswordField sshPasswordPasswordField;

    @FXML
    private GridPane sshKeyGridPane;

    @FXML
    private TextField sshKeyPathTextField;

    @FXML
    private Button chooseSshKeyButton;

    @FXML
    private TextField sshPortTextField;

    @FXML
    private TextField requestIntervalTextField;

    @FXML
    private TextField maintainPeriodTextField;

    @FXML
    private TextField logExpirationTextField;

    @FXML
    private GridPane preferencesGridPane;

    private ObservableList sshConfigObservableList = FXCollections.observableArrayList();

    // -----------------------------------------------------------------------------------------------------------------

    private final static int preferencesGridColsNum = 2;

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private Computer _computer;

    private List<CheckBox> preferenceCheckboxes = new ArrayList<>();
    private List<CheckBox> selectedCheckboxesBeforeChanges = new ArrayList<>();

    // Flags
    private boolean displayedNameIsIncorrect;
    private boolean hostIsIncorrect;
    private boolean localSshConfigPasswordDecryptionFailed;

    private SshConfig _localConfig;
    private SshFieldsState _prevLocalSshConfigFieldsFulfillment = new SshFieldsState();
    private ComputerInfoController _computerInfoController = this;
    private String decryptedPassword;

    private class SshConfigConverter extends StringConverter<SshConfig>
    {
        @Override
        public String toString(SshConfig sshConfig)
        {
            if(sshConfig.HasLocalScope())
            {
                return "LOCAL CONFIG";
            }
            else
            {
                return sshConfig.GetName() + " (" + sshConfig.GetUsername() + ")";
            }
        }

        @Override
        public SshConfig fromString(String string)
        {
            if(string.equals("LOCAL"))
            {
                return _localConfig;
            }
            else
            {
                String[] splitSshConfigString = string.split(" \\(");
                SshConfig sshConfig = _computersAndSshConfigsManager.GetGlobalSshConfigByName(splitSshConfigString[0]);

                return sshConfig;
            }
        }
    }

    private class SshFieldsState
    {
        public String Username;
        public Integer Port;
        public SshAuthMethod AuthMethod;
        public String Password;
        public String PrivateKeyPath;

        public SshFieldsState(){}

        private void Clear()
        {
            Username = null;
            Port = null;
            AuthMethod = null;
            Password = null;
            PrivateKeyPath = null;
        }

        public void FillWithCurrentSshTextFieldsValues()
        {
            Clear();

            Port = IsEmptyOrNull(sshPortTextField.getText())? null : Integer.parseInt(sshPortTextField.getText());
            Username = IsEmptyOrNull(sshUsernameTextField.getText())? null : sshUsernameTextField.getText();
            if(passwordAuthMethodRadioButton.isSelected())
            {
                AuthMethod = SshAuthMethod.PASSWORD;
                Password = IsEmptyOrNull(sshPasswordPasswordField.getText())? null : sshPasswordPasswordField.getText();
            }
            else if(privateKeyAuthMethodRadioButton.isSelected())
            {
                AuthMethod = SshAuthMethod.KEY;
                PrivateKeyPath = IsEmptyOrNull(sshKeyPathTextField.getText())? null : sshKeyPathTextField.getText();
            }
        }

        public void SetStateUsingSshConfig(SshConfig sshConfig) throws EncrypterException
        {
            Username = sshConfig.GetUsername();
            Port = sshConfig.GetPort();
            AuthMethod = sshConfig.GetAuthMethod();
            Password = sshConfig.GetEncryptedPassword() == null ?
                    null : Encrypter.GetInstance().Decrypt(sshConfig.GetEncryptedPassword());
            PrivateKeyPath = sshConfig.GetPrivateKeyPath();
        }

        public SshConfig ToSshConfig()
        {
            SshConfig sshConfig = new SshConfig(null, SshConfigScope.LOCAL, Port, AuthMethod, Username,
                    AuthMethod == SshAuthMethod.PASSWORD ? Password : PrivateKeyPath);

            return sshConfig;
        }
    }

    public ComputerInfoController(Computer computer, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _computer = computer;
    }

    // ---  INITIALIZATION & SETUP  ------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeChooseFileButton();
        InitializeAuthMethodRadioButtons();

        if(_computer.GetSshConfig().HasLocalScope())
        {
            _localConfig = _computer.GetSshConfig();
        }
        else
        {
            _localConfig = SshConfig.CreateEmpty();
        }

        InitializeGuiComponentsFromComputerContent();

        SetUpIntegerValidators();
        SetUpEmptinessValidators();
    }

    private void InitializeChooseFileButton()
    {
        chooseSshKeyButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                Stage stage = (Stage) sshUsernameTextField.getScene().getWindow();

                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(stage);
                sshKeyPathTextField.setText(file.getAbsolutePath());
            }
        });
    }

    private void InitializeAuthMethodRadioButtons()
    {
        passwordAuthMethodRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if(newValue == true)
                {
                    PrepareSshTextFieldsForPasswordAuthMethod();
                }
            }
        });

        privateKeyAuthMethodRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                if(newValue == true)
                {
                    PrepareSshTextFieldsForPrivateKeyAuthMethod();
                }
            }
        });
    }

    private void InitializeGuiComponentsFromComputerContent()
    {
        isSelectedCheckBox.setSelected(_computer.IsSelected());
        displayedNameTextField.setText(_computer.GetDisplayedName());
        hostTextField.setText(_computer.GetHost());
        classroomTextField.setText(_computer.GetClassroom());

        InitSshConfigChoiceBox();

        sshPortTextField.setText(String.valueOf(_computer.GetSshConfig().GetPort()));
        sshUsernameTextField.setText(_computer.GetSshConfig().GetUsername());

        if(_computer.GetSshConfig().HasGlobalScope())
        {
            SetGlobalConfigInChoiceBox(_computer.GetSshConfig());
        }
        else
        {
            if(_computer.GetSshConfig().HasPasswordAuth())
            {
                PrepareSshTextFieldsForPasswordAuthMethod();

                try
                {
                    decryptedPassword = Encrypter.GetInstance().Decrypt(_computer.GetSshConfig().GetEncryptedPassword());
                }
                catch (EncrypterException e)
                {
                    Utilities.ShowErrorDialog("Ssh password from local config could not be decrypted." +
                            "\nProvide new password.");
                    decryptedPassword = null;
                }

                sshPasswordPasswordField.setText(decryptedPassword);
                passwordAuthMethodRadioButton.setSelected(true);
            }
            else
            {
                PrepareSshTextFieldsForPrivateKeyAuthMethod();
                sshKeyPathTextField.setText(_computer.GetSshConfig().GetPrivateKeyPath());
                privateKeyAuthMethodRadioButton.setSelected(true);
            }
        }

        logExpirationTextField.setText(String.valueOf(_computer.GetLogExpiration().toSeconds()));
        maintainPeriodTextField.setText(String.valueOf(_computer.GetMaintainPeriod().toSeconds()));
        requestIntervalTextField.setText(String.valueOf(_computer.GetRequestInterval().toSeconds()));

        InitializeAndPopulatePreferencesCheckboxes();
        SetPreferencesCheckBoxesAsBeforeChanges();
    }

    private void SetUpIntegerValidators()
    {
        SetUpIntegerValidator(sshPortTextField);
        SetUpIntegerValidator(requestIntervalTextField);
        SetUpIntegerValidator(maintainPeriodTextField);
        SetUpIntegerValidator(logExpirationTextField);
    }

    private void SetUpIntegerValidator(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                ValidateIfInteger(textField)
        );
    }

    private void ValidateIfInteger(TextField textField)
    {
        try
        {
            Integer.parseInt(textField.getText());
            textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }
        catch (NumberFormatException e)
        {
            textField.getStyleClass().add("validation-error");
        }
    }

    private void SetUpEmptinessValidators()
    {
        SetUpEmptinessRuntimeValidator(displayedNameTextField);
        SetUpEmptinessRuntimeValidator(hostTextField);
        SetUpEmptinessRuntimeValidator(classroomTextField);
        SetUpEmptinessRuntimeValidator(sshUsernameTextField);
        SetUpEmptinessRuntimeValidator(sshPasswordPasswordField);
        SetUpEmptinessRuntimeValidator(sshKeyPathTextField);
    }

    private void SetUpEmptinessRuntimeValidator(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                ValidateIfEmpty(textField, oldValue, newValue));
    }

    private boolean IsEmptyOrNull(String str)
    {
        return str == null || str.trim().equals("");
    }

    private void ValidateIfEmpty(TextField textField, String oldValue, String newValue)
    {
        if(!IsEmptyOrNull(oldValue) && IsEmptyOrNull(newValue))
        {
            textField.getStyleClass().add("validation-error");
        }
        else if(IsEmptyOrNull(oldValue) && !IsEmptyOrNull(newValue))
        {
            textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }
    }

    private void ValidateIfEmpty(TextField textField)
    {
        textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        if(IsEmptyOrNull(textField.getText()))
        {
            textField.getStyleClass().add("validation-error");
        }
    }

    // ---  GUI INITIALIZATION  ----------------------------------------------------------------------------------------

    private void InitSshConfigChoiceBox()
    {
        SshConfig assignedSshConfig = _computer.GetSshConfig();
        int selectedSshConfigIndex = 0;

        if(assignedSshConfig.HasLocalScope())
        {
            sshConfigObservableList.add(assignedSshConfig);
        }
        else
        {
            sshConfigObservableList.add(_localConfig);
        }

        int i=1;
        for (SshConfig sshConfig : _computersAndSshConfigsManager.GetGlobalSshConfigs())
        {
            sshConfigObservableList.add(sshConfig);
            if(Utilities.AreEqual(sshConfig, _computer.GetSshConfig()))
            {
                selectedSshConfigIndex = i;
            }

            ++i;
        }
        sshConfigChoiceBox.setItems(sshConfigObservableList);
        sshConfigChoiceBox.setConverter(new SshConfigConverter());
        sshConfigChoiceBox.getSelectionModel().select(selectedSshConfigIndex);
        sshConfigChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                if(newValue.intValue() != 0)
                {
                    if(oldValue.intValue() == 0)
                    {
                        _prevLocalSshConfigFieldsFulfillment.FillWithCurrentSshTextFieldsValues();
                    }

                    SetGlobalConfigInChoiceBox(sshConfigChoiceBox.getItems().get(newValue.intValue()));
                }
                else if(newValue.intValue() == 0 && oldValue.intValue() != 0)
                {
                    SetLocalConfigInChoiceBox();
                }
            }
        });
    }

    private void InitializeAndPopulatePreferencesCheckboxes()
    {
        int gridRowsNum = (int) Math.ceil((double) Utilities.AvailablePreferences.size() / preferencesGridColsNum);

        for (int i = 0; i < Utilities.AvailablePreferences.size(); ++i)
        {
            CheckBox checkBox = new CheckBox();
            checkBox.setText(Utilities.ExtractPreferenceName(Utilities.AvailablePreferences.get(i).ClassName));

            if(_computer.HasPreferenceWithGivenClassName(Utilities.AvailablePreferences.get(i).ClassName))
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


    private void SetLocalConfigInChoiceBox()
    {
        sshConfigChoiceBox.getSelectionModel().select(_localConfig);

        sshPortTextField.setText(_prevLocalSshConfigFieldsFulfillment.Port == null ?
                "" : String.valueOf(_prevLocalSshConfigFieldsFulfillment.Port));
        sshUsernameTextField.setText(_prevLocalSshConfigFieldsFulfillment.Username == null ?
                "" : _prevLocalSshConfigFieldsFulfillment.Username);

        if(_prevLocalSshConfigFieldsFulfillment.AuthMethod == SshAuthMethod.PASSWORD)
        {
            passwordAuthMethodRadioButton.setSelected(true);
            sshPasswordPasswordField.setText(_prevLocalSshConfigFieldsFulfillment.Password == null ?
                    "" : _prevLocalSshConfigFieldsFulfillment.Password);
            sshKeyPathTextField.setText("");

            PrepareSshTextFieldsForPasswordAuthMethod();
        }
        else
        {
            privateKeyAuthMethodRadioButton.setSelected(true);
            sshKeyPathTextField.setText(_prevLocalSshConfigFieldsFulfillment.PrivateKeyPath == null ?
                    "" : _prevLocalSshConfigFieldsFulfillment.PrivateKeyPath);
            sshPasswordPasswordField.setText("");
            PrepareSshTextFieldsForPrivateKeyAuthMethod();
        }
    }

    private void SetGlobalConfigInChoiceBox(SshConfig sshConfig)
    {
        sshConfigChoiceBox.getSelectionModel().select(sshConfig);

        sshPortTextField.setText(String.valueOf(sshConfig.GetPort()));
        sshUsernameTextField.setText(sshConfig.GetUsername());
        if(sshConfig.HasPasswordAuth())
        {
            passwordAuthMethodRadioButton.setSelected(true);
            sshPasswordPasswordField.setText(sshConfig.GetEncryptedPassword());
        }
        else
        {
            privateKeyAuthMethodRadioButton.setSelected(true);
            sshKeyPathTextField.setText(sshConfig.GetPrivateKeyPath());
        }

        DisableAllSshFields();
    }

    private void DisableAllSshFields()
    {
        SetDisabledAuthMethodRadioButtons(true);

        sshUsernameTextField.setDisable(true);
        sshPortTextField.setDisable(true);
        sshPasswordPasswordField.setDisable(true);
        sshKeyGridPane.setDisable(true);

        sshUsernameTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshPortTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshPasswordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshKeyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
    }

    private void PrepareSshTextFieldsForPasswordAuthMethod()
    {
        SetDisabledAuthMethodRadioButtons(false);

        sshPasswordPasswordField.setDisable(false);
        sshUsernameTextField.setDisable(false);
        sshPortTextField.setDisable(false);

        sshKeyGridPane.setDisable(true);
        sshKeyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateRequiredFieldsIfEmpty(SshAuthMethod.PASSWORD);
    }

    private void PrepareSshTextFieldsForPrivateKeyAuthMethod()
    {
        SetDisabledAuthMethodRadioButtons(false);

        sshKeyGridPane.setDisable(false);
        sshUsernameTextField.setDisable(false);
        sshPortTextField.setDisable(false);

        sshPasswordPasswordField.setDisable(true);
        sshPasswordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateRequiredFieldsIfEmpty(SshAuthMethod.KEY);
    }

    private void ValidateRequiredFieldsIfEmpty(SshAuthMethod sshAuthMethod)
    {
        if(sshAuthMethod == SshAuthMethod.PASSWORD)
        {
            ValidateIfEmpty(sshUsernameTextField);
            ValidateIfEmpty(sshPortTextField);
            ValidateIfEmpty(sshPasswordPasswordField);
        }
        else
        {
            ValidateIfEmpty(sshUsernameTextField);
            ValidateIfEmpty(sshPortTextField);
            ValidateIfEmpty(sshKeyPathTextField);
        }
    }



    @FXML
    void SaveChanges(ActionEvent event)
    {
        List<String> errors = ValidateBeforeSave();

        if(errors.size() > 0)
        {
            Utilities.ShowSaveErrorDialog(errors);
            return;
        }

        try
        {
            boolean hadGlobalConfig = _computer.GetSshConfig().HasGlobalScope();
            CopyChangesToComputer();
            _computer.UpdateInDb();

            Utilities.ShowInfoDialog("Computer update has succeed.");

            if(_computer.GetSshConfig().HasLocalScope())
            {
                if(hadGlobalConfig)
                {
                    _localConfig = new SshConfig(_computer.GetSshConfig());
                    _localConfig.ConvertToNonExistingInDb();
                }
            }
            else if(_computer.GetSshConfig().HasGlobalScope() && hadGlobalConfig == false)
            {
                _localConfig = SshConfig.CreateEmpty();
            }

            _prevLocalSshConfigFieldsFulfillment = new SshFieldsState();
            selectedCheckboxesBeforeChanges.clear();
            selectedCheckboxesBeforeChanges = GetListOfSelectedPreferenceCheckboxes();

            /*
            TODO 1: Set decrypted password variable
             */
//            decryptedPassword = null; // TODO
        }
        catch (NothingToDoException e)
        {
            Utilities.ShowInfoDialog("No changes to save.");
        }
        catch(DatabaseException e)
        {
            RestoreComputerChanges();
            Utilities.ShowErrorDialog("Computer update in db has failed.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            RestoreComputerChanges();
            Utilities.ShowErrorDialog("Unknown error has occurred while saving.");
        }
    }

    private List<String> ValidateBeforeSave()
    {
        List<String> errors = new ArrayList<>();

        String displayedNameError = ValidateDisplayedName(displayedNameTextField.getText());
        if(displayedNameError != null)
        {
            errors.add(displayedNameError);
        }

        String hostError = ValidateHost(hostTextField.getText());
        if(hostError != null)
        {
            errors.add(hostError);
        }

        if(IsEmptyOrNull(classroomTextField.getText()))
        {
            errors.add("Classroom cannot be empty.");
        }

        if(IsSelectedLocalConfig())
        {
            if(IsEmptyOrNull(sshUsernameTextField.getText()))
            {
                errors.add("Ssh username cannot be empty.");
            }

            if(IsParsableToInteger(sshPortTextField.getText()) == false)
            {
                errors.add("Ssh port must be integer.");
            }

            if(IsSelectedPasswordAuthRadioButton() && IsEmptyOrNull(sshPasswordPasswordField.getText()))
            {
                errors.add("Ssh password cannot be empty.");
            }
            else if(IsSelectedPrivateKeyAuthRadioButton() && IsEmptyOrNull(sshKeyPathTextField.getText()))
            {
                errors.add("Ssh private key path cannot be empty.");
            }
        }

        if(IsParsableToInteger(requestIntervalTextField.getText()) == false)
        {
            errors.add("Request interval must be integer.");
        }

        if(IsParsableToInteger(maintainPeriodTextField.getText()) == false)
        {
            errors.add("Maintenance period must be integer.");
        }

        if(IsParsableToInteger(logExpirationTextField.getText()) == false)
        {
            errors.add("Log expiration value must be integer.");
        }

        return errors;
    }

    private String ValidateDisplayedName(String newDisplayedName)
    {
        if(IsEmptyOrNull(newDisplayedName))
        {
            if(displayedNameIsIncorrect == false)
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                displayedNameIsIncorrect = true;
            }

            return "Displayed name cannot be empty.";
        }

        if(_computer.GetDisplayedName().equals(newDisplayedName) == false
                && _computersAndSshConfigsManager.ComputerWithDisplayedNameExists(newDisplayedName))
        {
            if(displayedNameIsIncorrect == false)
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                displayedNameIsIncorrect = true;
            }

            return "Other computer has same displayed name.";
        }

        if(displayedNameIsIncorrect)
        {
            displayedNameTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }

        return null;
    }

    private String ValidateHost(String newHost)
    {
        if(IsEmptyOrNull(newHost))
        {
            if(hostIsIncorrect == false)
            {
                hostTextField.getStyleClass().add("validation-error");
                hostIsIncorrect = true;
            }

            return "Host cannot be empty.";
        }

        if(_computer.GetHost().equals(newHost) == false
                && _computersAndSshConfigsManager.ComputerWithHostExists(newHost))
        {
            if(hostIsIncorrect == false)
            {
                hostTextField.getStyleClass().add("validation-error");
                hostIsIncorrect= true;
            }

            return "Other computer has same hostTextField.";
        }

        if(hostIsIncorrect)
        {
            hostTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }

        return null;
    }


    private void CopyChangesToComputer()
    {
        if(IsSelectedGlobalConfig())
        {
            if(_computer.GetSshConfig().HasLocalScope()) // LOCAL -> GLOBAL
            {
                _computer.SetSshConfig(sshConfigChoiceBox.getSelectionModel().getSelectedItem());
            }
            else // GLOBAL -> GLOBAL
            {
                _computer.SetSshConfig(sshConfigChoiceBox.getSelectionModel().getSelectedItem());
            }
        }
        else
        {
            if((_computer.GetSshConfig().HasLocalScope() && LocalSshConfigChanged())) // LOCAL -> LOCAL
            {
                SshFieldsState currentSshFieldsState = new SshFieldsState();
                currentSshFieldsState.FillWithCurrentSshTextFieldsValues();

                _computer.SetSshConfig(currentSshFieldsState.ToSshConfig());
            }
            else if(_computer.GetSshConfig().HasGlobalScope()) // GLOBAL -> LOCAL
            {
                SshFieldsState currentSshFieldsState = new SshFieldsState();
                currentSshFieldsState.FillWithCurrentSshTextFieldsValues();
                _computer.SetSshConfig(currentSshFieldsState.ToSshConfig());
            }
        }

        _computer.SetDisplayedName(displayedNameTextField.getText());
        _computer.SetHost(hostTextField.getText());
        _computer.SetClassroom(classroomTextField.getText());

        Duration requestIntervalDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(requestIntervalTextField.getText()));
        _computer.SetRequestInterval(requestIntervalDuration);

        Duration maintainPeriodDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(maintainPeriodTextField.getText()));
        _computer.SetMaintainPeriod(maintainPeriodDuration);

        Duration logExpirationDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(logExpirationTextField.getText()));
        _computer.SetLogExpiration(logExpirationDuration);

        _computer.SetPreferences(GetListOfSelectedPreferences());
    }


    @FXML
    void DiscardChanges(ActionEvent event) throws EncrypterException
    {
        boolean response = Utilities.ShowYesNoDialog("Discard changes?", "Do you want to discard changes?");
        if(response == false)
        {
            return;
        }

        isSelectedCheckBox.setSelected(_computer.IsSelected());
        displayedNameTextField.setText(_computer.GetDisplayedName());
        hostTextField.setText(_computer.GetHost());
        classroomTextField.setText(_computer.GetClassroom());

        sshConfigChoiceBox.getSelectionModel().select(_computer.GetSshConfig());
        sshUsernameTextField.setText(_computer.GetSshConfig().GetUsername());
        sshPortTextField.setText(String.valueOf(_computer.GetSshConfig().GetPort()));
        if(_computer.GetSshConfig().HasLocalScope())
        {
            sshPasswordPasswordField.setText(decryptedPassword);
            if(_computer.GetSshConfig().HasPasswordAuth() && decryptedPassword == null)
            {
                Utilities.ShowErrorDialog("Ssh password from local config could not be decrypted." +
                        "\nProvide new password.");
            }
        }
        else
        {
            _prevLocalSshConfigFieldsFulfillment = new SshFieldsState();
            sshPasswordPasswordField.setText(_computer.GetSshConfig().GetEncryptedPassword());
        }
        sshKeyPathTextField.setText(_computer.GetSshConfig().GetPrivateKeyPath());

        if(_computer.GetSshConfig().HasPasswordAuth()) // Also performs validation
        {
            passwordAuthMethodRadioButton.setSelected(true);
        }
        else
        {
            privateKeyAuthMethodRadioButton.setSelected(true);
        }

        requestIntervalTextField.setText(String.valueOf(_computer.GetRequestInterval().toSeconds()));
        maintainPeriodTextField.setText(String.valueOf(_computer.GetMaintainPeriod().toSeconds()));
        logExpirationTextField.setText(String.valueOf(_computer.GetLogExpiration().toSeconds()));

        SetPreferencesCheckBoxesAsBeforeChanges();
    }



    @FXML
    void TestConnection(ActionEvent event)
    {
        try
        {
            Validate_TestConnection();
        }
        catch(SSHConnectionException e)
        {
            return;
        }

        SshConfig sshConfigToTest;
        if(IsSelectedGlobalConfig())
        {
            sshConfigToTest = sshConfigChoiceBox.getSelectionModel().getSelectedItem();
        }
        else
        {
            SshFieldsState currentSshFieldsState = new SshFieldsState();
            currentSshFieldsState.FillWithCurrentSshTextFieldsValues();
            sshConfigToTest = currentSshFieldsState.ToSshConfig();

            if(IsSelectedPasswordAuthRadioButton())
            {
                try
                {
                    String encryptedPassword = Encrypter.GetInstance().Encrypt(currentSshFieldsState.Password);
                    sshConfigToTest.SetPasswordAuthMethod(encryptedPassword);
                }
                catch (EncrypterException e)
                {
                    Utilities.ShowErrorDialog("Connection with computer cannot be established." +
                            "\nPassword cannot be encrypted.");
                    return;
                }
            }
        }

        String currentHost = hostTextField.getText();
        SSHConnection sshConnection = new SSHConnection();
        try
        {
            sshConnection.OpenConnection(
                    currentHost,
                    sshConfigToTest
            );

            Utilities.ShowInfoDialog("Connection with computer can be established.");
        }
        catch(EncrypterException e)
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established." +
                    "\nUnable to decrypt ssh password.");
        }
        catch(Exception e)
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.");
        }
    }

    private void Validate_TestConnection() throws SSHConnectionException
    {
        if(IsEmptyOrNull(hostTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nHost cannot be empty.");
            throw new SSHConnectionException("Host cannot be empty");
        }

        if(IsSelectedGlobalConfig())
        {
            return; // No more validation needed
        }

        if(IsEmptyOrNull(sshUsernameTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nUsername cannot be empty.");
            throw new SSHConnectionException("Username cannot be empty");
        }

        if(IsParsableToInteger(sshPortTextField.getText()) == false)
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nPort must be integer.");
            throw new SSHConnectionException("Port must be integer.");
        }

        if(IsSelectedPasswordAuthRadioButton() && IsEmptyOrNull(sshPasswordPasswordField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nPassword cannot be empty.");
            throw new SSHConnectionException("Password cannot be empty.");
        }

        if(IsSelectedPrivateKeyAuthRadioButton() && IsEmptyOrNull(sshKeyPathTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nKey path cannot be empty.");
            throw new SSHConnectionException("Key path cannot be empty.");
        }
    }

    private void SetDisabledAuthMethodRadioButtons(boolean value)
    {
        for (Toggle toggleButton : privateKeyAuthMethodRadioButton.getToggleGroup().getToggles())
        {
            Node node = (Node) toggleButton;
            node.setDisable(value);
        }
    }

    // ---  PREDICATES  ------------------------------------------------------------------------------------------------

    private boolean IsSelectedLocalConfig()
    {
        return sshConfigChoiceBox.getSelectionModel().getSelectedItem().HasLocalScope();
    }

    private boolean IsSelectedGlobalConfig()
    {
        return sshConfigChoiceBox.getSelectionModel().getSelectedItem().HasGlobalScope();
    }

    private boolean IsSelectedPasswordAuthRadioButton()
    {
        return passwordAuthMethodRadioButton.isSelected();
    }

    private boolean IsSelectedPrivateKeyAuthRadioButton()
    {
        return privateKeyAuthMethodRadioButton.isSelected();
    }

    private boolean IsParsableToInteger(String str)
    {
        try
        {
            Integer.parseInt(str);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }

    private boolean LocalSshConfigChanged()
    {
        SshFieldsState currentSshFieldsState = new SshFieldsState();
        currentSshFieldsState.FillWithCurrentSshTextFieldsValues();

        return  !Utilities.AreEqual(_computer.GetSshConfig().GetUsername(), currentSshFieldsState.Username) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetPort(), currentSshFieldsState.Port) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetAuthMethod(), currentSshFieldsState.AuthMethod) ||
                !Utilities.AreEqual(decryptedPassword, currentSshFieldsState.Password) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetPrivateKeyPath(), currentSshFieldsState.PrivateKeyPath);
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

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

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    private void RestoreComputerChanges()
    {
        _computer.Restore();
    }
}
