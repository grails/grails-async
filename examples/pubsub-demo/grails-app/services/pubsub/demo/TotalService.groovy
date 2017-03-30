package pubsub.demo

import grails.events.transform.Subscriber

class TotalService {

    int accumulatedTotal = 0

    @Subscriber('total')
    void newTotal(int total) {
        accumulatedTotal += total
    }
}
