package org.grails.events.subscriber

import grails.events.subscriber.MethodSubscriber
import org.grails.events.bus.SynchronousEventBus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by graemerocher on 28/03/2017.
 */
class MethodEventSubscriberSpec extends Specification {

    @Shared TestHandler testHandler = new TestHandler()

    def setup() {
        testHandler.eventHandled = false
    }

    void "test convert method argument"() {
        given:
        TestService testService = new TestService()
        def subscriber = new MethodSubscriber(testService, TestService.getMethod("foo", Integer))

        expect:
        subscriber.call(1) == 2
        subscriber.call("1") == 2
        subscriber.call("") == null
    }

    @Unroll
    void "sendAndReceive calls reply once subscriber handles the method - #type"() {

        given:
        def eventBus = new SynchronousEventBus()
        def topic = 'test_topic'
        def replied = false

        and:
        eventBus.subscribe(topic, subscriber)

        when:
        eventBus.sendAndReceive(topic, new Object()) {
            replied = true
        }

        then:
        testHandler.eventHandled
        replied

        where:
        type      | subscriber
        'closure' | { testHandler.handleEvent() }
        'method'  | new MethodSubscriber(testHandler, TestHandler.getMethod("handleEvent"))
    }
}

class TestService {
    def foo(Integer num) {
       return num + 1
    }
}

class TestHandler {
    boolean eventHandled = false

    def handleEvent() {
        eventHandled = true
    }
}
