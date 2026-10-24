
package com.meir.bandlocktoggle;

import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class BandLockTileService extends TileService {

    private static volatile boolean busy = false;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

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

        final String next =
                "0x4".equals(current) || "4".equals(current)
                        ? "ALL"
                        : "0x4";

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel(
                    "0x4".equals(next) ? "B3" : "ALL"
            );
            tile.updateTile();
        }

        final String path = Prefs.path(this);

        new Thread(() -> {
            RootQmi.Result result;

            try {
                result = RootQmi.apply(path, next);

                if (result.ok) {
                    Prefs.setMode(this, next);
                }
            } catch (Exception e) {
                result = new RootQmi.Result(
                        false,
                        -1,
                        e.getClass().getSimpleName()
                                + ": "
                                + String.valueOf(e.getMessage())
                );
            }

            final RootQmi.Result finalResult = result;

            mainHandler.post(() -> {
                try {
                    updateTile(
                            finalResult.ok,
                            next,
                            finalResult.output
                    );
                } finally {
                    busy = false;
                }
            });
        }).start();
    }

    private void updateTile(
            boolean success,
            String mode,
            String output
    ) {
        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        if (success) {
            tile.setState(Tile.STATE_ACTIVE);

            if ("0x4".equals(mode) || "4".equals(mode)) {
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
                    "Band lock failed: " + output
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

        if ("0x4".equals(mode) || "4".equals(mode)) {
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
