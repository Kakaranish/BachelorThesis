package GUI.Controllers;

import GUI.ListItems.ComputerItem;
import GUI.ListItems.GroupSettingsComputerListCell;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Preference;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Preferences.Preferences;
import Healthcheck.Utilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class GroupSettingsController implements Initializable
{
    public ObservableList<ComputerItem> computerItemsObservableList = FXCollections.observableArrayList();

    @FXML
    private CheckBox changePreferencesCheckBox;

    @FXML
    private CheckBox gatheringIntervalCheckBox;

    @FXML
    private CheckBox cpuInfoCheckBox;

    @FXML
    private TextField logExpirationTextField;

    @FXML
    private TextField maintenancePeriodTextField;

    @FXML
    private Button applyNewSettingsButton;

    @FXML
    private CheckBox maintenancePeriodCheckBox;

    @FXML
    private CheckBox usersInfoCheckBox;

    @FXML
    private CheckBox ramInfoCheckBox;

    @FXML
    private CheckBox swapInfoCheckBox;

    @FXML
    private CheckBox disksInfoCheckBox;

    @FXML
    private ListView<ComputerItem> computersListView;

    @FXML
    private CheckBox logExpirationCheckBox;

    @FXML
    private TextField gatheringIntervalTextField;

    @FXML
    private CheckBox processesInfoCheckBox;

    @FXML
    private Button deselectAllComputersButton;

    @FXML
    private Button selectAllComputersButton;

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private MainWindowController _controller;
    private GroupSettingsController thisController = this;

    private List<GroupSettingsComputerListCell> groupSettingsComputerListCells = new ArrayList<>();

    public GroupSettingsController(ComputersAndSshConfigsManager computersAndSshConfigsManager,
                                   MainWindowController mainWindowController)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = mainWindowController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        InitializeGeneralSettingsSection();
        InitializeChangePreferencesCheckBox();
        SetUpIntegerValidators();
        SetDisabledPreferencesCheckBoxes(true);

        LoadComputersToListView();

        InitializeApplyNewSettingsButton();
        InitializeSelectAllComputersButton();
        InitializeDeselectAllComputersButton();
    }

    private void SetUpIntegerValidators()
    {
        SetUpIntegerValidator(gatheringIntervalTextField);
        SetUpIntegerValidator(maintenancePeriodTextField);
        SetUpIntegerValidator(logExpirationTextField);

        ValidateIfInteger(gatheringIntervalTextField);
        ValidateIfInteger(maintenancePeriodTextField);
        ValidateIfInteger(logExpirationTextField);
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

    private void RemoveValidationError(TextField textField)
    {
        textField.getStyleClass().removeAll(Collections.singletonList("validation-error"));
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

    public List<String> ValidateBeforeApplyingNewSettings()
    {
        List<String> errors = new ArrayList<>();

        if(gatheringIntervalCheckBox.isSelected() && IsParsableToInteger(gatheringIntervalTextField.getText()) == false)
        {
            errors.add("Gathering interval must be integer.");
        }

        if(maintenancePeriodCheckBox.isSelected() && IsParsableToInteger(maintenancePeriodTextField.getText()) == false)
        {
            errors.add("Maintenance period must be integer.");
        }

        if(logExpirationCheckBox.isSelected() && IsParsableToInteger(logExpirationTextField.getText()) == false)
        {
            errors.add("Log expiration must be integer.");
        }

        return errors;
    }

    private void InitializeApplyNewSettingsButton()
    {
        applyNewSettingsButton.setOnAction(event ->
        {
            List<String> errors = ValidateBeforeApplyingNewSettings();
            if(errors.size() > 0)
            {
                Utilities.ShowSaveErrorDialog(errors);
                return;
            }

            List<String> displayedNamesOfUpdatedComputers = new ArrayList<>();
            for (GroupSettingsComputerListCell groupSettingsComputerListCell : groupSettingsComputerListCells)
            {
                if(groupSettingsComputerListCell.IsSelected() == false)
                {
                    continue;
                }

                Computer computerToUpdate = groupSettingsComputerListCell.GetComputer();
                if(displayedNamesOfUpdatedComputers.contains(computerToUpdate.GetDisplayedName()))
                {
                    continue;
                }

                if(gatheringIntervalCheckBox.isSelected())
                {
                    computerToUpdate.SetRequestInterval(Utilities.ConvertSecondsToDurationInNanos(
                            Long.parseLong(gatheringIntervalTextField.getText())));
                }
                if(maintenancePeriodCheckBox.isSelected())
                {
                    computerToUpdate.SetMaintainPeriod(Utilities.ConvertSecondsToDurationInNanos(
                            Long.parseLong(maintenancePeriodTextField.getText())));
                }
                if(logExpirationCheckBox.isSelected())
                {
                    computerToUpdate.SetLogExpiration(Utilities.ConvertSecondsToDurationInNanos(
                            Long.parseLong(logExpirationTextField.getText())));
                }
                if(changePreferencesCheckBox.isSelected())
                {
                    List<Preference> preferences = new ArrayList<>();

                    if(cpuInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.CpuInfoPreference));
                    }
                    if(usersInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.UsersInfoPreference));
                    }
                    if(swapInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.SwapInfoPreference));
                    }
                    if(ramInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.RamInfoPreference));
                    }
                    if(processesInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.ProcessesInfoPreference));
                    }
                    if(disksInfoCheckBox.isSelected())
                    {
                        preferences.add(Utilities.ConvertIPreferenceToPreference(Preferences.DisksInfoPreference));
                    }

                    computerToUpdate.SetPreferences(preferences);
                }

                try
                {
                    computerToUpdate.UpdateInDb();
                    displayedNamesOfUpdatedComputers.add(computerToUpdate.GetDisplayedName());
                }
                catch (NothingToDoException e)
                {
                    Platform.runLater(() -> AppLogger.Log(LogType.INFO, "GroupSettingsController",
                            "'" + computerToUpdate.GetUsernameAndHost() + "' has nothing to update."));
                }
                catch(Exception e)
                {
                    Utilities.ShowErrorDialog("Update of computers failed. Only some of them have been updated.");
                    return;
                }
            }

            Platform.runLater(() -> AppLogger.Log(LogType.INFO, "GroupSettingsController",
                    "Successfully applied new settings to selected computers."));
            Utilities.ShowInfoDialog("Successfully applied new settings to selected computers.");
        });
    }

    private void InitializeGeneralSettingsSection()
    {
        InitializeCheckBoxTextFieldPair(gatheringIntervalCheckBox, gatheringIntervalTextField);
        InitializeCheckBoxTextFieldPair(maintenancePeriodCheckBox, maintenancePeriodTextField);
        InitializeCheckBoxTextFieldPair(logExpirationCheckBox, logExpirationTextField);
    }

    private void InitializeCheckBoxTextFieldPair(CheckBox checkBox, TextField textField)
    {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
            {
                textField.setDisable(false);
                ValidateIfInteger(textField);
            }
            else
            {
                textField.setDisable(true);
                RemoveValidationError(textField);

            }
        });
        checkBox.selectedProperty().setValue(true);
    }

    private void InitializeSelectAllComputersButton()
    {
        selectAllComputersButton.setOnAction(event ->
        {
            for (GroupSettingsComputerListCell groupSettingsComputerListCell : groupSettingsComputerListCells)
            {
                groupSettingsComputerListCell.SetSelected(true);
            }
        });
    }

    private void InitializeDeselectAllComputersButton()
    {
        deselectAllComputersButton.setOnAction(event ->
        {
            for (GroupSettingsComputerListCell groupSettingsComputerListCell : groupSettingsComputerListCells)
            {
                groupSettingsComputerListCell.SetSelected(false);
            }
        });
    }

    private void InitializeChangePreferencesCheckBox()
    {
        changePreferencesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
            {
                SetDisabledPreferencesCheckBoxes(false);
            }
            else
            {
                SetDisabledPreferencesCheckBoxes(true);
            }
        });
    }

    private void SetDisabledPreferencesCheckBoxes(boolean disabled)
    {
        cpuInfoCheckBox.setDisable(disabled);
        processesInfoCheckBox.setDisable(disabled);
        swapInfoCheckBox.setDisable(disabled);
        ramInfoCheckBox.setDisable(disabled);
        usersInfoCheckBox.setDisable(disabled);
        disksInfoCheckBox.setDisable(disabled);
    }

    private void LoadComputersToListView()
    {
        for (Computer computer : _computersAndSshConfigsManager.GetComputers())
        {
            computerItemsObservableList.add(new ComputerItem()
            {{
                IsSelected = false;
                DisplayedName = computer.GetDisplayedName();
                Host = computer.GetHost();
            }});
        }

        computersListView.setItems(computerItemsObservableList);
        computersListView.setCellFactory(
                listCell -> new GroupSettingsComputerListCell(_computersAndSshConfigsManager, thisController));
    }

    public void AddGroupSettingsListCell(GroupSettingsComputerListCell groupSettingsComputerListCell)
    {
        groupSettingsComputerListCells.add(groupSettingsComputerListCell);
    }
}
