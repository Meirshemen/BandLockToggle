package com.meir.bandlocktoggle;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.content.Intent;

public class BandLockTileService extends TileService {
    @Override public void onStartListening() { super.onStartListening(); updateFromPrefs(); }

    @Override public void onClick() {
        super.onClick();
        final String next = Prefs.mode(this).equals("0x4") ? "ALL" : "0x4";
        getQsTile().setState(Tile.STATE_UNAVAILABLE);
        getQsTile().setLabel(next.equals("0x4") ? "B3" : "ALL");
        getQsTile().updateTile();
        new Thread(() -> {
            RootQmi.Result r = RootQmi.apply(Prefs.path(this), next);
            if (r.ok) Prefs.setMode(this, next);
            if (getQsTile() != null) {
                getQsTile().setState(r.ok ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                getQsTile().setLabel(r.ok ? (next.equals("0x4") ? "B3" : "ALL") : "ERR");
                getQsTile().updateTile();
            }
        }).start();
    }

    private void updateFromPrefs() {
        String m = Prefs.mode(this);
        getQsTile().setState(Tile.STATE_ACTIVE);
        getQsTile().setLabel(m.equals("0x4") ? "B3" : "ALL");
        getQsTile().setContentDescription("Band lock: " + (m.equals("0x4") ? "B3" : "All bands"));
        getQsTile().updateTile();
    }
}
