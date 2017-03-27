package org.grails.async.events.gpars

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import grails.async.events.registry.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.actor.Actor
import groovyx.gpars.agent.Agent
import org.grails.async.events.bus.AbstractEventBus
import org.grails.async.events.DefaultSubscription

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
    final Agent<Map<CharSequence, Collection<DefaultSubscription>>> registrations

    @CompileDynamic
    ActorEventBus() {
        actor = actor {
            loop {
                react() { Event msg ->
                    registrations.sendAndWait { Map<Object, Collection<DefaultSubscription>> all ->
                        for(reg in all.get(msg.id)) {
                            Closure listener = reg.listener
                            def data = msg.data
                            try {
                                def result
                                if(data.getClass().isArray() && reg.argCount == ((Object[])data).length) {
                                    result = listener.call(*data)
                                }
                                else {
                                    result = listener.call(data)
                                }
                                replyIfExists(result)
                            } catch (Throwable e) {
                                replyIfExists(e)
                            }

                        }

                    }
                }

            }
        }
        registrations = new Agent<>( new ConcurrentHashMap<CharSequence, Collection<DefaultSubscription>>().withDefault {
            new ConcurrentLinkedQueue<DefaultSubscription>()
        })
    }
    @Override
    Subscription on(CharSequence event, Closure listener) {
        DefaultSubscription registration
        registrations.sendAndWait {  Map<CharSequence, Collection<DefaultSubscription>> all ->
            registration = new DefaultSubscription(event, all, listener)

        }
        return registration
    }

    @Override
    EventRegistry unsubscribeAll(CharSequence event) {
        registrations.sendAndWait { Map<Object, Collection<DefaultSubscription>> all ->
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

}
