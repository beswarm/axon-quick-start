package io.axoniq.labs.chat.query.rooms;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * desc:
 *
 * @author <a href="kangqinghua#wxchina.com">beswarm</a>
 * @version 1.0.0
 * @date 2018-03-05
 */
@Aspect
@Slf4j
@Component
public class LoggingAspect {

    @Before("@annotation(org.axonframework.eventhandling.EventHandler)")
    public void log(JoinPoint point) {
        log.info("{} deals with event:{}", point.getSignature().toShortString(), point.getArgs()[0].toString());
    }
}
