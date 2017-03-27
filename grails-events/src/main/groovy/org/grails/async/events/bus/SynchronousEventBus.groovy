package org.grails.async.events.bus

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import grails.async.events.registry.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.async.events.DefaultSubscription

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A default synchronous event bus for testing
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class SynchronousEventBus extends AbstractEventBus {
    protected final Map<CharSequence, Collection<DefaultSubscription>> registrations = new ConcurrentHashMap<CharSequence, Collection<DefaultSubscription>>().withDefault {
        new ConcurrentLinkedQueue<DefaultSubscription>()
    }

    @Override
    Subscription on(CharSequence event, Closure listener) {
        return new DefaultSubscription(event, registrations, listener)
    }

    @Override
    EventRegistry unsubscribeAll(CharSequence event) {
        registrations.get(event).clear()
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        Object data = event.data
        int dataLength = data.getClass().isArray() ? ((Object[])data).length : 1
        for(reg in registrations.get(event.id)) {
            Closure listener = reg.listener
            if(reg.argCount == dataLength) {
                reply.call(
                        callSpread(listener, data)
                )

            }
            else {
                reply.call(
                        listener.call(data)
                )

            }
        }
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        Object data = event.data
        int dataLength = data.getClass().isArray() ? ((Object[])data).length : 1
        for(reg in registrations.get(event.id)) {
            Closure listener = reg.listener
            if(dataLength > 1 && reg.argCount == dataLength) {
                callSpread(listener, data)
            }
            else {
                listener.call(data)
            }
        }
        return this
    }

    @CompileDynamic
    protected Object callSpread(Closure listener, Object data) {
        listener.call(*data)
    }
}
