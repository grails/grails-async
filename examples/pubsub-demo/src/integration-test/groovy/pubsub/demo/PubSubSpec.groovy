package pubsub.demo

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

    void "test event bus within Grails"() {
        when:
        sumService.sum(1, 2)
        sleep(500)
        sumService.sum(1, 2)
        sleep(500)

        then:
        totalService.accumulatedTotal  == 6
    }
}
