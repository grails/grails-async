package grails.async.events.subscriber

/**
 * Functional interface for event listeners
 *
 * @author Graeme Rocher
 * @since 3.3
 */
interface EventSubscriber<T, R> {

    /**
     * Calls the event subscriber
     *
     * @param arg The argument
     * @return The result
     */
    R call(T arg)
}