package pubsub.demo

import grails.testing.mixin.integration.Integration
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by graemerocher on 30/05/2017.
 */
@Integration
@Ignore
class TaskControllerSpec extends HttpClientSpec {

    void "test async error handling"() {
        when:
        HttpRequest request = HttpRequest.GET("/task/error")
        client.toBlocking().exchange(request, Argument.of(String), Argument.of(String))

        then:
        HttpClientResponseException e = thrown()
        e.response.status == HttpStatus.INTERNAL_SERVER_ERROR
        e.response.body() == 'error occured'
    }
}
