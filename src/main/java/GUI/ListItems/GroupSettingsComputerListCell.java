package GUI.ListItems;

import GUI.Controllers.*;
import Healthcheck.ComputersAndSshConfigsManager;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GroupSettingsComputerListCell extends ListCell<ComputerItem>
{
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private GroupSettingsController _controller;

    private CheckBox IsSelected;
    private HBox content;
    private Text DisplayedName;
    private Text Host;

    private Boolean IsSelectedValue = null;

    public GroupSettingsComputerListCell(ComputersAndSshConfigsManager computersAndSshConfigsManager,
                                         GroupSettingsController controller)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _controller = controller;

        IsSelected = new CheckBox();
        IsSelected.setSelected(true);
        IsSelected.selectedProperty().addListener((observable, oldValue, newValue) -> IsSelectedValue = newValue);

        DisplayedName = new Text();
        DisplayedName.setFont(new Font(17.5));
        Host = new Text();

        VBox vBox = new VBox(DisplayedName, Host);

        content = new HBox(IsSelected, vBox);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void updateItem(ComputerItem item, boolean empty)
    {
        super.updateItem(item, empty);
        if (item != null && !empty)
        {
            if(IsSelectedValue == null)
            {
                IsSelectedValue = item.IsSelected;
            }

            IsSelected.setSelected(IsSelectedValue);
            DisplayedName.setText(item.DisplayedName);
            Host.setText(item.Host);

            setGraphic(content);
        }
        else
        {
            setGraphic(null);
        }
    }

    public GroupSettingsController GetController()
    {
        return _controller;
    }
}
