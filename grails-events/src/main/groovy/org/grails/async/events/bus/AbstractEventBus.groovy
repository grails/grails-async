package org.grails.async.events.bus

import grails.async.events.Event
import grails.async.events.bus.EventBus
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.Subscription

/**
 * Abstract event bus impl
 *
 * @author Graeme Rocher
 * @since 6.1
 */
abstract class AbstractEventBus implements EventBus {

    @Override
    boolean isActive() {
        return true
    }

    @Override
    EventEmitter notify(CharSequence eventId, Object... data) {
        return notify(new Event(eventId.toString(), data.length == 1 ? data[0] : data))
    }

    @Override
    EventEmitter publish(CharSequence eventId, Object... data) {
        return notify(eventId, data)
    }

    @Override
    EventEmitter publish(Event event) {
        return notify(event)
    }

    @Override
    EventEmitter sendAndReceive(CharSequence eventId, Object data, Closure reply) {
        return sendAndReceive(new Event(eventId.toString(), data), reply)
    }

    @Override
    Subscription subscribe(CharSequence event, Closure listener) {
        return on(event, listener)
    }
}
