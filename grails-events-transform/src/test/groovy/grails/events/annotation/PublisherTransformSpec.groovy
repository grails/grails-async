package grails.events.annotation

import org.grails.datastore.gorm.transactions.transform.TransactionalTransform
import org.grails.datastore.mapping.core.order.OrderedComparator
import org.grails.events.transform.PublisherTransform
import spock.lang.Specification

/**
 * Created by graemerocher on 30/03/2017.
 */
class PublisherTransformSpec extends Specification {

    void "test order"() {
        given:
        def list = [new PublisherTransform(), new TransactionalTransform()]
        Collections.sort(list, new OrderedComparator<>())

        expect:
        list.first() instanceof TransactionalTransform
    }
}
