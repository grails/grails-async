package grails.async.events.subscriber

import grails.async.events.Event
import grails.async.events.trigger.EventTrigger

/**
 * Represents a registered event
 */
interface Subscription {

    /**
     * @return Cancel the registration
     */
    Subscription cancel()
    /**
     * @return Whether it is cancelled
     */
    boolean isCancelled()

    /**
     * @return Builds a trigger
     */
    EventTrigger buildTrigger(Event event)

    /**
     * @return Builds a trigger
     */
    EventTrigger buildTrigger(Event event, Closure reply)
}