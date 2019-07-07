package GUI.Controllers;

import GUI.ListItems.ComputerItem;
import GUI.ListItems.GroupSettingsComputerListCell;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class GroupSettingsController implements Initializable
{
    public ObservableList<ComputerItem> computerItemsObservableList = FXCollections.observableArrayList();

    @FXML
    private TextField portTextField;

    @FXML
    private CheckBox changePreferencesCheckBox;

    @FXML
    private CheckBox portCheckBox;

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

    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private MainWindowController _controller;
    private GroupSettingsController thisController = this;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        LoadComputersToListView();
    }

    public GroupSettingsController(ComputersAndSshConfigsManager computersAndSshConfigsManager,
                                   MainWindowController mainWindowController)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = mainWindowController;
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
}
