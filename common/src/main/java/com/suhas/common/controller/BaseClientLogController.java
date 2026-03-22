package com.suhas.common.controller;

import com.suhas.common.dto.ClientLogDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class BaseClientLogController {
    private static final Logger logger = LoggerFactory.getLogger(BaseClientLogController.class);

    @PostMapping("/logs")
    public void recordLog(@RequestBody ClientLogDto log) {
        String logFormat = "FE_{}: {} | URL: {} | Details: {}";

        switch (log.level().toUpperCase()) {
            case "ERROR" -> logger.error(logFormat, "ERROR", log.message(), log.url(), log.details());
            case "WARN"  -> logger.warn(logFormat, "WARN", log.message(), log.url(), log.details());
            case "INFO"  -> logger.info(logFormat, "INFO", log.message(), log.url(), log.details());
            default      -> logger.debug(logFormat, "DEBUG", log.message(), log.url(), log.details());
        }
    }
}