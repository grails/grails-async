package org.grails.async.events.registry

import grails.async.events.Event
import grails.async.events.trigger.EventTrigger
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileStatic
import org.grails.async.events.ClosureEventTrigger

/**
 * A subscription via a Closure
 *
 * @since 3.3
 * @author Graeme Rocher
 */
@CompileStatic
class ClosureSubscription extends AbstractSubscription {

    final Closure listener

    ClosureSubscription(CharSequence eventKey, Map<CharSequence, Collection<Subscription>> subscriptions, Closure listener) {
        super(eventKey, subscriptions)
        this.listener = listener
    }

    @Override
    EventTrigger buildTrigger(Event event, Closure reply = null) {
        return new ClosureEventTrigger(event, listener, reply)
    }
}