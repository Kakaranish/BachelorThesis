package GUI.Controllers;

import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.ListItems.ComputerListCell;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.LogsMaintainer;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class AddOrUpdateComputerController implements Initializable
{
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

    @FXML
    private Button saveOrUpdateButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button gatheringIntervalHelperButton;

    @FXML
    private Button maintenancePeriodHelperButton;

    private ObservableList<SshConfig> sshConfigObservableList = FXCollections.observableArrayList();

    // -----------------------------------------------------------------------------------------------------------------

    private final static String ModuleName = "AddOrUpdateComputerController";
    private final static int preferencesGridColsNum = 2;

    private MainWindowController _parentController;
    private ComputerListCell _cellCaller;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private Computer _computer;

    private List<CheckBox> preferenceCheckboxes = new ArrayList<>();
    private List<CheckBox> selectedCheckboxesBeforeChanges = new ArrayList<>();

    private boolean _isDiscardingChanges = false;

    private SshConfig _localLocalConfig;
    private SshFieldsState _prevLocalSshFieldsState = new SshFieldsState();
    private AddOrUpdateComputerController _addOrUpdate_computerController = this;

    private class SshConfigConverter extends StringConverter<SshConfig>
    {
        @Override
        public String toString(SshConfig sshConfig)
        {
            if(sshConfig.HasLocalScope())
            {
                return "LOCAL";
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
                return sshConfigObservableList.get(0);
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

        public SshFieldsState()
        {
            AuthMethod = SshAuthMethod.PASSWORD;
        }

        private void Clear()
        {
            Username = null;
            Port = null;
            AuthMethod = null;
            Password = null;
            PrivateKeyPath = null;
        }

        public void FillSshTextFields()
        {
            sshPortTextField.setText(Port == null ? null : String.valueOf(Port));
            sshUsernameTextField.setText(Username == null ? null : Username);

            if(AuthMethod == SshAuthMethod.PASSWORD)
            {
                sshPasswordPasswordField.setText(Password == null ? null: Password);
                sshKeyPathTextField.setText(null);
            }
            else
            {
                sshKeyPathTextField.setText(PrivateKeyPath == null ? null : PrivateKeyPath);
                sshPasswordPasswordField.setText(null);
            }
        }

        public void FetchFromSshTextFields()
        {
            Clear();

            if(Utilities.EmptyOrNull(sshPortTextField.getText()))
            {
                Port = null;
            }
            else
            {
                try
                {
                    Port = Integer.parseInt(sshPortTextField.getText());
                }
                catch (NumberFormatException e)
                {
                    Port = null;
                }
            }
            Username = Utilities.EmptyOrNull(sshUsernameTextField.getText())? null : sshUsernameTextField.getText();
            AuthMethod = passwordAuthMethodRadioButton.isSelected() ? SshAuthMethod.PASSWORD : SshAuthMethod.KEY;
            Password = Utilities.EmptyOrNull(sshPasswordPasswordField.getText())? null : sshPasswordPasswordField.getText();
            PrivateKeyPath = Utilities.EmptyOrNull(sshKeyPathTextField.getText())? null : sshKeyPathTextField.getText();
        }

        public void FetchFromSshConfig(SshConfig sshConfig)
        {
            Username = sshConfig.GetUsername();
            Port = sshConfig.GetPort();
            AuthMethod = sshConfig.GetAuthMethod();
            try
            {
                Password = sshConfig.GetEncryptedPassword() == null ?
                        null : Encrypter.GetInstance().Decrypt(sshConfig.GetEncryptedPassword());
            }
            catch (EncrypterException e)
            {
                Password = null;
            }
            PrivateKeyPath = sshConfig.GetPrivateKeyPath();
        }

        public SshConfig ToSshConfig()
        {
            SshConfig sshConfig = new SshConfig(null, SshConfigScope.LOCAL, Port, AuthMethod, Username,
                    AuthMethod == SshAuthMethod.PASSWORD ? Password : PrivateKeyPath);

            return sshConfig;
        }
    }

    public AddOrUpdateComputerController(MainWindowController parentController, Computer computer,
                                         ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _parentController = parentController;
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _computer = computer;
    }

    public AddOrUpdateComputerController(ComputerListCell cellCaller, Computer computer,
                                         ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _cellCaller = cellCaller;
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _computer = computer;
    }

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        if(IsInEditMode())
        {
            if(_computer.GetSshConfig().HasLocalScope())
            {
                _localLocalConfig = _computer.GetSshConfig();
            }
            else
            {
                _localLocalConfig = SshConfig.CreateEmpty();
            }
        }
        else
        {
            _localLocalConfig = SshConfig.CreateEmpty();
        }

        SetUpIntegerValidators();
        SetUpEmptinessValidators();

        InitializeAndFillGuiComponents();

        ValidateStringTextFieldsIfEmpty();
        ValidateIntegerTextFieldsIfParsable();
    }

    private void InitializeAndFillGuiComponents()
    {
        InitializeChooseFileButton();
        InitializeAuthMethodRadioButtons();
        InitializeSshConfigChoiceBox();
        InitializeHelperButtons();

        if(IsInEditMode())
        {
            saveOrUpdateButton.setText("Update");
            removeButton.setDisable(false);

            displayedNameTextField.setText(_computer.GetDisplayedName());
            hostTextField.setText(_computer.GetHost());
            classroomTextField.setText(_computer.GetClassroom());

            logExpirationTextField.setText(String.valueOf(_computer.GetLogExpiration().toSeconds()));
            maintainPeriodTextField.setText(String.valueOf(_computer.GetMaintainPeriod().toSeconds()));
            requestIntervalTextField.setText(String.valueOf(_computer.GetRequestInterval().toSeconds()));
        }
        else
        {
            if(Utilities.UseDefaultValues)
            {
                sshPortTextField.setText(String.valueOf(Utilities.DefaultPort));
                requestIntervalTextField.setText(String.valueOf(Utilities.DefaultRequestInterval));
                maintainPeriodTextField.setText(String.valueOf(Utilities.DefaultMaintainPeriod));
                logExpirationTextField.setText(String.valueOf(Utilities.DefaultLogExpiration));
            }

            saveOrUpdateButton.setText("Add");
            removeButton.setDisable(true);
        }

        if(StartedInSaveMode())
        {
            selectedCheckboxesBeforeChanges = preferenceCheckboxes;
        }

        if(StartedInSaveMode() == false && _cellCaller.GetController().IsRemovingAllowed() == false)
        {
            removeButton.setDisable(true);
        }

        InitializePreferencesCheckboxes();
        DiscardChangesInPreferenceCheckboxes();
    }

    private void InitializeHelperButtons()
    {
        ImageView gatheringIntervalHelperImageView = new ImageView(MainWindowController.questionIcon);
        gatheringIntervalHelperImageView.setFitHeight(16);
        gatheringIntervalHelperImageView.setFitWidth(16);
        gatheringIntervalHelperImageView.setSmooth(true);
        gatheringIntervalHelperButton.setGraphic(gatheringIntervalHelperImageView);
        gatheringIntervalHelperButton.getStyleClass().add("interactive-menu-button");
        Tooltip gatheringIntervalTooltip = new Tooltip("Select time interval every which logs will be gathered.");
        gatheringIntervalTooltip.setShowDelay(new javafx.util.Duration(0));
        gatheringIntervalHelperButton.setTooltip(gatheringIntervalTooltip);

        ImageView maintenancePeriodHelperImageView = new ImageView(MainWindowController.questionIcon);
        maintenancePeriodHelperImageView.setFitHeight(16);
        maintenancePeriodHelperImageView.setFitWidth(16);
        maintenancePeriodHelperImageView.setSmooth(true);
        maintenancePeriodHelperButton.setGraphic(maintenancePeriodHelperImageView);
        maintenancePeriodHelperButton.getStyleClass().add("interactive-menu-button");
        Tooltip maintenancePeriodTooltip = new Tooltip("Select time interval every which expired logs will be removed.");
        maintenancePeriodTooltip.setShowDelay(new javafx.util.Duration(0));
        maintenancePeriodHelperButton.setTooltip(maintenancePeriodTooltip);
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
                if(file != null)
                {
                    sshKeyPathTextField.setText(file.getAbsolutePath());
                }
            }
        });
    }

    private void InitializeAuthMethodRadioButtons()
    {
        passwordAuthMethodRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean wasSet, Boolean isSet)
            {
                if(isSet)
                {
                    ChoosePasswordAuthMethod();
                }
            }
        });

        privateKeyAuthMethodRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean wasSet, Boolean isSet)
            {
                if(isSet)
                {
                    ChoosePrivateKeyAuthMethod();
                }
            }
        });
    }

    private void InitializeSshConfigChoiceBox()
    {
        sshConfigObservableList.add(_localLocalConfig);

        SshConfig assignedSshConfig;
        if(IsInEditMode())
        {
            assignedSshConfig = _computer.GetSshConfig();
        }
        else
        {
            assignedSshConfig = _localLocalConfig;
        }

        for (SshConfig sshConfig : _computersAndSshConfigsManager.GetGlobalSshConfigs())
        {
            sshConfigObservableList.add(sshConfig);
            if(IsInEditMode() && Utilities.AreEqual(sshConfig, _computer.GetSshConfig()))
            {
                assignedSshConfig = sshConfig;
            }
        }

        sshConfigChoiceBox.setItems(sshConfigObservableList);
        sshConfigChoiceBox.setConverter(new SshConfigConverter());
        sshConfigChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                if(newValue.intValue() < 0)
                {
                    newValue = 0;
                }

                if(newValue.intValue() != 0)
                {
                    if(oldValue.intValue() == 0 && _isDiscardingChanges == false) // Backup when LOCAL -> GLOBAL
                    {
                        if(_prevLocalSshFieldsState == null)
                        {
                            _prevLocalSshFieldsState = new SshFieldsState();
                        }

                        _prevLocalSshFieldsState.FetchFromSshTextFields();
                    }

                    ChooseGlobalConfigInChoiceBox(sshConfigChoiceBox.getItems().get(newValue.intValue()));
                }
                else if(oldValue.intValue() != 0 && newValue.intValue() == 0) // GLOBAL -> LOCAL
                {
                    ChooseLocalConfigInChoiceBox();
                }
            }
        });

        if(assignedSshConfig.HasLocalScope())
        {
            _prevLocalSshFieldsState.FetchFromSshConfig(assignedSshConfig);
        }
        sshConfigChoiceBox.getSelectionModel().select(assignedSshConfig);
    }

    private void InitializePreferencesCheckboxes()
    {
        int gridRowsNum = (int) Math.ceil((double) Utilities.AvailablePreferences.size() / preferencesGridColsNum);

        for (int i = 0; i < Utilities.AvailablePreferences.size(); ++i)
        {
            CheckBox checkBox = new CheckBox();
            checkBox.setText(Utilities.ExtractPreferenceName(Utilities.AvailablePreferences.get(i).ClassName));

            if(IsInEditMode() && _computer.HasPreferenceWithGivenClassName(Utilities.AvailablePreferences.get(i).ClassName))
            {
                selectedCheckboxesBeforeChanges.add(checkBox);
            }

            preferenceCheckboxes.add(checkBox);
            preferencesGridPane.add(checkBox, i / gridRowsNum, i % gridRowsNum);
        }
    }

    // ---  RUNTIME VALIDATION  ----------------------------------------------------------------------------------------

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
        if(IsParsableToInteger(textField.getText()))
        {
            textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }
        else
        {
            textField.getStyleClass().add("validation-error");
        }
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

    private void SetUpEmptinessValidators()
    {
        SetUpEmptinessValidator(displayedNameTextField);
        SetUpEmptinessValidator(hostTextField);
        SetUpEmptinessValidator(classroomTextField);
        SetUpEmptinessValidator(sshUsernameTextField);
        SetUpEmptinessValidator(sshPasswordPasswordField);
        SetUpEmptinessValidator(sshKeyPathTextField);
    }

    private void SetUpEmptinessValidator(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                ValidateIfEmpty(textField, oldValue, newValue));
    }

    private void ValidateIfEmpty(TextField textField, String oldValue, String newValue)
    {
        textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        if(Utilities.EmptyOrNull(newValue))
        {
            textField.getStyleClass().add("validation-error");
        }
    }

    // ---  OTHER VALIDATION  ------------------------------------------------------------------------------------------

    private void ValidateStringTextFieldsIfEmpty()
    {
        ValidateIfEmpty(displayedNameTextField);
        ValidateIfEmpty(hostTextField);
        ValidateIfEmpty(classroomTextField);
        ValidateIfEmpty(logExpirationTextField);
        ValidateIfEmpty(maintainPeriodTextField);
        ValidateIfEmpty(requestIntervalTextField);
    }

    private void ValidateIntegerTextFieldsIfParsable()
    {
        ValidateIfInteger(sshPortTextField);
        ValidateIfInteger(requestIntervalTextField);
        ValidateIfInteger(maintainPeriodTextField);
        ValidateIfInteger(logExpirationTextField);
    }

    private void ValidateIfRequiredSshFieldsCorrect(SshAuthMethod sshAuthMethod)
    {
        if(sshAuthMethod == SshAuthMethod.PASSWORD)
        {
            ValidateIfEmpty(sshUsernameTextField);
            ValidateIfEmpty(sshPortTextField);
            ValidateIfInteger(sshPortTextField);
            ValidateIfEmpty(sshPasswordPasswordField);
        }
        else
        {
            ValidateIfEmpty(sshUsernameTextField);
            ValidateIfEmpty(sshPortTextField);
            ValidateIfInteger(sshPortTextField);
            ValidateIfEmpty(sshKeyPathTextField);
        }
    }

    private void ValidateIfEmpty(TextField textField)
    {
        if(Utilities.EmptyOrNull(textField.getText()))
        {
            textField.getStyleClass().add("validation-error");
        }
        else
        {
            textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        }
    }

    private void ValidateIfConnectionCanBeTested() throws SSHConnectionException
    {
        if(Utilities.EmptyOrNull(hostTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nHost cannot be empty.");
            throw new SSHConnectionException("Host cannot be empty");
        }

        if(IsSelectedGlobalConfig())
        {
            return; // No more validation needed
        }

        if(Utilities.EmptyOrNull(sshUsernameTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nUsername cannot be empty.");
            throw new SSHConnectionException("Username cannot be empty");
        }

        if(IsParsableToInteger(sshPortTextField.getText()) == false)
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nPort must be integer.");
            throw new SSHConnectionException("Port must be integer.");
        }

        if(IsSelectedPasswordAuthRadioButton() && Utilities.EmptyOrNull(sshPasswordPasswordField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nPassword cannot be empty.");
            throw new SSHConnectionException("Password cannot be empty.");
        }

        if(IsSelectedPrivateKeyAuthRadioButton() && Utilities.EmptyOrNull(sshKeyPathTextField.getText()))
        {
            Utilities.ShowErrorDialog("Connection with computer cannot be established.\nKey path cannot be empty.");
            throw new SSHConnectionException("Key path cannot be empty.");
        }
    }

    // ---  BEFORE SAVE VALIDATION  ------------------------------------------------------------------------------------

    private List<String> GetValidationErrorListBeforeSaveOrUpdate()
    {
        List<String> errors = new ArrayList<>();

        if(Utilities.EmptyOrNull(displayedNameTextField.getText()))
        {
            errors.add("Displayed name cannot be empty.");
        }

        if(Utilities.EmptyOrNull(hostTextField.getText()))
        {
            errors.add("Host cannot be empty.");
        }

        if(Utilities.EmptyOrNull(classroomTextField.getText()))
        {
            errors.add("Classroom cannot be empty.");
        }

        if(IsSelectedLocalConfig())
        {
            if(Utilities.EmptyOrNull(sshUsernameTextField.getText()))
            {
                errors.add("Ssh username cannot be empty.");
            }

            if(IsParsableToInteger(sshPortTextField.getText()) == false)
            {
                errors.add("Ssh port must be integer.");
            }

            if(IsSelectedPasswordAuthRadioButton() && Utilities.EmptyOrNull(sshPasswordPasswordField.getText()))
            {
                errors.add("Ssh password cannot be empty.");
            }
            else if(IsSelectedPrivateKeyAuthRadioButton() && Utilities.EmptyOrNull(sshKeyPathTextField.getText()))
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

    private String ValidateDisplayedNameExistsInDb(String newDisplayedName)
    {
        if(IsInEditMode())
        {
            if(_computer.GetDisplayedName().equals(newDisplayedName) == false
                    && _computersAndSshConfigsManager.ComputerWithDisplayedNameExists(newDisplayedName))
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                return "Other computer has same displayed name.";
            }
        }
        else
        {
            if(_computersAndSshConfigsManager.ComputerWithDisplayedNameExists(newDisplayedName))
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                return "Other computer has same displayed name.";
            }
        }

        return null;
    }

    private String ValidateHostExistsInDb(String newHost)
    {
        if(IsInEditMode())
        {
            if(_computer.GetHost().equals(newHost) == false
                    && _computersAndSshConfigsManager.ComputerWithHostExists(newHost))
            {
                hostTextField.getStyleClass().add("validation-error");
                return "Other computer has same host.";
            }
        }
        else
        {
            if(_computersAndSshConfigsManager.ComputerWithHostExists(newHost))
            {
                hostTextField.getStyleClass().add("validation-error");
                return "Other computer has same host.";
            }
        }

        return null;
    }

    // ---  COPYING CHANGES TO COMPUTER  -------------------------------------------------------------------------------

    private void CopyChangesToComputerInSaveMode(boolean encryptPassword)
    {
        // This method is always called after validation

        SshConfig sshConfig;
        if(IsSelectedLocalConfig()) // EMPTY LOCAL -> LOCAL
        {
            SshFieldsState currentSshFieldsState = new SshFieldsState();
            currentSshFieldsState.FetchFromSshTextFields();

            if(currentSshFieldsState.AuthMethod == SshAuthMethod.PASSWORD)
            {
                if(encryptPassword)
                {
                    try
                    {
                        currentSshFieldsState.Password = Encrypter.GetInstance().Encrypt(currentSshFieldsState.Password);
                    }
                    catch (EncrypterException e)
                    {
                        // Never enters this block
                    }
                }
            }

            sshConfig = currentSshFieldsState.ToSshConfig();
        }
        else // EMPTY LOCAL -> GLOBAL
        {
            sshConfig = sshConfigChoiceBox.getSelectionModel().getSelectedItem();
        }

        Duration requestIntervalDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(requestIntervalTextField.getText()));
        Duration maintainPeriodDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(maintainPeriodTextField.getText()));
        Duration logExpirationDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(logExpirationTextField.getText()));

        _computer = new Computer(
                displayedNameTextField.getText(),
                hostTextField.getText(),
                classroomTextField.getText(),
                sshConfig,
                requestIntervalDuration,
                maintainPeriodDuration,
                logExpirationDuration,
                true
        );

        _computer.SetPreferences(GetSelectedPreferences());
        _computer.SetComputersAndSshConfigsManager(_computersAndSshConfigsManager);
    }

    private void CopyChangesToComputerInEditMode(boolean encryptPassword)
    {
        if(IsSelectedGlobalConfig()) // LOCAL -> GLOBAL || GLOBAL -> GLOBAL
        {
            _computer.SetSshConfig(sshConfigChoiceBox.getSelectionModel().getSelectedItem());
        }
        else
        {
            if((_computer.GetSshConfig().HasLocalScope() && LocalSshConfigChanged()) // LOCAL -> LOCAL
                    || _computer.GetSshConfig().HasGlobalScope()) // GLOBAL -> LOCAL
            {
                SshFieldsState currentSshFieldsState = new SshFieldsState();
                currentSshFieldsState.FetchFromSshTextFields();

                if(currentSshFieldsState.AuthMethod == SshAuthMethod.PASSWORD)
                {
                    if(encryptPassword)
                    {
                        try
                        {
                            currentSshFieldsState.Password = Encrypter.GetInstance().Encrypt(currentSshFieldsState.Password);
                        }
                        catch (EncrypterException e)
                        {
                            // Never enters this block
                        }
                    }
                }

                _computer.SetSshConfig(currentSshFieldsState.ToSshConfig());
            }
        }

        _computer.SetSelected(_computer.IsSelected());
        _computer.SetDisplayedName(displayedNameTextField.getText());
        _computer.SetHost(hostTextField.getText());
        _computer.SetClassroom(classroomTextField.getText());

        Duration requestIntervalDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(requestIntervalTextField.getText()));
        Duration maintainPeriodDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(maintainPeriodTextField.getText()));
        Duration logExpirationDuration =
                Utilities.ConvertSecondsToDurationInNanos(Long.parseLong(logExpirationTextField.getText()));
        _computer.SetRequestInterval(requestIntervalDuration);
        _computer.SetMaintainPeriod(maintainPeriodDuration);
        _computer.SetLogExpiration(logExpirationDuration);

        _computer.SetPreferences(GetSelectedPreferences());
    }


    // ---  BUTTONS LOGIC  ---------------------------------------------------------------------------------------------

    @FXML
    void SaveOrUpdateComputer(ActionEvent event)
    {
        List<String> emptinessOrIntegerErrors = GetValidationErrorListBeforeSaveOrUpdate();
        if (emptinessOrIntegerErrors.size() > 0)
        {
            Utilities.ShowSaveErrorDialog(emptinessOrIntegerErrors);
            return;
        }

        String displayedNameError = ValidateDisplayedNameExistsInDb(displayedNameTextField.getText());
        String hostError = ValidateHostExistsInDb(hostTextField.getText());
        List<String> existenceErrors = new ArrayList<>(){{
            if(displayedNameError != null)
            {
                add(displayedNameError);
            }
            if(hostError != null)
            {
                add(hostError);
            }
        }};

        if(existenceErrors.size() > 0)
        {
            Utilities.ShowSaveErrorDialog(existenceErrors);
            return;
        }

        if(IsInSaveMode())
        {
            boolean saveSucceed = SaveComputer();
            if(saveSucceed)
            {
                saveOrUpdateButton.setText("Update");
                removeButton.setDisable(false);
                _parentController.RefreshStatsChoiceBox();
            }
        }
        else
        {
            boolean updateSucceed = UpdateComputer();
            if(updateSucceed)
            {
                ChangeEvent changeEvent = new ChangeEvent();
                changeEvent.ChangeType = ChangeEventType.UPDATED;
                changeEvent.Computer = _computer;

                if(StartedInSaveMode() == false)
                {
                    _cellCaller.NotifyChanged(changeEvent);

                    if(_cellCaller.GetController().IsRemovingAllowed() == false)
                    {
                        _cellCaller.GetController().RestartMaintainingLogs();
                    }
                }
                else
                {
                    _parentController.RefreshStatsChoiceBox();
                }
            }
        }
    }

    private boolean SaveComputer()
    {
        CopyChangesToComputerInSaveMode(true);

        SshConfig backupConfig = _localLocalConfig;

        if(_computer.GetSshConfig().HasGlobalScope()) // EMPTY LOCAL -> GLOBAL
        {
            _localLocalConfig = SshConfig.CreateEmpty();
            sshConfigObservableList.set(0, _localLocalConfig);
        }

        try
        {
            _computer.AddToDb();

            if(_computer.GetSshConfig().HasLocalScope()) // EMPTY LOCAL -> LOCAL
            {
                _localLocalConfig = _computer.GetSshConfig();
                sshConfigObservableList.set(0, _localLocalConfig);
                sshConfigChoiceBox.getSelectionModel().select(0);
            }

            selectedCheckboxesBeforeChanges.clear();
            selectedCheckboxesBeforeChanges = GetSelectedPreferenceCheckboxes();
            _prevLocalSshFieldsState = null;

            Utilities.ShowInfoDialog("Adding computer has succeed.");

            return true;
        }
        catch(DatabaseException e)
        {
            _computer = null;
            Utilities.ShowErrorDialog("Adding computer to db has failed.");

            return false;
        }
        catch(Exception e)
        {
            _computer = null;

            AppLogger.Log(LogType.FATAL_ERROR , ModuleName, "Unknown error has occurred while saving computer.");
            Utilities.ShowErrorDialog("Unknown error has occurred while saving computer.");

            return false;
        }
    }

    private boolean UpdateComputer()
    {
        boolean hadGlobalConfig = _computer.GetSshConfig().HasGlobalScope();

        CopyChangesToComputerInEditMode(true);

        if(_computer.Changed() == false)
        {
            Utilities.ShowInfoDialog("Nothing to update.");
            return false;
        }

        try
        {
            if(_computer.GetSshConfig().HasGlobalScope() && hadGlobalConfig == false) // LOCAL -> GLOBAL
            {
                _localLocalConfig = SshConfig.CreateEmpty();
                sshConfigObservableList.set(0, _localLocalConfig);
            }

            _computer.UpdateInDb();

            if(_computer.GetSshConfig().HasLocalScope() && hadGlobalConfig) // GLOBAL -> LOCAL
            {
                _localLocalConfig = _computer.GetSshConfig();
                sshConfigObservableList.set(0, _localLocalConfig);
                sshConfigChoiceBox.getSelectionModel().select(0);
            }

            selectedCheckboxesBeforeChanges.clear();
            selectedCheckboxesBeforeChanges = GetSelectedPreferenceCheckboxes();
            _prevLocalSshFieldsState = null;

            Utilities.ShowInfoDialog("Computer update has succeed.");

            return true;
        }
        catch (NothingToDoException e)
        {
            Utilities.ShowInfoDialog("No changes to save.");
            return false;
        }
        catch(DatabaseException e)
        {
            RestoreComputerChanges();
            Utilities.ShowErrorDialog("Computer update in db has failed.");

            return false;
        }
        catch(Exception e)
        {
            RestoreComputerChanges();
            AppLogger.Log(LogType.FATAL_ERROR , ModuleName, "Unknown error has occurred while updating computer.");
            Utilities.ShowErrorDialog("Unknown error has occurred while updating computer.");

            return false;
        }
    }

    @FXML
    void RemoveComputer(ActionEvent event)
    {
        if(GetParentController().IsRemovingAllowed() == false)
        {
            Utilities.ShowErrorDialog("LogsManager is running. Stop it to remove computer.");
            return;
        }

        boolean removeResponse = Utilities.ShowYesNoDialog("Remove computer?",
                "Do your want to remove computer?");
        if(removeResponse == false)
        {
            return;
        }

        boolean removeLogsResponse = Utilities.ShowYesNoDialog(
                "Remove logs?", "Do your want to remove logs associated with computer?");

        try
        {
           if(removeLogsResponse)
           {
               LogsMaintainer.RemoveAllLogsAssociatedWithComputerFromDb(_computer);
           }

           _computer.RemoveFromDb();

           ChangeEvent changeEvent = new ChangeEvent();
           changeEvent.ChangeType = ChangeEventType.REMOVED;
           changeEvent.Computer = _computer;

           Utilities.ShowInfoDialog("Removing computer succeed.");
           if(StartedInSaveMode() == false)
           {
               _cellCaller.NotifyChanged(changeEvent);
           }
           else
           {
               _parentController.RefreshStatsChoiceBox();
           }

            ((Stage) removeButton.getScene().getWindow()).close();
        }
        catch (DatabaseException e)
        {
           Utilities.ShowErrorDialog("Removing computer from db has failed - database error.");
        }
        catch (ComputerException e)
        {
           Utilities.ShowErrorDialog("Removing computer from db has failed.");
        }
        catch (Exception e)
        {
            AppLogger.Log(LogType.FATAL_ERROR , ModuleName, "Unknown error has occurred while removing computer.");
            Utilities.ShowErrorDialog("Removing computer from db has failed - unknown error.");
        }
    }

    @FXML
    void DiscardChanges(ActionEvent event)
    {
        boolean response = Utilities.ShowYesNoDialog("Discard changes?", "Do you want to discard changes?");
        if(response == false)
        {
            return;
        }

        _isDiscardingChanges = true;

        if(IsInSaveMode())
        {
            displayedNameTextField.setText(null);
            hostTextField.setText(null);
            classroomTextField.setText(null);

            if(sshConfigChoiceBox.getSelectionModel().getSelectedItem() != _localLocalConfig)
            {
                sshConfigChoiceBox.getSelectionModel().select(_localLocalConfig);
            }
            else
            {
                sshUsernameTextField.setText(null);
                sshPasswordPasswordField.setText(null);
                sshKeyPathTextField.setText(null);
                sshPortTextField.setText(null);
            }
            ChooseLocalConfigInChoiceBox();

            requestIntervalTextField.setText(null);
            maintainPeriodTextField.setText(null);
            logExpirationTextField.setText(null);
        }
        else
        {
            displayedNameTextField.setText(_computer.GetDisplayedName());
            hostTextField.setText(_computer.GetHost());
            classroomTextField.setText(_computer.GetClassroom());

            if(sshConfigChoiceBox.getSelectionModel().getSelectedItem() != _computer.GetSshConfig())
            {
                sshConfigChoiceBox.getSelectionModel().select(_computer.GetSshConfig()); // Responsible for setting ssh text fields
            }
            else
            {
                SshConfig oldSshConfig = sshConfigChoiceBox.getSelectionModel().getSelectedItem();
                SshConfig newSshConfig = _computer.GetSshConfig();

                if(newSshConfig.HasGlobalScope())
                {
                    if(oldSshConfig.HasLocalScope() && _isDiscardingChanges == false)
                    {
                        if(_prevLocalSshFieldsState == null)
                        {
                            _prevLocalSshFieldsState = new SshFieldsState();
                        }
                        _prevLocalSshFieldsState.FetchFromSshTextFields();
                    }

                    ChooseGlobalConfigInChoiceBox(newSshConfig);
                }
                else
                {
                    ChooseLocalConfigInChoiceBox();
                }
            }

            requestIntervalTextField.setText(String.valueOf(_computer.GetRequestInterval().toSeconds()));
            maintainPeriodTextField.setText(String.valueOf(_computer.GetMaintainPeriod().toSeconds()));
            logExpirationTextField.setText(String.valueOf(_computer.GetLogExpiration().toSeconds()));
        }

        DiscardChangesInPreferenceCheckboxes();
        _prevLocalSshFieldsState = null;
        _isDiscardingChanges = false;
    }

    @FXML
    void TestConnection(ActionEvent event)
    {
        try
        {
            ValidateIfConnectionCanBeTested();
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
            currentSshFieldsState.FetchFromSshTextFields();
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

    public void OnCloseAction(WindowEvent event)
    {
        if(SomethingChanged()
                && Utilities.ShowYesNoDialog("Discard changes?", "Do you want to discard changes?") == false)
        {
            event.consume();
        }

        if(StartedInSaveMode())
        {
            ChangeEvent changeEvent = new ChangeEvent();
            changeEvent.ChangeType = ChangeEventType.ADDED;
            changeEvent.Computer = _computer;

            _parentController.NotifyChanged(changeEvent);
        }
    }

    // ---  CHOOSING SSH CONFIG IN CHOICEBOX  --------------------------------------------------------------------------

    private void ChooseLocalConfigInChoiceBox()
    {
        if(IsInSaveMode())
        {
            ChooseLocalConfigInChoiceBoxInSaveMode();
        }
        else
        {
            ChooseLocalConfigInChoiceBoxInEditMode();
        }
    }

    private void ChooseLocalConfigInChoiceBoxInSaveMode()
    {
        if(_prevLocalSshFieldsState != null && _isDiscardingChanges == false) // GLOBAL -> EMPTY LOCAL
        {
            _prevLocalSshFieldsState.FillSshTextFields();

            if(_prevLocalSshFieldsState.AuthMethod == SshAuthMethod.PASSWORD)
            {
                if(passwordAuthMethodRadioButton.isSelected())
                {
                    ChoosePasswordAuthMethod();
                }
                else
                {
                    passwordAuthMethodRadioButton.setSelected(true);
                }
            }
            else
            {
                if(privateKeyAuthMethodRadioButton.isSelected())
                {
                    ChoosePrivateKeyAuthMethod();
                }
                else
                {
                    privateKeyAuthMethodRadioButton.setSelected(true);
                }
            }
        }
        else // EMPTY LOCAL -> EMPTY LOCAL
        {
            sshUsernameTextField.setText(null);
            sshPasswordPasswordField.setText(null);
            sshKeyPathTextField.setText(null);
            sshPortTextField.setText(null);

            if(_localLocalConfig.GetAuthMethod()  == SshAuthMethod.PASSWORD)
            {
                if(passwordAuthMethodRadioButton.isSelected())
                {
                    ChoosePasswordAuthMethod();
                }
                else
                {
                    passwordAuthMethodRadioButton.setSelected(true);
                }
            }
            else
            {
                if(privateKeyAuthMethodRadioButton.isSelected())
                {
                    ChoosePrivateKeyAuthMethod();
                }
                else
                {
                    privateKeyAuthMethodRadioButton.setSelected(true);
                }
            }
        }

        _prevLocalSshFieldsState = null;
    }

    private void ChooseLocalConfigInChoiceBoxInEditMode()
    {
        if(_computer.GetSshConfig().HasGlobalScope()) // GLOBAL -> LOCAL
        {
            if(_prevLocalSshFieldsState != null)
            {
                _prevLocalSshFieldsState.FillSshTextFields();

                if(_prevLocalSshFieldsState.AuthMethod == SshAuthMethod.PASSWORD)
                {
                    if(passwordAuthMethodRadioButton.isSelected())
                    {
                        ChoosePasswordAuthMethod();
                    }
                    else
                    {
                        passwordAuthMethodRadioButton.setSelected(true);
                    }
                }
                else
                {
                    if(privateKeyAuthMethodRadioButton.isSelected())
                    {
                        ChoosePrivateKeyAuthMethod();
                    }
                    else
                    {
                        privateKeyAuthMethodRadioButton.setSelected(true);
                    }
                }

                _prevLocalSshFieldsState = null;
            }
            else
            {
                SshFieldsState emptySshFieldsState = new SshFieldsState();
                emptySshFieldsState.FillSshTextFields();

                if(passwordAuthMethodRadioButton.isSelected())
                {
                    ChoosePasswordAuthMethod();
                }
                else
                {
                    passwordAuthMethodRadioButton.setSelected(true);
                }
            }
        }
        else // LOCAL -> LOCAL
        {
            SshFieldsState sshFieldsState = new SshFieldsState();
            sshFieldsState.FetchFromSshConfig(_computer.GetSshConfig());
            sshFieldsState.FillSshTextFields();

            if(sshFieldsState.AuthMethod == SshAuthMethod.PASSWORD)
            {
                if(passwordAuthMethodRadioButton.isSelected())
                {
                    ChoosePasswordAuthMethod();
                }
                else
                {
                    passwordAuthMethodRadioButton.setSelected(true);
                }
            }
            else
            {
                if(privateKeyAuthMethodRadioButton.isSelected())
                {
                    ChoosePrivateKeyAuthMethod();
                }
                else
                {
                    privateKeyAuthMethodRadioButton.setSelected(true);
                }
            }
        }
    }

    private void ChooseGlobalConfigInChoiceBox(SshConfig sshConfig)
    {
        sshPortTextField.setText(String.valueOf(sshConfig.GetPort()));
        sshUsernameTextField.setText(sshConfig.GetUsername());
        if(sshConfig.HasPasswordAuth())
        {
            sshPasswordPasswordField.setText(sshConfig.GetEncryptedPassword());
            sshKeyPathTextField.setText(null);
            if(passwordAuthMethodRadioButton.isSelected())
            {
                ChoosePasswordAuthMethod();
            }
            else
            {
                passwordAuthMethodRadioButton.setSelected(true);
            }
        }
        else
        {
            sshPasswordPasswordField.setText(null);
            sshKeyPathTextField.setText(sshConfig.GetPrivateKeyPath());
            if(privateKeyAuthMethodRadioButton.isSelected())
            {
                ChoosePrivateKeyAuthMethod();
            }
            else
            {
                privateKeyAuthMethodRadioButton.setSelected(true);
            }
        }

        DisableAllSshFields();
        RemoveValidationErrorsFromAllSshFields();
    }

    private void DisableAllSshFields()
    {
        SetDisabledAuthMethodRadioButtons(true);

        sshUsernameTextField.setDisable(true);
        sshPortTextField.setDisable(true);
        sshPasswordPasswordField.setDisable(true);
        sshKeyGridPane.setDisable(true);
    }

    private void SetDisabledAuthMethodRadioButtons(boolean value)
    {
        for (Toggle toggleButton : privateKeyAuthMethodRadioButton.getToggleGroup().getToggles())
        {
            Node node = (Node) toggleButton;
            node.setDisable(value);
        }
    }

    private void RemoveValidationErrorsFromAllSshFields()
    {
        sshUsernameTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshPortTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshPasswordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        sshKeyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
    }

    // ---  CHOOSING AUTH METHOD   -------------------------------------------------------------------------------------

    private void ChoosePasswordAuthMethod()
    {
        SetDisabledAuthMethodRadioButtons(false);
        sshPasswordPasswordField.setDisable(false);
        sshUsernameTextField.setDisable(false);
        sshPortTextField.setDisable(false);

        sshKeyGridPane.setDisable(true);
        sshKeyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateIfRequiredSshFieldsCorrect(SshAuthMethod.PASSWORD);
    }

    private void ChoosePrivateKeyAuthMethod()
    {
        SetDisabledAuthMethodRadioButtons(false);
        sshKeyGridPane.setDisable(false);
        sshUsernameTextField.setDisable(false);
        sshPortTextField.setDisable(false);

        sshPasswordPasswordField.setDisable(true);
        sshPasswordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateIfRequiredSshFieldsCorrect(SshAuthMethod.KEY);
    }

    // ---  PREDICATES  ------------------------------------------------------------------------------------------------

    private boolean StartedInSaveMode()
    {
        return _cellCaller == null;
    }

    private boolean IsInSaveMode()
    {
        return _computer == null;
    }

    private boolean IsInEditMode()
    {
        return _computer != null;
    }

    public boolean SomethingChanged()
    {
        if(IsInEditMode())
        {
            return SomethingChangedInEditMode();
        }
        else
        {
            return SomethingChangedInSaveMode();
        }
    }

    private boolean SomethingChangedInEditMode()
    {
        boolean somethingChanged = _computer.Changed();
        if(somethingChanged)
        {
            _computer.Restore();
        }

        return somethingChanged;
    }

    private boolean SomethingChangedInSaveMode()
    {
        if(IsSelectedGlobalConfig())
        {
            return true;
        }

        if(preferenceCheckboxes.stream().anyMatch(c -> c.isSelected() == true))
        {
            return true;
        }

        return  !Utilities.EmptyOrNull(displayedNameTextField.getText()) ||
                !Utilities.EmptyOrNull(hostTextField.getText()) ||
                !Utilities.EmptyOrNull(classroomTextField.getText()) ||
                !Utilities.EmptyOrNull(sshUsernameTextField.getText()) ||
                !Utilities.EmptyOrNull(sshPasswordPasswordField.getText()) ||
                !Utilities.EmptyOrNull(sshKeyPathTextField.getText()) ||
                !Utilities.EmptyOrNull(sshPortTextField.getText()) ||
                !Utilities.EmptyOrNull(requestIntervalTextField.getText()) ||
                !Utilities.EmptyOrNull(maintainPeriodTextField.getText()) ||
                !Utilities.EmptyOrNull(logExpirationTextField.getText());
    }

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

    private boolean LocalSshConfigChanged()
    {
        SshFieldsState currentSshFieldsState = new SshFieldsState();
        currentSshFieldsState.FetchFromSshTextFields();

        return  !Utilities.AreEqual(_computer.GetSshConfig().GetUsername(), currentSshFieldsState.Username) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetPort(), currentSshFieldsState.Port) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetAuthMethod(), currentSshFieldsState.AuthMethod) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetEncryptedPassword() == null ?
                        null : GetDecryptedPasswordFromLocalSshConfig(), currentSshFieldsState.Password) ||
                !Utilities.AreEqual(_computer.GetSshConfig().GetPrivateKeyPath(), currentSshFieldsState.PrivateKeyPath);
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

    private MainWindowController GetParentController()
    {
        if(_parentController != null)
        {
            return _parentController;
        }
        else
        {
            return _cellCaller.GetController();
        }
    }

    private List<Preference> GetSelectedPreferences()
    {
        List<Preference> preferences = new ArrayList<>();
        List<CheckBox> selectedCheckBoxes = GetSelectedPreferenceCheckboxes();

        for (CheckBox selectedCheckBox : selectedCheckBoxes)
        {
            String preferenceClassName = Utilities.GetClassNameForPreferenceName(selectedCheckBox.getText());
            Preference preference = Utilities.GetPreferenceFromClassName(preferenceClassName);
            preferences.add(preference);
        }

        return preferences;
    }

    private List<CheckBox> GetSelectedPreferenceCheckboxes()
    {
        List<CheckBox> selectedCheckBoxes = preferenceCheckboxes.stream()
                .filter(cb -> cb.isSelected() == true).collect(Collectors.toList());

        return selectedCheckBoxes;
    }

    public SshConfig GetSelectedSshConfig()
    {
        return sshConfigChoiceBox.getSelectionModel().getSelectedItem();
    }

    private String GetDecryptedPasswordFromLocalSshConfig()
    {
        try
        {
            return Encrypter.GetInstance().Decrypt(_computer.GetSshConfig().GetEncryptedPassword());
        }
        catch (EncrypterException e)
        {
            Utilities.ShowErrorDialog("Ssh password from local config could not be decrypted." +
                    "\nProvide new password.");

            return null;
        }
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    private void RestoreComputerChanges()
    {
        _computer.Restore();
    }

    private void DiscardChangesInPreferenceCheckboxes()
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
}
