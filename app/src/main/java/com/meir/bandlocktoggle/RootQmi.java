package com.meir.bandlocktoggle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RootQmi {
    static final String DEFAULT_PATH = "/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool";

    static Result run(String path, String args) {
        String command = "'" + path.replace("'", "'\\''") + "' " + args;
        Process p = null;
        StringBuilder out = new StringBuilder();
        try {
            p = new ProcessBuilder("su", "-c", command)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) out.append(line).append('\n');
            }
            int code = p.waitFor();
            return new Result(code == 0, code, out.toString().trim());
        } catch (Exception e) {
            return new Result(false, -1, e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            if (p != null) p.destroy();
        }
    }

    static boolean apply(String path, String mode) {
        return run(path, mode.equals("ALL") ? "unlock" : "band_lock " + mode).ok;
    }

    static final class Result {
        final boolean ok; final int code; final String output;
        Result(boolean ok, int code, String output) { this.ok = ok; this.code = code; this.output = output; }
    }
}
