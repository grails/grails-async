package org.grails.events.transform

import grails.async.events.bus.EventBusAware
import grails.async.events.subscriber.MethodEventSubscriber
import grails.async.events.subscriber.MethodSubscriber
import grails.events.transform.Subscriber
import groovy.transform.CompileStatic

import javax.annotation.PostConstruct
import java.lang.reflect.Method

/**
 * Registers subscribed methods. Used by the {@link Subscriber} transformation
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
trait MethodRegisteringSubscriber extends EventBusAware {

    List<Method> getSubscribedMethods() {
        return []
    }

    @PostConstruct
    void registerMethods() {
        for(Method m in subscribedMethods) {
            Subscriber sub = m.getAnnotation(Subscriber)
            if(sub != null) {
                String eventId = sub.value()
                if(m.parameterTypes.length == 1 && m.parameterTypes[0].isAssignableFrom(grails.async.events.Event)) {
                    eventBus.subscribe(eventId, new MethodEventSubscriber(this, m))
                }
                else {
                    eventBus.subscribe(eventId, new MethodSubscriber(this, m))
                }
            }
        }
    }
}