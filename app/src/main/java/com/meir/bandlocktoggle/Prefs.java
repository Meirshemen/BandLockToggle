package com.meir.bandlocktoggle;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private static final String NAME = "bandlock";
    private static final String PATH = "qmi_path";
    private static final String LAST = "last_mode";
    static SharedPreferences get(Context c) { return c.getSharedPreferences(NAME, Context.MODE_PRIVATE); }
    static String path(Context c) { return get(c).getString(PATH, RootQmi.DEFAULT_PATH); }
    static String mode(Context c) { return get(c).getString(LAST, "0x4"); }
    static void setPath(Context c, String p) { get(c).edit().putString(PATH, p).apply(); }
    static void setMode(Context c, String m) { get(c).edit().putString(LAST, m).apply(); }
}
