package grails.async.events.bus

import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry

/**
 * An EventBus is both an {@link EventEmitter} and a {@lnk EventRegistry}
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventBus extends EventEmitter, EventRegistry {

    /**
     * @return Whether the event bus is active or has been shutdown
     */
    boolean isActive()
}
