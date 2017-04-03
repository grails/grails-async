package grails.events.annotation

import grails.async.events.bus.EventBusAware
import org.grails.events.transform.AnnotatedSubscriber
import spock.lang.Specification

class PubSubSpec extends Specification {

    void "test pub/sub with default event bus"() {
        given:
        SumService sumService = new SumService()
        TotalService totalService = new TotalService()
        AnnotatedSubscriber annotatedSubscriber = (AnnotatedSubscriber)totalService
        EventBusAware publisher = (EventBusAware)sumService
        annotatedSubscriber.setTargetEventBus(publisher.getEventBus())
        annotatedSubscriber.registerMethods()

        when:
        sumService.sum(1,2)
        sumService.sum(1,2)

        then:
        totalService.total == 6
    }
}

class SumService {
    @Publisher
    int sum(int a, int b) {
        a + b
    }
}

class TotalService {
    int total
    @Subscriber
    void onSum(int num) {
        total += num
    }
}
