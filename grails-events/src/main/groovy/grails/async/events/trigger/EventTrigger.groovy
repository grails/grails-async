package grails.async.events.trigger

import grails.async.events.Event
import grails.async.events.subscriber.Subscriber

/**
 * Encapsulates the execution of an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventTrigger<T> {

    /**
     * @return The event being triggered
     */
    Event<T> getEvent()

    /**
     * @return The event listener
     */
    Subscriber getSubscriber()

    /**
     * @return Proceed to trigger the event
     */
    Object proceed()
}