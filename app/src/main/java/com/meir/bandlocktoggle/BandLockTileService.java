package com.meir.bandlocktoggle;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class BandLockTileService extends TileService {

    private static boolean busy = false;

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateFromPrefs();
    }

    @Override
    public void onClick() {
        super.onClick();

        if (busy) {
            return;
        }

        busy = true;

        final String current = Prefs.mode(this);

        // Toggle:
        // B3 -> ALL
        // ALL -> B3
        final String next =
                "0x4".equals(current)
                        ? "ALL"
                        : "0x4";

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel(
                    "0x4".equals(next)
                            ? "B3"
                            : "ALL"
            );
            tile.updateTile();
        }

        new Thread(() -> {

            RootQmi.Result r =
                    RootQmi.apply(
                            Prefs.path(this),
                            next
                    );

            if (r.ok) {
                // Save the NEW state only after QMI succeeded.
                Prefs.setMode(this, next);
            }

            busy = false;

            updateTile(r.ok, next);

        }).start();
    }

    private void updateTile(boolean success, String mode) {

        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        if (success) {

            tile.setState(Tile.STATE_ACTIVE);

            if ("0x4".equals(mode)) {
                tile.setLabel("B3");
                tile.setContentDescription(
                        "Band lock: B3"
                );
            } else {
                tile.setLabel("ALL");
                tile.setContentDescription(
                        "Band lock: All bands"
                );
            }

        } else {

            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("ERR");
            tile.setContentDescription(
                    "Band lock command failed"
            );
        }

        tile.updateTile();
    }

    private void updateFromPrefs() {

        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        String mode = Prefs.mode(this);

        tile.setState(Tile.STATE_ACTIVE);

        if ("0x4".equals(mode)) {
            tile.setLabel("B3");
            tile.setContentDescription(
                    "Band lock: B3"
            );
        } else {
            tile.setLabel("ALL");
            tile.setContentDescription(
                    "Band lock: All bands"
            );
        }

        tile.updateTile();
    }
}
