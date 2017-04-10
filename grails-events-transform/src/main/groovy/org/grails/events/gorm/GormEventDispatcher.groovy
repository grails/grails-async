package org.grails.events.gorm

import grails.events.bus.EventBus
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.springframework.context.ApplicationEvent

import java.beans.Introspector

@CompileStatic
class GormEventDispatcher extends AbstractPersistenceEventListener {

    protected final EventBus eventBus
    protected final Map<Class<? extends AbstractPersistenceEvent>, String> subscribedEvents

    GormEventDispatcher(EventBus eventBus, Datastore datastore, Set<Class<? extends AbstractPersistenceEvent>> subscribedEvents) {
        super(datastore)
        this.eventBus = eventBus
        Map<Class<? extends AbstractPersistenceEvent>, String> subscribedEventMap = [:]
        for(event in subscribedEvents) {
            subscribedEventMap.put(event, "gorm:"+ (Introspector.decapitalize(event.simpleName) - "Event"))
        }
        this.subscribedEvents = Collections.unmodifiableMap(subscribedEventMap)
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        Class eventType = event.getClass()
        if(subscribedEvents.containsKey(eventType)) {
            String eventName = subscribedEvents.get(eventType)
            eventBus.notify(eventName, event)
        }
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return AbstractPersistenceEvent.isAssignableFrom(aClass)
    }
}
