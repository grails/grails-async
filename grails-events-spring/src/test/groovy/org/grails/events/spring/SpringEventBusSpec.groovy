package org.grails.events.spring

import org.springframework.context.support.GenericApplicationContext
import spock.lang.Specification

/**
 * Created by graemerocher on 27/03/2017.
 */
class SpringEventBusSpec extends Specification{

    void 'test spring event bus single arg'() {

        given:
        def context = new GenericApplicationContext()
        context.refresh()
        SpringEventBus eventBus = new SpringEventBus(context)
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar")

        expect:
        result == 'foo bar'
    }

    void 'test spring event bus multiple args'() {
        given:
        def context = new GenericApplicationContext()
        context.refresh()
        SpringEventBus eventBus = new SpringEventBus(context)
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo [bar, baz]'
    }

    void 'test spring event bus multiple args listener'() {
        given:
        def context = new GenericApplicationContext()
        context.refresh()

        SpringEventBus eventBus = new SpringEventBus(context)
        def result
        eventBus.on("test") { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo bar baz'
    }

    void 'test spring event bus error handling'() {
        given:
        def context = new GenericApplicationContext()
        context.refresh()

        SpringEventBus eventBus = new SpringEventBus(context)
        def result
        eventBus.on("test") { String data ->
            throw new RuntimeException("bad")
        }
        eventBus.sendAndReceive("test", "bar") {
            result = it
        }

        expect:
        result instanceof Throwable
    }
}
