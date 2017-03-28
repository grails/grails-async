package grails.async.events

import grails.async.events.bus.EventBus
import grails.async.events.bus.EventBusFactory
import grails.async.events.emitter.EventEmitter
import groovy.transform.CompileStatic
import org.springframework.transaction.event.TransactionPhase

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
    void setTargetEventBus(EventBus eventBus) {
        this.eventBus = eventBus
    }

    /**
     * @see {@link EventEmitter#notify(java.lang.CharSequence, java.lang.Object[])}
     */
    @Override
    EventEmitter notify(CharSequence eventId, Object... data) {
        assertEventBus()
        return getEventBus().notify(eventId, data)
    }

    /**
     * @see {@link EventEmitter#notify(grails.async.events.Event)}
     */
    @Override
    EventEmitter notify(Event event) {
        assertEventBus()
        return getEventBus().notify(event)
    }


    /**
     * @see {@link EventEmitter#notify(grails.async.events.Event, org.springframework.transaction.event.TransactionPhase)}
     */
    @Override
    EventEmitter notify(Event event, TransactionPhase transactionPhase) {
        assertEventBus()
        return getEventBus().notify(event, transactionPhase)
    }

    /**
     * @see {@link EventEmitter#notify(grails.async.events.Event, org.springframework.transaction.event.TransactionPhase)}
     */
    @Override
    EventEmitter publish(Event event, TransactionPhase transactionPhase) {
        assertEventBus()
        return getEventBus().notify(event, transactionPhase)
    }

    /**
     * @see {@link EventEmitter#notify(java.lang.CharSequence, java.lang.Object[])} )}
     */
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

    private EventBus getEventBus() {
        if(this.eventBus == null) {
            this.eventBus = new EventBusFactory().create()
        }
        return this.eventBus
    }

}