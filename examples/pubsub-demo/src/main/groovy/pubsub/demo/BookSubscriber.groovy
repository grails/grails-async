package pubsub.demo

import grails.events.annotation.Subscriber
import org.springframework.stereotype.Component

@Component
class BookSubscriber {

    List<String> newBooks =[]

    @Subscriber("newBook")
    void withBook(Book book) {
        newBooks.add(book.title)
    }
}
