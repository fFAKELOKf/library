package com.khazhimetov.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/** Утилита: вызывает mysqldump и пишет .sql.gz */
public class MySqlBackupUtil {

    private final String mysqldumpPath;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Path backupDir;

    public MySqlBackupUtil(String mysqldumpPath, String jdbcUrl, String dbUser, String dbPassword, String backupDir) {
        this.mysqldumpPath = mysqldumpPath;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.backupDir = Paths.get(backupDir);
    }

    /** Выполнить резервное копирование. Возвращает путь к файлу. */
    public Path runBackup() throws IOException, InterruptedException {
        Files.createDirectories(backupDir);

        ParsedJdbc pj = parseMySqlJdbcUrl(jdbcUrl);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path out = backupDir.resolve(ts + "_" + pj.db + ".sql.gz");

        List<String> cmd = List.of(
                mysqldumpPath,
                "-h", pj.host,
                "-P", String.valueOf(pj.port),
                "-u", dbUser,
                "-p" + dbPassword,
                "--routines", "--triggers", "--events",
                "--single-transaction",
                pj.db
        );

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // поток stdout mysqldump -> gzip файл
        try (InputStream in = p.getInputStream();
             GZIPOutputStream gz = new GZIPOutputStream(Files.newOutputStream(out, StandardOpenOption.CREATE_NEW))) {
            in.transferTo(gz);
        }

        int code = p.waitFor();
        if (code != 0) {
            Files.deleteIfExists(out);
            throw new IllegalStateException("mysqldump exited with code " + code);
        }
        return out;
    }

    /** Удалить бэкапы старше retentionDays. */
    public void rotate(int retentionDays) throws IOException {
        long cutoff = System.currentTimeMillis() - retentionDays * 24L * 60 * 60 * 1000;
        try (var stream = Files.list(backupDir)) {
            stream.filter(f -> f.getFileName().toString().endsWith(".sql.gz"))
                    .filter(f -> {
                        try { return Files.getLastModifiedTime(f).toMillis() < cutoff; }
                        catch (IOException e) { return false; }
                    })
                    .forEach(f -> {
                        try { Files.deleteIfExists(f); }
                        catch (IOException ignored) {}
                    });
        }
    }

    // --- helpers ---

    private static final Pattern MYSQL_JDBC =
            Pattern.compile("^jdbc:mysql://(?<host>[^:/?]+)(:(?<port>\\d+))?/(?<db>[^?]+).*$", Pattern.CASE_INSENSITIVE);

    private static ParsedJdbc parseMySqlJdbcUrl(String url) {
        Matcher m = MYSQL_JDBC.matcher(url);
        if (!m.matches()) throw new IllegalArgumentException("Unsupported JDBC url: " + url);
        String host = m.group("host");
        String portStr = m.group("port");
        String db = m.group("db");
        int port = (portStr == null || portStr.isBlank()) ? 3306 : Integer.parseInt(portStr);
        return new ParsedJdbc(host, port, db);
    }

    private static final class ParsedJdbc {
        final String host; final int port; final String db;
        ParsedJdbc(String host, int port, String db) { this.host = host; this.port = port; this.db = db; }
    }
}
