package org.grails.events

import org.grails.events.bus.SynchronousEventBus
import spock.lang.Specification

/**
 * Created by graemerocher on 24/03/2017.
 */
class SynchronousEventBusSpec extends Specification {

    void 'test synchronous event bus single arg'() {
        given:
        SynchronousEventBus eventBus = new SynchronousEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar")

        expect:
        result == 'foo bar'
    }

    void 'test synchronous event bus multiple args'() {
        given:
        SynchronousEventBus eventBus = new SynchronousEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo [bar, baz]'
    }

    void 'test synchronous event bus multiple args listener'() {
        given:
        SynchronousEventBus eventBus = new SynchronousEventBus()
        def result
        eventBus.on("test") { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo bar baz'
    }
}
