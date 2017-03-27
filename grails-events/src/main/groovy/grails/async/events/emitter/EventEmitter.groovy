package grails.async.events.emitter

import grails.async.events.Event

/**
 * An emitter sends events
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventEmitter {

    /**
     * Notify of an event
     *
     * @param eventId The event
     * @param data The data
     *
     * @return This emitter
     */
    EventEmitter notify(CharSequence eventId, Object...data)

    /**
     * Notify of an event
     *
     * @param event The event
     * @param data The data
     *
     * @return This emitter
     */
    EventEmitter notify(Event event)

    /**
     * Synonym for {@link #notify(grails.async.events.Event)}
     */
    EventEmitter publish(CharSequence eventId, Object...data)

    /**
     * Synonym for {@link #notify(grails.async.events.Event)}
     */
    EventEmitter publish(Event event)
    /**
     * Send and event and receive a reply
     *
     * @param event The event
     * @param reply The reply logic
     * @return This emitter
     */
    EventEmitter sendAndReceive(Event event, Closure reply)

    /**
     * Send and event and receive a reply
     *
     * @param eventId The event
     * @param reply The reply logic
     * @return This emitter
     */
    EventEmitter sendAndReceive(CharSequence eventId, Object data, Closure reply)
}