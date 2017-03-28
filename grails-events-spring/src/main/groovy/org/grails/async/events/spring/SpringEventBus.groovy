package org.grails.async.events.spring

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.subscriber.EventSubscriber
import grails.async.events.subscriber.Subscription
import grails.async.events.subscriber.Subjects
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.async.events.bus.AbstractEventBus
import org.grails.async.events.registry.ClosureSubscription
import org.grails.async.events.registry.EventSubscriberSubscription
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

    SpringEventBus(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext
        applicationContext.addApplicationListener(new GenericApplicationListenerAdapter(
                new EventBusListener(subscriptions)
        ) )
    }

    @Override
    protected AbstractEventBus.NotificationTrigger buildNotificationTrigger(Event event, Collection<Subscription> eventSubscriptions, Closure reply) {

        ConfigurableApplicationContext applicationContext = this.applicationContext
        return new AbstractEventBus.NotificationTrigger(event, eventSubscriptions, reply) {

            @Override
            void run() {
                applicationContext.publishEvent(new SpringEventBusEvent(event, reply))
            }
        }
    }

    @Slf4j
    private static class EventBusListener implements ApplicationListener<SpringEventBusEvent> {
        final Map<CharSequence, Collection<Subscription>> registrations

        EventBusListener(Map<CharSequence, Collection<Subscription>> registrations) {
            this.registrations = registrations
        }

        @Override
        @CompileDynamic
        void onApplicationEvent(SpringEventBusEvent event) {
            Event e = event.source
            Closure reply = event.replyTo
            for(reg in registrations.get(e.id)) {
                reg.buildTrigger(e, reply)
                    .proceed()
            }
        }
    }

}
