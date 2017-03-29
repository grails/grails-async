package org.grails.async.events

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.subscriber.Subscriber
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Simple trigger for an Subscriber
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@Slf4j
class EventSubscriberTrigger implements EventTrigger {
    final Event event
    final Subscriber subscriber

    EventSubscriberTrigger(Event event, Subscriber subscriber) {
        this.event = event
        this.subscriber = subscriber
    }

    @Override
    Object proceed() {
        try {
            if(subscriber instanceof EventSubscriber) {
                return subscriber.call(event)
            }
            else {
                return subscriber.call(event.data)
            }
        } catch (Throwable e) {
            log.error("Error triggering event [$event.id] for subscriber [${subscriber}]: $e.message", e)
            throw e
        }
    }
}
