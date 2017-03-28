package org.grails.async.events.bus.spring

import grails.async.events.Event
import grails.async.events.subscriber.Subscription
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileStatic
import org.grails.async.events.bus.AbstractEventBus
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

/**
 * An event bus that uses Spring's {@link TaskExecutor} interface
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class TaskExecutorEventBus extends AbstractEventBus {
    final TaskExecutor taskExecutor

    TaskExecutorEventBus(TaskExecutor taskExecutor = new SyncTaskExecutor()) {
        this.taskExecutor = taskExecutor
    }

    @Override
    protected NotificationTrigger buildNotificationTrigger(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {
        TaskExecutor taskExecutor = this.taskExecutor
        return new AbstractEventBus.NotificationTrigger(event, eventSubscriptions, reply) {
            @Override
            void run() {
                taskExecutor.execute {
                    for (Subscription subscription in eventSubscriptions) {
                        taskExecutor.execute {
                            EventTrigger trigger = subscription.buildTrigger(event, reply)
                            trigger.proceed()
                        }
                    }
                }
            }
        }
    }
}
