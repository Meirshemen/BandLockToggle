package com.meir.bandlocktoggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RootQmi {

    static final String DEFAULT_PATH =
            "/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool";

    private static final String TERMUX_PREFIX =
            "/data/data/com.termux/files/usr";

    private static final String TERMUX_HOME =
            "/data/data/com.termux/files/home";

    private static final String TERMUX_SH =
            TERMUX_PREFIX + "/bin/sh";

    static Result run(String path, String args) {
        Process p = null;
        StringBuilder out = new StringBuilder();

        try {
            String toolCommand =
                    shellQuote(path) +
                    (args == null || args.trim().isEmpty()
                            ? ""
                            : " " + args);

            String command =
                    "export " +
                    "HOME=" + shellQuote(TERMUX_HOME) + " " +
                    "PREFIX=" + shellQuote(TERMUX_PREFIX) + " " +
                    "TMPDIR=" + shellQuote(TERMUX_PREFIX + "/tmp") + " " +
                    "PATH=" + shellQuote(
                            TERMUX_PREFIX + "/bin:"
                            + TERMUX_PREFIX + "/bin/applets:"
                            + "/system/bin:"
                            + "/system/xbin"
                    ) + " " +
                    "LD_LIBRARY_PATH=" + shellQuote(
                            TERMUX_PREFIX + "/lib"
                    ) + "; " +
                    "exec " + shellQuote(TERMUX_SH) +
                    " -c " + shellQuote(toolCommand);

            p = new ProcessBuilder("su", "-c", command)
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String line;

                while ((line = br.readLine()) != null) {
                    out.append(line).append('\n');
                }
            }

            int code = p.waitFor();

            String output = out.toString().trim();

            return new Result(
                    code == 0,
                    code,
                    output.isEmpty()
                            ? ("exit code " + code)
                            : output
            );

        } catch (Exception e) {

            return new Result(
                    false,
                    -1,
                    e.getClass().getSimpleName() +
                            ": " +
                            e.getMessage()
            );

        } finally {

            if (p != null) {
                p.destroy();
            }
        }
    }

    static Result apply(String path, String mode) {
        return run(
                path,
                mode.equals("ALL")
                        ? "unlock"
                        : "band_lock " + mode
        );
    }

    private static String shellQuote(String value) {
        if (value == null) {
            return "''";
        }

        return "'" + value.replace("'", "'\\''") + "'";
    }

    static final class Result {

        final boolean ok;
        final int code;
        final String output;

        Result(boolean ok, int code, String output) {
            this.ok = ok;
            this.code = code;
            this.output = output;
        }
    }
}
