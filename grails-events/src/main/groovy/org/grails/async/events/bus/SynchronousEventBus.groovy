package org.grails.async.events.bus

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.trigger.EventTrigger
import grails.async.events.emitter.EventEmitter
import grails.async.events.subscriber.Subjects
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileStatic
import org.grails.async.events.registry.ClosureSubscription
import org.grails.async.events.registry.EventSubscriberSubscription

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A default synchronous event bus for testing
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class SynchronousEventBus extends AbstractEventBus {
    protected final Map<CharSequence, Collection<Subscription>> subscriptions = new ConcurrentHashMap<CharSequence, Collection<Subscription>>().withDefault {
        new ConcurrentLinkedQueue<ClosureSubscription>()
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
    EventEmitter sendAndReceive(Event event, Closure reply) {
        for(reg in subscriptions.get(event.id)) {
            EventTrigger trigger = reg.buildTrigger(event, reply)
            trigger.proceed()

        }
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        for(reg in subscriptions.get(event.id)) {
            EventTrigger trigger = reg.buildTrigger(event)
            trigger.proceed()
        }
        return this
    }

}
