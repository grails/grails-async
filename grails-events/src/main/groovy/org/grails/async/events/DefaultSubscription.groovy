package org.grails.async.events

import grails.async.events.registry.Subscription
import groovy.transform.CompileStatic

@CompileStatic
class DefaultSubscription implements Subscription {

    final CharSequence eventKey
    private final Map<CharSequence, Collection<DefaultSubscription>> registrations
    final Closure listener
    final int argCount

    DefaultSubscription(CharSequence eventKey, Map<CharSequence, Collection<DefaultSubscription>> registrations, Closure listener) {
        this.eventKey = eventKey
        this.registrations = registrations
        this.listener = listener
        this.argCount = listener.parameterTypes.length
        this.registrations.get(eventKey).add(this)
    }

    @Override
    Subscription cancel() {
        registrations.get(eventKey).remove(this)
        return this
    }

    @Override
    boolean isCancelled() {
        return !registrations.get(eventKey).contains(this)
    }
}