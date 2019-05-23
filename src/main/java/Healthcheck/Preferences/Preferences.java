package Healthcheck.Preferences;

import java.util.ArrayList;
import java.util.List;

public class Preferences
{
    public static final CpuInfoPreference CpuInfoPreference = new CpuInfoPreference();

    public static final DisksInfoPreference DisksInfoPreference = new DisksInfoPreference();

    public static final ProcessesInfoPreference ProcessesInfoPreference = new ProcessesInfoPreference();

    public static final RamInfoPreference RamInfoPreference = new RamInfoPreference();

    public static final SwapInfoPreference SwapInfoPreference = new SwapInfoPreference();

    public static final UsersInfoPreference UsersInfoPreference = new UsersInfoPreference();

    public static final List<IPreference> AllPreferencesList = new ArrayList<IPreference>(){{
        add(CpuInfoPreference);
        add(DisksInfoPreference);
        add(ProcessesInfoPreference);
        add(RamInfoPreference);
        add(SwapInfoPreference);
        add(UsersInfoPreference);
    }};
}
