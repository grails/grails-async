package org.grails.async.events.bus

import grails.async.events.Event
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileStatic

import java.util.concurrent.Callable

/**
 * A default synchronous event bus for testing
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class SynchronousEventBus extends AbstractEventBus {

    @Override
    protected Callable buildNotificationCallable(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {
        return {
            for(Subscription subscription in eventSubscriptions) {
                subscription.buildTrigger(event, reply)
                        .proceed()
            }
        }
    }
}
