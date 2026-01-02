package com.mok.ddd.infrastructure.log.listener;

import com.mok.ddd.domain.sys.model.OperLog;
import com.mok.ddd.domain.sys.repository.OperLogRepository;
import com.mok.ddd.infrastructure.log.event.OperLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogListener {

    private final OperLogRepository operLogRepository;

    @Async
    @EventListener
    public void recordOperLog(OperLogEvent event) {
        OperLog operLog = event.getOperLog();
        try {
            operLogRepository.save(operLog);
            log.info("Remote operation log recorded: {}", operLog.getTitle());
        } catch (Exception e) {
            log.error("Failed to record operation log", e);
        }
    }
}
