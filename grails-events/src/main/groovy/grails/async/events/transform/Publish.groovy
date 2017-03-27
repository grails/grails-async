package grails.async.events.transform


/**
 * Transforms a method so the return value is emitted as an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@interface Publish {
    /**
     * @return A closure that returns the event id to publish to
     */
    Class value()
}