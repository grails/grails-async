package org.grails.events.gpars

import spock.lang.Specification

class ActorEventBusSpec extends Specification {
    void 'test actor event bus single arg'() {
        given:
        ActorEventBus eventBus = new ActorEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar")
        sleep(500)
        expect:
        result == 'foo bar'
    }

    void 'test actor event bus multiple args'() {
        given:
        ActorEventBus eventBus = new ActorEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar", "baz")
        sleep(500)
        expect:
        result == 'foo [bar, baz]'
    }

    void 'test actor event bus multiple args listener'() {
        given:
        ActorEventBus eventBus = new ActorEventBus()
        def result
        eventBus.on("test") { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify("test", "bar", "baz")
        sleep(500)

        expect:
        result == 'foo bar baz'
    }

    void 'test actor event bus send and receive'() {
        given:
        ActorEventBus eventBus = new ActorEventBus()
        def result
        eventBus.on("test") { String data ->
            "foo $data"
        }
        eventBus.sendAndReceive("test", "bar") {
            result = it
        }
        sleep(500)

        expect:
        result == 'foo bar'
    }

    void 'test actor event bus error handling'() {
        given:
        ActorEventBus eventBus = new ActorEventBus()
        def result
        eventBus.on("test") { String data ->
            throw new RuntimeException("bad")
        }
        eventBus.sendAndReceive("test", "bar") {
            result = it
        }
        sleep(500)

        expect:
        result instanceof Throwable
    }

}