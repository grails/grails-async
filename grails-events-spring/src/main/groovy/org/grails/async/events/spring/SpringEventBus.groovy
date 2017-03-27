package org.grails.async.events.spring

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import grails.async.events.registry.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
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

    @Slf4j
    private static class EventBusListener implements ApplicationListener<SpringEventBusEvent> {
        final Map<CharSequence, Collection<DefaultSubscription>> registrations

        EventBusListener(Map<CharSequence, Collection<DefaultSubscription>> registrations) {
            this.registrations = registrations
        }

        @Override
        @CompileDynamic
        void onApplicationEvent(SpringEventBusEvent event) {
            Event e = event.source
            Closure reply = event.replyTo
            def data = e.data

            for(reg in registrations.get(e.id)) {
                Closure listener = reg.listener
                try {
                    boolean isSpread = data.getClass().isArray() && reg.argCount == ((Object[]) data).length
                    if(isSpread) {
                        def result = listener.call(*data)
                        if(reply != null) {
                            reply.call(*result)
                        }
                    }
                    else {
                        def result = listener.call(data)
                        if(reply != null) {
                            reply.call(result)
                        }
                    }
                } catch (Throwable t) {
                    log.error("Error occurred triggering event listener for event [$event]: ${t.message}", t)
                    if(reply != null && reply.parameterTypes && reply.parameterTypes[0].isInstance(t)) {
                        reply.call(t)
                    }
                }
            }
        }
    }

}
