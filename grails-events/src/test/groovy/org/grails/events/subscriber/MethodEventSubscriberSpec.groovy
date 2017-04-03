package org.grails.events.subscriber

import grails.events.subscriber.MethodSubscriber
import spock.lang.Specification

/**
 * Created by graemerocher on 28/03/2017.
 */
class MethodEventSubscriberSpec extends Specification {

    void "test convert method argument"() {
        given:
        TestService testService = new TestService()
        def subscriber = new MethodSubscriber(testService, TestService.getMethod("foo", Integer))

        expect:
        subscriber.call(1) == 2
        subscriber.call("1") == 2
        subscriber.call("") == null
    }
}

class TestService {
    def foo(Integer num) {
       return num + 1
    }
}
