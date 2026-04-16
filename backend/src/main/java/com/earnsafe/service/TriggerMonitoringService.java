package com.earnsafe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerMonitoringService {

    private final TriggerService triggerService;
    private final PayoutService payoutService;

    @Transactional
    @Scheduled(fixedDelayString = "${app.trigger.interval:300000}")
    public void runTriggerScan() {
        log.info("=== [AutoTrigger] Starting scheduled OpenWeather scan at {} ===", LocalDateTime.now());
        int created = triggerService.scanAndEvaluateAllCities().size();
        int retried = payoutService.retryPendingPayouts();
        log.info("=== [AutoTrigger] Scan complete. Auto-created {} claim(s), reconciled {} payout(s). ===", created, retried);
    }
}
