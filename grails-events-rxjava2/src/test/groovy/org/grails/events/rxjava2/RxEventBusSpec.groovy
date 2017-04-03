package org.grails.events.rxjava2

import io.reactivex.schedulers.Schedulers
import spock.lang.Specification

/**
 * Created by graemerocher on 27/03/2017.
 */
class RxEventBusSpec extends Specification {
    void 'test rx event bus single arg'() {
        given:
        RxEventBus eventBus = new RxEventBus(Schedulers.trampoline())
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar")

        expect:
        result == 'foo bar'
    }

    void 'test rx event bus multiple args'() {
        given:
        RxEventBus eventBus = new RxEventBus(Schedulers.trampoline())
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo [bar, baz]'
    }

    void 'test rx event bus multiple args listener'() {
        given:
        RxEventBus eventBus = new RxEventBus(Schedulers.trampoline())
        def result
        eventBus.on("test") { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo bar baz'
    }

    void 'test rx event bus send and receive'() {
        given:
        RxEventBus eventBus = new RxEventBus(Schedulers.trampoline())
        def result
        eventBus.on("test") { String data ->
            "foo $data"
        }
        eventBus.sendAndReceive("test", "bar") {
            result = it
        }
        expect:
        result == 'foo bar'
    }

    void 'test rx event bus error handling'() {
        given:
        RxEventBus eventBus = new RxEventBus(Schedulers.trampoline())
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
