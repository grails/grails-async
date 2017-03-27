package grails.async.events.transform

/**
 * Transforms a method into listener for an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@interface Listener {
    /**
     * @return A closure that returns the event to listen for
     */
    Class value()
}