package com.khazhimetov.library.controller;

import com.khazhimetov.library.service.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    // ОДИН метод обрабатывает и GET, и POST на /admin/backup/run
    @RequestMapping(value = "/run", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> run() {
        try {
            var file = backupService.runNow();
            return ResponseEntity.ok("Backup created: " + file);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Backup failed: " + e.getMessage());
        }
    }
}
