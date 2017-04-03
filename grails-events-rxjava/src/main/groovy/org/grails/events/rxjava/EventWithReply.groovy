package org.grails.events.rxjava

import grails.events.Event
import groovy.transform.CompileStatic

/**
 * An event with a reply
 *
 * @since 3.3
 * @author Graeme Rocher
 */
@CompileStatic
class EventWithReply {
    final Event event
    final Closure reply

    EventWithReply(Event event, Closure reply) {
        this.event = event
        this.reply = reply
    }
}
