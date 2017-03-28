package org.grails.async.events

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Triggers an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class ClosureEventTrigger<T> implements EventTrigger<T> {

    final Event<T> event
    final Closure subscriberClosure
    final Closure reply
    private final T data
    private final int argCount
    private final boolean eventArg

    ClosureEventTrigger(Event<T> event, Closure subscriber, Closure reply = null) {
        this.event = event
        this.subscriberClosure = subscriber
        this.reply = reply
        this.data = event.data
        Closure closure = (Closure)subscriber
        Class[] parameterTypes = closure.parameterTypes
        this.argCount = parameterTypes.length
        this.eventArg = argCount == 1 && Event.isAssignableFrom(parameterTypes[0])
    }

    @Override
    EventSubscriber getSubscriber() {
        return subscriberClosure as EventSubscriber
    }

    @Override
    Object proceed() {
        int dataLength = data.getClass().isArray() ? ((Object[])data).length : 1

        boolean isSpread = !eventArg && dataLength > 1 && argCount == dataLength
        try {
            def result
            if(isSpread) {
                result = callSpread(subscriberClosure, data)
            }
            else {
                if(eventArg) {
                    result = subscriberClosure.call(event)
                }
                else {
                    result = subscriberClosure.call(data)
                }
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
                throw e
            }
        }
    }


    @CompileDynamic
    protected Object callSpread(Closure listener, Object data) {
        listener.call(*data)
    }
}
