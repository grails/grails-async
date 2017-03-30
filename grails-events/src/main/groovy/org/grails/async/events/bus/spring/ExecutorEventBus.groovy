package org.grails.async.events.bus.spring

import grails.async.events.Event
import grails.async.events.subscriber.Subscription
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileStatic
import org.grails.async.events.bus.AbstractEventBus
import org.springframework.core.task.SyncTaskExecutor

import java.util.concurrent.Executor

/**
 * An event bus that uses an {@link Executor}
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class ExecutorEventBus extends AbstractEventBus {
    final Executor executor

    ExecutorEventBus(Executor executor = new SyncTaskExecutor()) {
        this.executor = executor
    }

    @Override
    protected NotificationTrigger buildNotificationTrigger(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {
        Executor executor = this.executor
        return new AbstractEventBus.NotificationTrigger(event, eventSubscriptions, reply) {
            @Override
            void run() {
                executor.execute {
                    for (Subscription subscription in eventSubscriptions) {
                        executor.execute {
                            EventTrigger trigger = subscription.buildTrigger(event, reply)
                            trigger.proceed()
                        }
                    }
                }
            }
        }
    }
}
