package com.meir.bandlocktoggle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

final class RootQmi {

    static final String DEFAULT_PATH =
            "/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool";

    private static final String LOG_PATH =
            "/data/user/0/com.meir.bandlocktoggle/files/bandlock-debug.txt";

    static Result run(String path, String args) {
        Process p = null;
        StringBuilder out = new StringBuilder();
        String command = "";

        try {
            if (path == null || path.trim().isEmpty()) {
                path = DEFAULT_PATH;
            }

            command = shellQuote(path);

            if (args != null && !args.trim().isEmpty()) {
                command += " " + args;
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

            logResult(command, code, output);

            return new Result(
                    code == 0,
                    code,
                    output.isEmpty()
                            ? "exit code " + code
                            : output
            );

        } catch (Exception e) {
            String error = e.getClass().getSimpleName()
                    + ": " + e.getMessage();

            logResult(command, -1, error);

            return new Result(
                    false,
                    -1,
                    error
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
                "ALL".equals(mode)
                        ? "unlock"
                        : "band_lock " + mode
        );
    }

    private static String shellQuote(String value) {
        if (value == null || value.isEmpty()) {
            return "''";
        }

        return "'" + value.replace("'", "'\\''") + "'";
    }

    private static synchronized void logResult(
            String command,
            int code,
            String output) {

        try {
            File file = new File(LOG_PATH);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write("COMMAND: " + command + "\n");
                writer.write("EXIT: " + code + "\n");
                writer.write("OUTPUT: " + output + "\n");
                writer.write("---\n");
            }

        } catch (Exception ignored) {
            // Logging must not prevent the band-lock operation.
        }
    }

    static final class Result {
        final boolean ok;
        final int code;
        final String output;

        Result(int codeOk, int code, String output) {
            this.ok = codeOk == 0;
            this.code = code;
            this.output = output;
        }

        Result(boolean ok, int code, String output) {
            this.ok = ok;
            this.code = code;
            this.output = output;
        }
    }
}
