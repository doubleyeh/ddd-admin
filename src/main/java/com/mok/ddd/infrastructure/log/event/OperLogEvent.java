package com.mok.ddd.infrastructure.log.event;

import com.mok.ddd.domain.sys.model.OperLog;
import org.springframework.context.ApplicationEvent;

public class OperLogEvent extends ApplicationEvent {
    public OperLogEvent(OperLog operLog) {
        super(operLog);
    }

    public OperLog getOperLog() {
        return (OperLog) getSource();
    }
}
