package pubsub.demo

import grails.events.annotation.Subscriber
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.springframework.stereotype.Component

@Component
class BookSubscriber {

    List<String> newBooks =[]
    List<PreInsertEvent> insertEvents =[]

    @Subscriber("newBook")
    void withBook(Book book) {
        newBooks.add(book.title)
    }

    @Subscriber
    void beforeInsert(PreInsertEvent event) {
        insertEvents.add(event)
    }
}
