package com.khazhimetov.library.service;

import com.khazhimetov.library.util.MySqlBackupUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class BackupService implements InitializingBean {

    @Value("${backup.enabled:true}")
    private boolean enabled;

    @Value("${backup.dir}")
    private String backupDir;

    @Value("${backup.retentionDays:14}")
    private int retentionDays;

    @Value("${backup.mysqldumpPath:mysqldump}")
    private String mysqldumpPath;

    // JDBC из application.properties
    @Value("${db.url}")
    private String jdbcUrl;
    @Value("${db.username}")
    private String dbUser;
    @Value("${db.password}")
    private String dbPass;

    private MySqlBackupUtil util;

    @Override
    public void afterPropertiesSet() {
        this.util = new MySqlBackupUtil(mysqldumpPath, jdbcUrl, dbUser, dbPass, backupDir);
    }

    /** Плановый бэкап по cron (значение берётся из application.properties) */
    @Scheduled(cron = "${backup.schedule}")
    public void scheduledBackup() {
        if (!enabled) return;
        try {
            Path p = util.runBackup();
            util.rotate(retentionDays);
            // можно добавить логер, если используешь
        } catch (Exception e) {
            // залогируй по-своему
            e.printStackTrace();
        }
    }

    /** Ручной запуск (из контроллера) */
    public Path runNow() throws Exception {
        Path p = util.runBackup();
        util.rotate(retentionDays);
        return p;
    }
}
