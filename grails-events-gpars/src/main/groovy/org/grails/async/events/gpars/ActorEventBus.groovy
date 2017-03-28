package org.grails.async.events.gpars

import grails.async.events.Event
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.trigger.EventTrigger
import grails.async.events.emitter.EventEmitter
import grails.async.events.subscriber.Subjects
import grails.async.events.subscriber.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.actor.Actor
import groovyx.gpars.agent.Agent
import org.grails.async.events.bus.AbstractEventBus
import org.grails.async.events.registry.ClosureSubscription
import org.grails.async.events.registry.EventSubscriberSubscription

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

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
    final Agent<Map<CharSequence, Collection<Subscription>>> registrations

    @CompileDynamic
    ActorEventBus() {
        actor = actor {
            loop {
                react() { Event msg ->
                    registrations.sendAndWait { Map<Object, Collection<ClosureSubscription>> all ->
                        for(reg in all.get(msg.id)) {
                            try {
                                EventTrigger eventTrigger = reg.buildTrigger(msg)
                                replyIfExists( eventTrigger.proceed() )
                            } catch (Throwable e) {
                                replyIfExists(e)
                            }
                        }
                    }
                }
            }
        }
        registrations = new Agent<>( new ConcurrentHashMap<CharSequence, Collection<ClosureSubscription>>().withDefault {
            new ConcurrentLinkedQueue<ClosureSubscription>()
        })
    }
    @Override
    Subscription on(CharSequence event, Closure subscriber) {
        ClosureSubscription registration
        registrations.sendAndWait {  Map<CharSequence, Collection<Subscription>> all ->
            registration = new ClosureSubscription(event, all, subscriber)

        }
        return registration
    }

    @Override
    Subscription subscribe(CharSequence event, EventSubscriber subscriber) {
        EventSubscriberSubscription registration
        registrations.sendAndWait {  Map<CharSequence, Collection<Subscription>> all ->
            registration = new EventSubscriberSubscription(event, all, subscriber)

        }
        return registration
    }

    @Override
    Subjects unsubscribeAll(CharSequence event) {
        registrations.sendAndWait { Map<Object, Collection<ClosureSubscription>> all ->
            all.get(event).clear()

        }
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        actor.send(event)
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        actor.sendAndContinue(event, reply)
        return this
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
