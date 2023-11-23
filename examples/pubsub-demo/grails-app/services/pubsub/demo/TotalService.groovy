package pubsub.demo

import grails.events.Event
import grails.events.annotation.Subscriber
import groovy.transform.CompileStatic

@CompileStatic
class TotalService {

    int accumulatedTotal = 0

    @Subscriber
    @SuppressWarnings('unused')
    void onSum(int total) {
        accumulatedTotal += total
    }

    @Subscriber
    @SuppressWarnings('unused')
    void onSum(Event<Integer> event) {
        println "Event $event.id"
        println "Data $event.data"
        println "Parameters $event.parameters"
    }

    @Subscriber
    @SuppressWarnings('unused')
    void onSum(Throwable error) {
        println "Oh NO!!! $error.message"
    }
}
