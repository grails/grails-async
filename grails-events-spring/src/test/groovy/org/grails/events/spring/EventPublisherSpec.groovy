package org.grails.events.spring

import grails.events.EventPublisher
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import spock.lang.Specification

/**
 * Created by graemerocher on 27/03/2017.
 */
class EventPublisherSpec extends Specification {

    def "test event publisher within Spring"() {
        given:
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        def bus = new SpringEventBus(applicationContext)
        applicationContext.beanFactory.registerSingleton("eventBus", bus)
        applicationContext.register(MyPublisher)
        applicationContext.refresh()

        when:
        MyPublisher publisher = applicationContext.getBean(MyPublisher)
        def result
        bus.on("test") {
            result = "good $it"
        }
        publisher.publish("test", "data")


        then:
        result == "good data"

    }
}
@Component
class MyPublisher implements EventPublisher {
}
