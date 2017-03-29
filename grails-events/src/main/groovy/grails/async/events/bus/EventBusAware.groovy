package grails.async.events.bus

import groovy.transform.CompileStatic
import javax.annotation.Resource

/**
 * Trait for classes aware of the event bus
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
trait EventBusAware {

    private EventBus eventBus

    /**
     * Sets the target event bus to use
     *
     * @param eventBus The event bus
     */
    @Resource
    void setTargetEventBus(EventBus eventBus) {
        this.eventBus = eventBus
    }

    /**
     * @return Retrieves the event bus
     */
    EventBus getEventBus() {
        if(this.eventBus == null) {
            this.eventBus = new EventBusFactory().create()
        }
        return this.eventBus
    }
}