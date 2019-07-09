package Healthcheck.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preferences
{
    public static final CpusInfoPreference CpusInfoPreference = new CpusInfoPreference();
    public static final DisksInfoPreference DisksInfoPreference = new DisksInfoPreference();
    public static final ProcessesInfoPreference ProcessesInfoPreference = new ProcessesInfoPreference();
    public static final RamInfoPreference RamInfoPreference = new RamInfoPreference();
    public static final SwapInfoPreference SwapInfoPreference = new SwapInfoPreference();
    public static final UsersInfoPreference UsersInfoPreference = new UsersInfoPreference();

    public static final List<IPreference> AllPreferencesList = new ArrayList<IPreference>(){{
        add(CpusInfoPreference);
        add(DisksInfoPreference);
        add(ProcessesInfoPreference);
        add(RamInfoPreference);
        add(SwapInfoPreference);
        add(UsersInfoPreference);
    }};

    public static final Map<String, IPreference> PreferenceClassNameMap = new HashMap<String, IPreference>(){{
        put("Healthcheck.Preferences.CpusInfoPreference", CpusInfoPreference);
        put("Healthcheck.Preferences.DisksInfoPreference", DisksInfoPreference);
        put("Healthcheck.Preferences.ProcessesInfoPreference", ProcessesInfoPreference);
        put("Healthcheck.Preferences.RamInfoPreference", RamInfoPreference);
        put("Healthcheck.Preferences.SwapInfoPreference", SwapInfoPreference);
        put("Healthcheck.Preferences.UsersInfoPreference", UsersInfoPreference);
    }};

    public static final Map<String, IPreference> PreferenceNameMap = new HashMap<String, IPreference>(){{
        put("CpusInfoPreference", CpusInfoPreference);
        put("DisksInfoPreference", DisksInfoPreference);
        put("ProcessesInfoPreference", ProcessesInfoPreference);
        put("RamInfoPreference", RamInfoPreference);
        put("SwapInfoPreference", SwapInfoPreference);
        put("UsersInfoPreference", UsersInfoPreference);
    }};
}
