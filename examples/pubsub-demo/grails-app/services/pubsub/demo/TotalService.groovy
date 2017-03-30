package pubsub.demo

import grails.async.events.Event
import grails.events.transform.Subscriber

class TotalService {

    int accumulatedTotal = 0

    @Subscriber('total')
    void newTotal(int total) {
        accumulatedTotal += total
    }

    @Subscriber('total')
    void newTotal(Event<Integer> event) {
        println "Event $event.data"
        println "Data $event.data"
        println "Parameters $event.parameters"
    }
}
