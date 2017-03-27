package grails.async.events.registry

/**
 * An event registry
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventRegistry {

    /**
     * Listen for an event
     *
     * @param event The event
     * @param listener The listener
     */
    Subscription on(CharSequence event, Closure listener)

    /**
     * Listen for an event
     *
     * @param event The event
     * @param listener The listener
     */
    Subscription subscribe(CharSequence event, Closure listener)

    /**
     * Clear all listeners for the given event
     *
     * @param event The even id
     *
     * @return The registry
     */
    EventRegistry unsubscribeAll(CharSequence event)


}