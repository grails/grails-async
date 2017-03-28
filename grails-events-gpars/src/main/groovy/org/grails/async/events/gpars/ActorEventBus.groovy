package org.grails.async.events.gpars

import grails.async.events.Event
import grails.async.events.subscriber.Subscription
import grails.async.events.trigger.EventTrigger
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.actor.Actor
import org.grails.async.events.bus.AbstractEventBus

import static groovyx.gpars.actor.Actors.actor

/**
 * A event bus that uses GPars actors
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@CompileStatic
class ActorEventBus extends AbstractEventBus implements Closeable {

    final Actor actor

    @CompileDynamic
    ActorEventBus() {
        actor = actor {
            loop {
                react() { Event msg ->
                    if(subscriptions.containsKey(msg.id)) {
                        Collection<Subscription> subscriptions = subscriptions.get(msg.id)
                        for(Subscription sub in subscriptions) {
                            try {
                                EventTrigger eventTrigger = sub.buildTrigger(msg)
                                replyIfExists( eventTrigger.proceed() )
                            } catch (Throwable e) {
                                replyIfExists(e)
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected AbstractEventBus.NotificationTrigger buildNotificationTrigger(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {
        Actor actor = this.actor
        return new AbstractEventBus.NotificationTrigger(event, eventSubscriptions, reply) {

            @Override
            void run() {
                if(reply != null) {
                    actor.sendAndContinue(event, reply)
                }
                else {
                    actor.send(event)
                }
            }
        }
    }

    @Override
    void close() throws IOException {
        if( actor.isActive()) {
            actor.stop()
        }
    }

    @Override
    boolean isActive() {
        return actor.isActive()
    }
}
