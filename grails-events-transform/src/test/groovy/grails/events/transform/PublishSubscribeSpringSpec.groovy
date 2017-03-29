package grails.events.transform

import grails.async.events.Event
import grails.gorm.transactions.Transactional
import org.grails.async.events.bus.SynchronousEventBus
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by graemerocher on 29/03/2017.
 */
class PublishSubscribeSpringSpec extends Specification {

    @Shared @AutoCleanup SimpleMapDatastore datastore = new SimpleMapDatastore()

    def "test event publisher within Spring"() {
        given:
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        def bus = new SynchronousEventBus()
        applicationContext.beanFactory.registerSingleton("eventBus", bus)
        applicationContext.register(OneService, TwoService)
        applicationContext.refresh()

        when:
        OneService publisher = applicationContext.getBean(OneService)
        TwoService subscriber = applicationContext.getBean(TwoService)

        publisher.sum(1, 2)


        then:
        subscriber.total == 3
        subscriber.events.size() == 1
        subscriber.events[0].parameters == [a:1,b:2]
        subscriber.transactionalInvoked

        when:
        publisher.wrongType()

        then:
        subscriber.total == 3
        subscriber.events.size() == 2
    }
}

@Component
class OneService {
    @Publisher('total')
    int sum(int a, int b) {
        a + b
    }

    @Publisher('total')
    Date wrongType() {
        new Date()
    }
}

@Component
class TwoService {
    int total = 0
    List<Event> events = []
    boolean transactionalInvoked = false

    @Subscriber('total')
    void onSum(int num) {
        total += num
    }

    @Subscriber('total')
    void onSum2(Event event) {
        events.add(event)
    }

    @Subscriber('total')
    @Transactional
    void doSomething(Event event) {
        transactionStatus != null
        transactionalInvoked = true
    }
}
