package org.grails.async.events

import org.grails.async.events.bus.SynchronousEventBus
import org.grails.async.events.bus.spring.TaskExecuterEventBus
import spock.lang.Specification

/**
 * Created by graemerocher on 28/03/2017.
 */
class TaskExecuterEventBusSpec  extends Specification {

    void 'test task executor event bus single arg'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar")

        expect:
        result == 'foo bar'
    }

    void 'test task executor event bus multiple args'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo [bar, baz]'
    }

    void 'test task executor event bus multiple args listener'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
        def result
        eventBus.on("test") { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify("test", "bar", "baz")

        expect:
        result == 'foo bar baz'
    }

    void 'test task executor event bus send and receive'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
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

    void 'test task executor bus error handling'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
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

    void 'test task executor bus error handling with publish'() {
        given:
        TaskExecuterEventBus eventBus = new TaskExecuterEventBus()
        when:
        def result
        eventBus.on("test") { String data ->
            throw new RuntimeException("bad")
        }
        eventBus.publish("test", "bar")

        then:
        thrown(RuntimeException)
    }
}

