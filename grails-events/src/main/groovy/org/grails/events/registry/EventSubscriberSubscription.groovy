package org.grails.events.registry

import grails.events.Event
import grails.events.subscriber.Subscriber
import grails.events.trigger.EventTrigger
import grails.events.subscriber.Subscription
import groovy.transform.CompileStatic
import org.grails.events.EventSubscriberTrigger

/**
 * An event subscription for an {@link Subscriber}
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class EventSubscriberSubscription extends AbstractSubscription {
    final Subscriber subscriber

    EventSubscriberSubscription(CharSequence eventKey, Map<CharSequence, Collection<Subscription>> subscriptions, Subscriber subscriber) {
        super(eventKey, subscriptions)
        this.subscriber = subscriber
    }

    @Override
    EventTrigger buildTrigger(Event event) {
        return new EventSubscriberTrigger(event, subscriber)
    }

    @Override
    EventTrigger buildTrigger(Event event, Closure reply) {
        return new EventSubscriberTrigger(event, subscriber, reply)
    }
}
