package com.meir.bandlocktoggle;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        build();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Band Lock"); title.setTextSize(28); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, lp(WRAP(), 64));

        TextView hint = new TextView(this);
        hint.setText("Root required • LTE band preference via Qualcomm QMI"); hint.setTextSize(14);
        root.addView(hint, lp(WRAP(), 50));

        status = new TextView(this);
        status.setTextSize(18); status.setTextColor(Color.DKGRAY);
        status.setText("Last selected: " + label(Prefs.mode(this)));
        root.addView(status, lp(WRAP(), 56));

        String[][] modes = {
                {"B3", "0x4"},
                {"B7", "0x40"},
                {"B28", "0x8000000"},
                {"B3 + B7", "0x44"},
                {"B3 + B28", "0x8000004"},
                {"ALL BANDS", "ALL"}
        };
        for (String[] m : modes) addModeButton(root, m[0], m[1]);

        TextView pathLabel = new TextView(this);
        pathLabel.setText("qmi_tool path"); pathLabel.setTextSize(15); pathLabel.setPadding(0, 30, 0, 6);
        root.addView(pathLabel, lp(WRAP(), 34));

        EditText path = new EditText(this);
        path.setSingleLine(true); path.setText(Prefs.path(this));
        root.addView(path, lp(WRAP(), 58));

        Button save = new Button(this); save.setText("Save path");
        save.setOnClickListener(v -> { Prefs.setPath(this, path.getText().toString().trim()); Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show(); });
        root.addView(save, lp(WRAP(), 58));

        Button test = new Button(this); test.setText("Test root / modem");
        test.setOnClickListener(v -> run("test", "Test"));
        root.addView(test, lp(WRAP(), 58));

        setContentView(scroll);
    }

    private void addModeButton(LinearLayout root, String text, String value) {
        Button b = new Button(this); b.setText(text); b.setTextSize(16);
        b.setOnClickListener(v -> new Thread(() -> {
            RootQmi.Result r = RootQmi.apply(Prefs.path(this), value);
            runOnUiThread(() -> {
                if (r.ok) {
                    Prefs.setMode(this, value);
                    status.setText("Current: " + text);
                    Toast.makeText(this, "Applied: " + text, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(this, "Failed: " + r.output, Toast.LENGTH_LONG).show();
            });
        }).start());
        root.addView(b, lp(WRAP(), 64));
    }

    private void run(String args, String what) {
        new Thread(() -> {
            RootQmi.Result r = RootQmi.run(Prefs.path(this), args);
            runOnUiThread(() -> Toast.makeText(this, what + ": " + (r.ok ? "OK" : r.output), Toast.LENGTH_LONG).show());
        }).start();
    }

    private static String label(String v) {
        switch (v) { case "0x4": return "B3"; case "0x40": return "B7"; case "0x8000000": return "B28"; case "0x44": return "B3 + B7"; case "0x8000004": return "B3 + B28"; default: return "ALL"; }
    }
    private static int WRAP() { return LinearLayout.LayoutParams.MATCH_PARENT; }
    private static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
}
