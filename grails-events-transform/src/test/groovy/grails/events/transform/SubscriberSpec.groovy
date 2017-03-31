package grails.events.transform

import grails.async.events.bus.EventBus
import grails.async.events.subscriber.MethodSubscriber
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.events.transform.MethodRegisteringSubscriber
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
    @grails.events.transform.Subscriber('total')
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
        service instanceof MethodRegisteringSubscriber

        when:
        service.registerMethods()


        then:
        1 * eventBus.subscribe("total", new MethodSubscriber(service, methodObject))
    }
}
