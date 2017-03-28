package org.grails.async.events.bus

import grails.async.events.Event
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileStatic

/**
 * A default synchronous event bus for testing
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class SynchronousEventBus extends AbstractEventBus {

    @Override
    protected NotificationTrigger buildNotificationTrigger(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {
        return new AbstractEventBus.NotificationTrigger(event, eventSubscriptions, reply) {
            @Override
            void run() {
                for(Subscription subscription in eventSubscriptions) {
                    subscription.buildTrigger(event, reply)
                            .proceed()
                }
            }
        }
    }
}
