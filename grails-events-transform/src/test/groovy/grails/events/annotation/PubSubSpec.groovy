package grails.events.annotation

import grails.events.bus.EventBusAware
import org.grails.events.transform.AnnotatedSubscriber
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

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
        totalService.total.intValue() == 6
    }
}

// tag::publisher[]
class SumService {
    @Publisher
    int sum(int a, int b) {
        a + b
    }
}
// end::publisher[]

// tag::subscriber[]
class TotalService {
    AtomicInteger total = new AtomicInteger(0)
    @Subscriber
    void onSum(int num) {
        total.addAndGet(num)
    }
}
// end::subscriber[]
