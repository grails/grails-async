package pubsub.demo

import grails.events.transform.Publisher
import grails.gorm.transactions.Transactional

class SumService {

    @Publisher("total")
    @Transactional
    int sum(int a, int b) {

        return a + b
    }
}
