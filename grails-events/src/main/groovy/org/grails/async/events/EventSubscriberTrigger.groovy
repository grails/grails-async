package org.grails.async.events

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileStatic

/**
 * Simple trigger for an EventSubscriber
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class EventSubscriberTrigger implements EventTrigger {
    final Event event
    final EventSubscriber subscriber

    EventSubscriberTrigger(Event event, EventSubscriber subscriber) {
        this.event = event
        this.subscriber = subscriber
    }

    @Override
    Object proceed() {
        try {
            return subscriber.call(event.data)
        } catch (Throwable e) {
            throw e
        }
    }
}
