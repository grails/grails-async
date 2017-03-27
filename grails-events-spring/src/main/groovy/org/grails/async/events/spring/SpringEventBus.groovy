package org.grails.async.events.spring

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import grails.async.events.registry.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.async.events.bus.AbstractEventBus
import org.grails.async.events.DefaultSubscription
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.GenericApplicationListenerAdapter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An event bus that uses the Spring Event Publisher
 *
 * @since 3.3
 * @author Graeme Rocher
 */
@CompileStatic
class SpringEventBus extends AbstractEventBus {

    final ConfigurableApplicationContext applicationContext
    protected final Map<CharSequence, Collection<DefaultSubscription>> registrations = new ConcurrentHashMap<CharSequence, Collection<DefaultSubscription>>().withDefault {
        new ConcurrentLinkedQueue<DefaultSubscription>()
    }


    SpringEventBus(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext
        applicationContext.addApplicationListener(new GenericApplicationListenerAdapter(
                new EventBusListener(registrations)
        ) )
    }

    @Override
    Subscription on(CharSequence event, Closure listener) {
        def reg = new DefaultSubscription(event, registrations, listener)
        registrations.get(event.toString())
                     .add(reg)
        return reg
    }

    @Override
    EventRegistry unsubscribeAll(CharSequence event) {
        registrations.get(event.toString()).clear()
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        applicationContext.publishEvent(new SpringEventBusEvent(event))
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        applicationContext.publishEvent(new SpringEventBusEvent(event, reply))
        return this
    }

    private static class EventBusListener implements ApplicationListener<SpringEventBusEvent> {
        final Map<CharSequence, Collection<DefaultSubscription>> registrations

        EventBusListener(Map<CharSequence, Collection<DefaultSubscription>> registrations) {
            this.registrations = registrations
        }

        @Override
        @CompileDynamic
        void onApplicationEvent(SpringEventBusEvent event) {
            Event e = event.source
            Closure replyTo = event.replyTo
            def data = e.data

            for(reg in registrations.get(e.id)) {
                Closure listener = reg.listener
                boolean isSpread = data.getClass().isArray() && reg.argCount == ((Object[]) data).length
                if(isSpread) {
                    def result = listener.call(*data)
                    if(replyTo != null) {
                        replyTo.call(*result)
                    }
                }
                else {
                    def result = listener.call(data)
                    if(replyTo != null) {
                        replyTo.call(result)
                    }
                }
            }
        }
    }

}
