package org.grails.events.bus.spring

import grails.events.bus.EventBus
import grails.events.bus.EventBusBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.events.bus.ExecutorEventBus
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor

import java.util.concurrent.ExecutorService

/**
 * Factory bean for usage within Spring
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@Slf4j
class EventBusFactoryBean extends EventBusBuilder implements FactoryBean<EventBus>, InitializingBean {

    @Autowired(required = false)
    @Qualifier("taskExecutor")
    Object springTaskExecutor

    @Autowired(required = false)
    @Qualifier("grailsPromiseFactory")
    Object promiseFactory

    EventBus eventBus

    @Override
    EventBus getObject() throws Exception {
        return eventBus
    }

    @Override
    Class<?> getObjectType() {
        return EventBus
    }

    @Override
    boolean isSingleton() {
        return true
    }

    @Override
    void afterPropertiesSet() throws Exception {
        this.eventBus = super.build()
    }

    @Override
    protected EventBus createDefaultEventBus() {
        if(springTaskExecutor instanceof AsyncTaskExecutor) {
            log.debug("Creating event bus from Spring task executor {}", springTaskExecutor)
            return new ExecutorEventBus((AsyncTaskExecutor)springTaskExecutor)
        }
        else if(promiseFactory instanceof ExecutorService) {
            log.debug("Creating event bus from PromiseFactory {}", promiseFactory)
            return new ExecutorEventBus((ExecutorService)promiseFactory)
        }
        else {
            return super.createDefaultEventBus()
        }
    }
}
