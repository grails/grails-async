package org.grails.events

import grails.events.Event
import grails.events.subscriber.EventSubscriber
import grails.events.subscriber.Subscriber
import grails.events.trigger.EventTrigger
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
    final Closure reply

    EventSubscriberTrigger(Event event, Subscriber subscriber, Closure reply = null) {
        this.event = event
        this.subscriber = subscriber
        this.reply = reply
    }

    @Override
    Object proceed() {
        try {
            def result
            if(subscriber instanceof EventSubscriber) {
                result = subscriber.call(event)
            }
            else {
                result = subscriber.call(event.data)
            }
            if(reply != null) {
                return reply.call(result)
            }
            return result
        } catch (Throwable e) {
            if(reply != null && reply.parameterTypes && reply.parameterTypes[0].isInstance(e)) {
                reply.call(e)
            }
            else {
                log.error("Error triggering event [$event.id] for subscriber [${subscriber}]: $e.message", e)
                throw e
            }
        }
    }
}
