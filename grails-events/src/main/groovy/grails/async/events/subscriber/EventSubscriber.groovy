package grails.async.events.subscriber

import grails.async.events.Event

/**
 * An interface for subscribers that accept the Event as an argument
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventSubscriber<T> extends Subscriber<Event, T> {
}
