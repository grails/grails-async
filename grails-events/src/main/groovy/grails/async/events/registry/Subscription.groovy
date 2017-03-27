package grails.async.events.registry

/**
 * Represents a registered event
 */
interface Subscription {

    /**
     * @return Cancel the registration
     */
    Subscription cancel()
    /**
     * @return Whether it is cancelled
     */
    boolean isCancelled()
}