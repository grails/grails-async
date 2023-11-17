package pubsub.demo

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import jakarta.inject.Inject
import spock.lang.Specification

/**
 * Created by graemerocher on 03/04/2017.
 */
@Integration
class PubSubSpec extends Specification {

    @Inject SumService sumService
    @Inject TotalService totalService
    @Inject BookService bookService
    @Inject BookSubscriber bookSubscriber

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
        bookSubscriber.insertEvents.isEmpty()
    }

    void "test event from data service"() {
        when:"A transaction is committed"
        bookService.saveBook("The Stand")
        sleep(500)
        then:"The event is fired and received"
        bookSubscriber.newBooks == ["The Stand"]
        bookSubscriber.insertEvents.size() == 1

    }

    @Rollback
    void "test modify property event listener"() {
        when:"When an event listener modifies a property"
        bookService.saveBook("funny book")

        then:"The property was modified"
        Book.findByTitle("Humor - funny book") != null

    }


    @Rollback
    void "test synchronous event listener"() {
        when:"When a event listener cancels an insert"
        bookService.saveBook("UK Politics")

        // due to  https://hibernate.atlassian.net/browse/HHH-11721
        // an exception most be thrown
        then:"The insert was cancelled"
        def e = thrown(IllegalArgumentException)
        e.message == "Books about politics not allowed"

    }
}
