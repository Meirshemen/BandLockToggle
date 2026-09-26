package com.meir.bandlocktoggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RootQmi {

    static final String DEFAULT_PATH =
            "/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool";

    static Result run(String path, String args) {
        Process p = null;
        StringBuilder out = new StringBuilder();

        try {
            /*
             * Run exactly like the working Termux command:
             *
             * su -c '/path/to/qmi_tool args'
             *
             * Everything after -c is one shell command.
             */
            String command = shellQuote(path);

            if (args != null && !args.trim().isEmpty()) {
                command += " " + args;
            }

            p = new ProcessBuilder(
                    "su",
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

            if (code == 0) {
                return new Result(true, code, output);
            }

            return new Result(
                    false,
                    code,
                    output.isEmpty()
                            ? "Command failed (exit code " + code + ")"
                            : output
            );

        } catch (Exception e) {

            return new Result(
                    false,
                    -1,
                    e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );

        } finally {

            if (p != null) {
                p.destroy();
            }
        }
    }

    static Result apply(String path, String mode) {

        String command;

        if ("ALL".equals(mode)) {
            command = "unlock";
        } else {
            command = "band_lock " + mode;
        }

        return run(path, command);
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
