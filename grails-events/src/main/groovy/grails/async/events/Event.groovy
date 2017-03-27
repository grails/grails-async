package grails.async.events

import groovy.transform.CompileStatic

/**
 * Wraps an event
 *
 * @since 3.3
 * @author Graeme Rocher
 */
@CompileStatic
class Event<T> {
    final String id
    final T data
    final Map<String, Object> parameters

    Event(String id, T data) {
        this.id = id
        this.data = data
        this.parameters = Collections.emptyMap()
    }

    Event(String id, Map<String, Object> parameters, T data) {
        this.id = id
        this.data = data
        this.parameters = Collections.unmodifiableMap(parameters)
    }

    /**
     * Wrap the given object with an {@link Event}.
     *
     * @param obj
     *     The object to wrap.
     *
     * @return The new {@link Event}.
     */
    static <T> Event<T> wrap(final String id, T obj) {
        return new Event<T>(id, obj)
    }
}
