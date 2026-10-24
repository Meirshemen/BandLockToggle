
package com.meir.bandlocktoggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RootQmi {

    static final String DEFAULT_PATH =
            "/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool";

    private RootQmi() {
    }

    static Result run(String path, String args) {
        Process p = null;
        StringBuilder out = new StringBuilder();

        try {
            String actualPath =
                    path == null || path.trim().isEmpty()
                            ? DEFAULT_PATH
                            : path.trim();

            String command = shellQuote(actualPath);

            if (args != null && !args.trim().isEmpty()) {
                command += " " + args.trim();
            }

            p = new ProcessBuilder(
                    "su",
                    "-mm",
                    "-c",
                    command
            )
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

            boolean ok = code == 0;

            if (output.isEmpty()) {
                output = "exit code " + code;
            }

            return new Result(ok, code, output);

        } catch (Exception e) {

            return new Result(
                    false,
                    -1,
                    e.getClass().getSimpleName()
                            + ": "
                            + String.valueOf(e.getMessage())
            );

        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    static Result apply(String path, String mode) {
        if ("ALL".equals(mode)) {
            return run(path, "unlock");
        }

        // The qmi_tool command verified in Termux is:
        // qmi_tool band_lock 4
        if ("0x4".equals(mode) || "4".equals(mode)) {
            return run(path, "band_lock 4");
        }

        return new Result(
                false,
                -1,
                "Unsupported band mode: " + mode
        );
    }

    private static String shellQuote(String value) {
        if (value == null || value.isEmpty()) {
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
