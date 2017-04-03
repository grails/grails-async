package grails.events.annotation

import grails.events.Event
import grails.events.EventPublisher
import grails.events.bus.EventBus
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.transactions.DatastoreTransactionManager
import org.springframework.transaction.event.TransactionPhase
import spock.lang.Specification

/**
 * Created by graemerocher on 28/03/2017.
 */
class PublisherSpec extends Specification {

    void "test publisher transform on method that returns void"() {
        given:
        def service = new GroovyClassLoader().parseClass('''
class TestService {

    @grails.events.annotation.Publisher('total')
    void sum(int a, int b) {
        a + b
    }
}

''').newInstance()
        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        service instanceof EventPublisher

        when:
        service.sum(1, 2)

        then:
        1 * eventBus.publish(new Event("total", [a:1, b:2], null))
    }


    void "test publisher transform"() {
        given:
        def service = new GroovyClassLoader().parseClass('''
class TestService {

    @grails.events.annotation.Publisher('total')
    Integer sum(int a, int b) {
        a + b
    }
}

''').newInstance()
        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        service instanceof EventPublisher

        when:
        def result = service.sum(1, 2)

        then:
        result == 3
        1 * eventBus.publish(new Event("total", [a:1, b:2], 3))
    }

    void "test publisher transform on transactional service"() {
        given:
        def service = new GroovyClassLoader().parseClass('''
class TestService {

    @grails.events.annotation.Publisher('total')
    @grails.gorm.transactions.Transactional
    Integer sum(int a, int b) {
        a + b
    }
}

''').newInstance()
        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus
        def manager = new DatastoreTransactionManager()
        def datastore = Mock(Datastore)
        datastore.connect() >> Mock(Session)
        manager.setDatastore(datastore)
        service.transactionManager = manager

        then:
        service instanceof EventPublisher

        when:
        def result = service.sum(1, 2)

        then:
        result == 3
        1 * eventBus.notify(new Event("total", [a:1, b:2], 3), TransactionPhase.AFTER_COMMIT)
    }
}

