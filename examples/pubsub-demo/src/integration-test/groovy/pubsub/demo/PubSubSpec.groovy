package pubsub.demo

import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

/**
 * Created by graemerocher on 03/04/2017.
 */
@Integration
class PubSubSpec extends Specification {

    @Autowired SumService sumService
    @Autowired TotalService totalService
    @Autowired BookService bookService
    @Autowired BookSubscriber bookSubscriber

    void "test event bus within Grails"() {
        when:
        sumService.sum(1, 2)
        sleep(500)
        sumService.sum(1, 2)
        sleep(500)

        then:
        totalService.accumulatedTotal  == 6
    }

    @Rollback
    void "test event from data service with rollback"() {
        when:"A transaction is rolled back"
        bookService.saveBook("The Stand")
        sleep(500)
        then:"no event is fired"
        bookSubscriber.newBooks == []
        bookSubscriber.insertEvents == []
    }

    void "test event from data service"() {
        when:"A transaction is committed"
        bookService.saveBook("The Stand")
        sleep(500)
        then:"The event is fired and received"
        bookSubscriber.newBooks == ["The Stand"]
        bookSubscriber.insertEvents.size() == 1

    }


}
