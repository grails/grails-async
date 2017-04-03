package pubsub.demo

import grails.async.events.Event
import grails.events.annotation.Subscriber

class TotalService {

    int accumulatedTotal = 0

    @Subscriber
    void onSum(int total) {
        accumulatedTotal += total
    }

    @Subscriber
    void onSum(Event<Integer> event) {
        println "Event $event.id"
        println "Data $event.data"
        println "Parameters $event.parameters"
    }

    @Subscriber
    void onSum(Throwable error) {
        println "Oh NO!!! $error.message"
    }
}
