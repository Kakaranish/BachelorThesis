package GUI.Controllers;

import GUI.ChangeEvent.ChangeEvent;
import GUI.ChangeEvent.ChangeEventType;
import GUI.ListItems.SshConfigListCell;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.SshAuthMethod;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigException;
import Healthcheck.Entities.SshConfigScope;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Utilities;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class AddOrUpdateSshConfigController implements Initializable
{
    @FXML
    private TextField displayedNameTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private ToggleGroup authenticationMethodGroup;

    @FXML
    private RadioButton passwordAuthMethodRadioButton;

    @FXML
    private RadioButton privateKeyAuthMethodRadioButton;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private GridPane keyGridPane;

    @FXML
    private TextField keyPathTextField;

    @FXML
    private Button chooseSshKeyButton;

    @FXML
    private TextField portTextField;

    @FXML
    private Button saveOrUpdateButton;

    @FXML
    private Button removeButton;

    // -----------------------------------------------------------------------------------------------------------------

    private MainWindowController _parentController;
    private SshConfigListCell _cellCaller;
    private SshConfig _sshConfig;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;

    public AddOrUpdateSshConfigController(MainWindowController parent, SshConfig sshConfig,
                                          ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _parentController = parent;
        _sshConfig = sshConfig;
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
    }

    public AddOrUpdateSshConfigController(SshConfigListCell cellCaller, SshConfig sshConfig,
                                          ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _cellCaller = cellCaller;
        _sshConfig = sshConfig;
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
    }

    // ---  INITIALIZATION  --------------------------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeAndFillGuiComponents();

        SetUpIntegerValidators();
        SetUpEmptinessValidators();

        ClearValidationErrorsFromAllFields();
        AddValidationErrorsToAllFieldsIfIncorrect();
    }

    private void InitializeAndFillGuiComponents()
    {
        InitializeChooseFileButton();
        InitializeAuthMethodRadioButtons();

        if(IsInEditMode())
        {
            saveOrUpdateButton.setText("Update");
            removeButton.setDisable(false);

            displayedNameTextField.setText(_sshConfig.GetName());
            usernameTextField.setText(_sshConfig.GetUsername());

            if(_sshConfig.HasPasswordAuth())
            {
                String decryptedPassword = null;
                try
                {
                    decryptedPassword = Encrypter.GetInstance().Decrypt(_sshConfig.GetEncryptedPassword());
                }
                catch (EncrypterException e)
                {
                    Utilities.ShowErrorDialog("Connection with computer cannot be established." +
                            "\nUnable to decrypt ssh password.");
                }
                passwordPasswordField.setText(decryptedPassword);
                passwordAuthMethodRadioButton.setSelected(true);
            }
            else
            {
                keyPathTextField.setText(_sshConfig.GetPrivateKeyPath());
                privateKeyAuthMethodRadioButton.setSelected(true);
            }
            portTextField.setText(String.valueOf(_sshConfig.GetPort()));
        }
        else
        {
            if(Utilities.UseDefaultValues)
            {
                portTextField.setText(String.valueOf(Utilities.DefaultPort));
            }

            passwordAuthMethodRadioButton.setSelected(true);
            saveOrUpdateButton.setText("Add");
            removeButton.setDisable(true);
        }
    }

    private void InitializeChooseFileButton()
    {
        chooseSshKeyButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                Stage stage = (Stage) displayedNameTextField.getScene().getWindow();

                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(stage);
                if(file != null)
                {
                    keyPathTextField.setText(file.getAbsolutePath());
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


    // ---  RUNTIME VALIDATION  ----------------------------------------------------------------------------------------

    private void SetUpIntegerValidators()
    {
        SetUpIntegerValidator(portTextField);
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
        SetUpEmptinessValidator(usernameTextField);
        SetUpEmptinessValidator(passwordPasswordField);
        SetUpEmptinessValidator(keyPathTextField);
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

    // ---  INSTANT VALIDATION  ----------------------------------------------------------------------------------------

    private void ClearValidationErrorsFromAllFields()
    {
        displayedNameTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        usernameTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        passwordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        keyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
        portTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
    }

    private void AddValidationErrorsToAllFieldsIfIncorrect()
    {
        AddValidationErrorIfEmpty(displayedNameTextField);
        AddValidationErrorIfEmpty(usernameTextField);
        if(IsSelectedPasswordAuthMethod())
        {
            AddValidationErrorIfEmpty(passwordPasswordField);
        }
        else
        {
            AddValidationErrorIfEmpty(keyPathTextField);
        }
        AddValidationErrorIfNotParsableToInteger(portTextField);
    }

    private void AddValidationErrorIfEmpty(TextField textField)
    {
        if(Utilities.EmptyOrNull(textField.getText()))
        {
            textField.getStyleClass().add("validation-error");
        }
    }

    private void AddValidationErrorIfNotParsableToInteger(TextField textField)
    {
        if(IsParsableToInteger(textField.getText()) == false)
        {
            textField.getStyleClass().add("validation-error");
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

    // ---  BEFORE SAVE VALIDATION  ------------------------------------------------------------------------------------

    private List<String> GetValidationErrorListBeforeSaveOrUpdate()
    {
        List<String> errors = new ArrayList<>();

        if(Utilities.EmptyOrNull(displayedNameTextField.getText()))
        {
            errors.add("Displayed name cannot be empty.");
        }

        if(Utilities.EmptyOrNull(usernameTextField.getText()))
        {
            errors.add("Username cannot be empty.");
        }

        if(IsSelectedPasswordAuthMethod() && Utilities.EmptyOrNull(passwordPasswordField.getText()))
        {
            errors.add("Password cannot be empty.");
        }
        else if(IsSelectedPrivateKeyAuthMethod() && Utilities.EmptyOrNull(keyPathTextField.getText()))
        {
            errors.add("Private key path cannot be empty.");
        }

        if(IsParsableToInteger(portTextField.getText()) == false)
        {
            errors.add("Port must be integer.");
        }

        return errors;
    }

    private String ValidateOtherSshConfigHasSameDisplayedName()
    {
        if(IsInEditMode())
        {
            if(_sshConfig.GetName().equals(displayedNameTextField.getText()) == false
                    && _computersAndSshConfigsManager.SshConfigWithNameExists(displayedNameTextField.getText()))
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                return "Other ssh config has same displayed name.";
            }
        }
        else
        {
            if(_computersAndSshConfigsManager.SshConfigWithNameExists(displayedNameTextField.getText()))
            {
                displayedNameTextField.getStyleClass().add("validation-error");
                return "Other ssh config has same displayed name.";
            }
        }

        return null;
    }

    // ---  COPYING CHANGES TO SSH CONFIG  -----------------------------------------------------------------------------

    private void CopyChangesToSshConfigInSaveMode()
    {
        if(IsSelectedPasswordAuthMethod())
        {
            try
            {
                String encryptedPassword = Encrypter.GetInstance().Encrypt(passwordPasswordField.getText());
                _sshConfig  = new SshConfig(
                        displayedNameTextField.getText(),
                        SshConfigScope.GLOBAL,
                        Integer.valueOf(portTextField.getText()),
                        SshAuthMethod.PASSWORD,
                        usernameTextField.getText(),
                        encryptedPassword
                );
            }
            catch (EncrypterException e)
            {
                // This block will be never entered
            }
        }
        else
        {
            _sshConfig  = new SshConfig(
                    displayedNameTextField.getText(),
                    SshConfigScope.GLOBAL,
                    Integer.valueOf(portTextField.getText()),
                    SshAuthMethod.KEY,
                    usernameTextField.getText(),
                    keyPathTextField.getText()
            );
        }
        _sshConfig.SetComputersAndSshConfigsManager(_computersAndSshConfigsManager);
    }

    private void CopyChangesToSshConfigInEditMode()
    {
        _sshConfig.SetGlobalScope(displayedNameTextField.getText());
        _sshConfig.SetUsername(usernameTextField.getText());
        if(IsSelectedPasswordAuthMethod())
        {
            try
            {
                String encryptedPassword = Encrypter.GetInstance().Encrypt(passwordPasswordField.getText());
                _sshConfig.SetPasswordAuthMethod(encryptedPassword);
            }
            catch (EncrypterException e)
            {
                // This block will be never entered
            }
        }
        else
        {
            _sshConfig.SetSshKeyAuthMethod(keyPathTextField.getText());
        }
        _sshConfig.SetPort(Integer.valueOf(portTextField.getText()));
    }

    // ---  BUTTONS LOGIC  ---------------------------------------------------------------------------------------------

    @FXML
    void SaveOrUpdate(ActionEvent event)
    {
        if(IsInEditMode() && GetParentController().IsEditionAllowed() == false)
        {
            Utilities.ShowErrorDialog("LogsManager is running. Stop it to edit ssh config.");
            return;
        }

        List<String> emptinessOrIntegerErrors = GetValidationErrorListBeforeSaveOrUpdate();
        if (emptinessOrIntegerErrors.size() > 0)
        {
            Utilities.ShowSaveErrorDialog(emptinessOrIntegerErrors);
            return;
        }
        String displayedNameError = ValidateOtherSshConfigHasSameDisplayedName();
        if(displayedNameError != null)
        {
            Utilities.ShowSaveErrorDialog(new ArrayList<>(){{
                add(displayedNameError);
            }});
            return;
        }

        if(IsInSaveMode())
        {
            boolean saveSucceed = Save();
            if(saveSucceed)
            {
                saveOrUpdateButton.setText("Update");
                removeButton.setDisable(false);
            }
        }
        else
        {
            boolean updateSucceed = Update();
            if(updateSucceed)
            {
                if(StartedInSaveMode() == false)
                {
                    ChangeEvent changeEvent = new ChangeEvent();
                    changeEvent.ChangeType = ChangeEventType.UPDATED;
                    changeEvent.SshConfig = _sshConfig;

                    _cellCaller.NotifyChanged(changeEvent);
                }
            }
        }
    }

    private boolean Save()
    {
        CopyChangesToSshConfigInSaveMode();

        try
        {
            _sshConfig.AddToDb();

            Utilities.ShowInfoDialog("Adding ssh config has succeed.");

            return true;
        }
        catch(DatabaseException e)
        {
            _sshConfig = null;
            Utilities.ShowErrorDialog("Adding ssh config to db has failed.");

            return false;
        }
        catch(Exception e)
        {
            _sshConfig = null;
            e.printStackTrace();
            Utilities.ShowErrorDialog("Unknown error has occurred while saving.");

            return false;
        }
    }

    private boolean Update()
    {
        CopyChangesToSshConfigInEditMode();

        if(_sshConfig.Changed() == false)
        {
            Utilities.ShowInfoDialog("Nothing to update.");
            return false;
        }

        try
        {
            _sshConfig.UpdateInDb();

            Utilities.ShowInfoDialog("Ssh config update has succeed.");

            return true;
        }
        catch (NothingToDoException e)
        {
            Utilities.ShowInfoDialog("No changes to save.");
            return false;
        }
        catch(DatabaseException e)
        {
            RestoreSshConfigChanges();
            Utilities.ShowErrorDialog("Ssh config update in db has failed.");

            return false;
        }
        catch(Exception e)
        {
            RestoreSshConfigChanges();
            e.printStackTrace();
            Utilities.ShowErrorDialog("Unknown error has occurred while updating ssh config.");

            return false;
        }
    }

    @FXML
    void Remove(ActionEvent event)
    {
        if(GetParentController().IsRemovingAllowed() == false)
        {
            Utilities.ShowErrorDialog("LogsManager is running. Stop it to remove ssh config.");
            return;
        }

        boolean removeResponse = Utilities.ShowYesNoDialog("Remove ssh config?",
                "Do your want to remove ssh config?");
        if(removeResponse == false)
        {
            return;
        }

        try
        {
            _sshConfig.RemoveGlobalFromDb();

            if(StartedInSaveMode() == false)
            {
                ChangeEvent changeEvent = new ChangeEvent();
                changeEvent.ChangeType = ChangeEventType.REMOVED;
                changeEvent.SshConfig = _sshConfig;

                _cellCaller.NotifyChanged(changeEvent);
            }

            Utilities.ShowInfoDialog("Removing ssh config succeed.");

            ((Stage) removeButton.getScene().getWindow()).close();
        }
        catch(DatabaseException e)
        {
            Utilities.ShowErrorDialog("Removing ssh config from db has failed - database error.");
        }
        catch(SshConfigException e)
        {
            Utilities.ShowErrorDialog("Removing ssh config from db has failed.\n" + e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Utilities.ShowErrorDialog("Removing ssh config from db has failed - unknown error.");
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

        if(IsInSaveMode())
        {
            displayedNameTextField.setText(null);
            usernameTextField.setText(null);
            passwordPasswordField.setText(null);
            keyPathTextField.setText(null);
            portTextField.setText(null);

            passwordAuthMethodRadioButton.setSelected(true);
        }
        else
        {
            displayedNameTextField.setText(_sshConfig.GetName());
            usernameTextField.setText(_sshConfig.GetUsername());
            if(_sshConfig.HasPasswordAuth())
            {
                try
                {
                    String decryptedPassword = Encrypter.GetInstance().Decrypt(_sshConfig.GetEncryptedPassword());
                    passwordPasswordField.setText(decryptedPassword);
                }
                catch(EncrypterException e)
                {
                    Utilities.ShowErrorDialog("Connection with computer cannot be established." +
                            "\nUnable to decrypt ssh password.");
                    passwordPasswordField.setText(null);
                }
                keyPathTextField.setText(null);
                passwordAuthMethodRadioButton.setSelected(true);
            }
            else
            {
                passwordPasswordField.setText(null);
                keyPathTextField.setText(_sshConfig.GetPrivateKeyPath());
                privateKeyAuthMethodRadioButton.setSelected(true);
            }

            portTextField.setText(String.valueOf(_sshConfig.GetPort()));
        }

        ClearValidationErrorsFromAllFields();
        AddValidationErrorsToAllFieldsIfIncorrect();
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
            changeEvent.SshConfig = _sshConfig;

            _parentController.NotifyChanged(changeEvent);
        }
    }

    // --- CHOOSING AUTH METHOD  ---------------------------------------------------------------------------------------

    private void ChoosePasswordAuthMethod()
    {
        passwordPasswordField.setDisable(false);
        keyGridPane.setDisable(true);
        keyPathTextField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateIfEmpty(passwordPasswordField);
    }

    private void ChoosePrivateKeyAuthMethod()
    {
        keyGridPane.setDisable(false);
        passwordPasswordField.setDisable(true);
        passwordPasswordField.getStyleClass().removeAll(Collections.singletonList("validation-error"));

        ValidateIfEmpty(keyPathTextField);
    }

    // ---  PREDICATES  ------------------------------------------------------------------------------------------------

    private boolean StartedInSaveMode()
    {
        return _cellCaller == null;
    }

    private boolean IsInSaveMode()
    {
        return _sshConfig == null;
    }

    private boolean IsInEditMode()
    {
        return _sshConfig != null;
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

    private boolean SomethingChangedInSaveMode()
    {
        if(IsSelectedPrivateKeyAuthMethod())
        {
            return true;
        }

        return  !Utilities.EmptyOrNull(displayedNameTextField.getText()) ||
                !Utilities.EmptyOrNull(usernameTextField.getText()) ||
                !Utilities.EmptyOrNull(passwordPasswordField.getText()) ||
                !Utilities.EmptyOrNull(keyPathTextField.getText()) ||
                !Utilities.EmptyOrNull(portTextField.getText());
    }

    private boolean SomethingChangedInEditMode()
    {
        boolean somethingChanged = _sshConfig.Changed();
        if(somethingChanged)
        {
            _sshConfig.Restore();
        }

        return somethingChanged;
    }

    private boolean IsSelectedPasswordAuthMethod()
    {
        return passwordAuthMethodRadioButton.isSelected();
    }

    private boolean IsSelectedPrivateKeyAuthMethod()
    {
        return privateKeyAuthMethodRadioButton.isSelected();
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    private void RestoreSshConfigChanges()
    {
        _sshConfig.Restore();
    }

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
}
