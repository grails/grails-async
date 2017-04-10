package grails.events.annotation

import grails.events.bus.EventBus
import grails.events.subscriber.MethodSubscriber
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.events.gorm.GormAnnotatedSubscriber
import org.grails.events.transform.AnnotatedSubscriber
import spock.lang.Specification

/**
 * Created by graemerocher on 29/03/2017.
 */
class SubscriberSpec extends Specification {

    void "test subscriber transform"() {
        given:
        def service = new GroovyClassLoader().parseClass('''
class TestService {
    int total = 0
    @grails.events.annotation.Subscriber('total')
    void onSum(int num) {
        total += num
    }
}

''').newInstance()
        def methodObject = service.getClass().getDeclaredMethod("onSum", int)

        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        ClassPropertyFetcher.forClass(service.getClass()).getPropertyValue("lazyInit") == false
        service instanceof AnnotatedSubscriber

        when:
        service.registerMethods()


        then:
        1 * eventBus.subscribe("total", new MethodSubscriber(service, methodObject))
    }

    void "test gorm event subscriber transform"() {
        given:
        def service = new GroovyShell().evaluate('''
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import grails.events.annotation.*

class TestService {
    @Subscriber
    void onInsert(PreInsertEvent event) {
        // whatever
    }
}
return TestService
''').newInstance()
        def methodObject = service.getClass().getDeclaredMethod("onInsert", PreInsertEvent)

        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        ClassPropertyFetcher.forClass(service.getClass()).getPropertyValue("lazyInit") == false
        service instanceof GormAnnotatedSubscriber

        when:
        service.registerMethods()


        then:
        1 * eventBus.subscribe("gorm:preInsert", new MethodSubscriber(service, methodObject))
    }
}
