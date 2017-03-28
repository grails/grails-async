package org.grails.async.events.bus.spring

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.trigger.EventTrigger
import grails.async.events.emitter.EventEmitter
import grails.async.events.subscriber.Subjects
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileStatic
import org.grails.async.events.registry.ClosureSubscription
import org.grails.async.events.bus.AbstractEventBus
import org.grails.async.events.registry.EventSubscriberSubscription
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An event bus that uses Spring's {@link TaskExecutor} interface
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class TaskExecuterEventBus extends AbstractEventBus {
    protected final Map<CharSequence, Collection<Subscription>> subscriptions = new ConcurrentHashMap<CharSequence, Collection<Subscription>>().withDefault {
        new ConcurrentLinkedQueue<ClosureSubscription>()
    }
    final TaskExecutor taskExecutor

    TaskExecuterEventBus(TaskExecutor taskExecutor = new SyncTaskExecutor()) {
        this.taskExecutor = taskExecutor
    }

    @Override
    Subscription on(CharSequence event, Closure subscriber) {
        return new ClosureSubscription(event, subscriptions, subscriber)
    }

    @Override
    Subscription subscribe(CharSequence event, EventSubscriber subscriber) {
        return new EventSubscriberSubscription(event, subscriptions, subscriber)
    }

    @Override
    Subjects unsubscribeAll(CharSequence event) {
        subscriptions.get(event).clear()
        return this
    }


    @Override
    EventEmitter notify(Event event) {
        String eventId = event.id
        if(subscriptions.containsKey(eventId))  {
            taskExecutor.execute {
                for(reg in subscriptions.get(event.id)) {
                    taskExecutor.execute {
                        EventTrigger trigger = reg.buildTrigger(event)
                        trigger.proceed()
                    }
                }
            }
        }
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        if(event == null) throw new IllegalArgumentException("Argument [event] cannot be null")
        if(event == null) throw new IllegalArgumentException("Argument [reply] cannot be null")
        String eventId = event.id
        if(subscriptions.containsKey(eventId)) {
            taskExecutor.execute {

                for(reg in subscriptions.get(eventId)) {
                    taskExecutor.execute {
                        EventTrigger trigger = reg.buildTrigger(event, reply)
                        trigger.proceed()
                    }
                }
            }
        }
        return this
    }

}
