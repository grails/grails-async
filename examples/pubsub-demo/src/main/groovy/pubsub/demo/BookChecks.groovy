package pubsub.demo

import grails.events.annotation.gorm.Listener
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.springframework.stereotype.Component

@Component
class BookChecks {

    @Listener(Book)
    void checkBook(PreInsertEvent event) {
        String title = event.getEntityAccess().getPropertyValue("title")
        if(title?.contains("Politics")) {
            throw new IllegalArgumentException("Books about politics not allowed")
        }
    }

    @Listener(Book)
    void tagFunnyBooks(PreInsertEvent event) {
        String title = event.getEntityAccess().getPropertyValue("title")
        if(title?.contains("funny")) {
            event.getEntityAccess().setProperty("title", "Humor - ${title}".toString())
        }
    }

    @Listener(Author)
    void tagAuthor(PreInsertEvent event) {
        assert event.entityObject instanceof Author
    }
}
