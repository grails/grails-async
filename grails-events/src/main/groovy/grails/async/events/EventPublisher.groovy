package grails.async.events

import grails.async.events.bus.EventBus
import grails.async.events.bus.EventBusFactory
import grails.async.events.emitter.EventEmitter
import groovy.transform.CompileStatic

import javax.annotation.Resource

/**
 * A trait that can be implemented to make a class an event publisher
 *
 * @since 3.3
 * @author Graeme Rocher
 */
@CompileStatic
trait EventPublisher implements EventEmitter {

    private EventBus eventBus

    @Resource
    void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus
    }

    EventBus getEventBus() {
        if(this.eventBus == null) {
            this.eventBus = EventBusFactory.create()
        }
        return this.eventBus
    }

    @Override
    EventEmitter notify(CharSequence eventId, Object... data) {
        assertEventBus()
        return getEventBus().notify(eventId, data)
    }


    @Override
    EventEmitter notify(Event event) {
        assertEventBus()
        return getEventBus().notify(event)
    }

    @Override
    EventEmitter publish(CharSequence eventId, Object... data) {
        assertEventBus()
        return getEventBus().publish(eventId, data)
    }

    @Override
    EventEmitter publish(Event event) {
        assertEventBus()
        return getEventBus().publish(event)
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        assertEventBus()
        return eventBus.sendAndReceive(event, reply)
    }

    @Override
    EventEmitter sendAndReceive(CharSequence eventId, Object data, Closure reply) {
        assertEventBus()
        return getEventBus().sendAndReceive(eventId, data, reply)
    }

    private void assertEventBus() {
        if (eventBus == null) {
            throw new IllegalStateException("No EventBus configured. Please set the 'eventBus' property")
        }
    }

}