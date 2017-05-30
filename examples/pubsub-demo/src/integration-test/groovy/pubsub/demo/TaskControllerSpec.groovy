package pubsub.demo

import geb.spock.GebSpec
import grails.plugins.rest.client.RestBuilder
import grails.test.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by graemerocher on 30/05/2017.
 */
@Integration
@Ignore
class TaskControllerSpec extends GebSpec {

    void "test async error handling"() {
        given:
        RestBuilder restBuilder = new RestBuilder()
        when:
        def result = restBuilder.get("${baseUrl}/task/error")

        then:
        result.status == 500
        result.text == "error occured"
    }
}
