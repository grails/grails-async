package org.grails.events.gorm

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.springframework.util.ReflectionUtils

/**
 * Marks a class as a synchronous listener of GORM events
 *
 * @author Graeme Rocher
 * @since 3.3
 */
trait GormAnnotatedListener extends GormAnnotatedSubscriber {

    /**
     * Whether the listener supports the given event
     * @param event The event
     * @return True if it does
     */
    boolean supports(AbstractPersistenceEvent event) {
        getSubscribedEvents().contains(event.getClass())
    }
    /**
     * Dispatch the event to this listener
     * @param event
     */
    void dispatch(AbstractPersistenceEvent event) {
        for(method in getSubscribedMethods()) {
            if(method.parameterTypes[0].isInstance(event)) {
                ReflectionUtils.invokeMethod(method, this, event)
            }
        }
    }
}
