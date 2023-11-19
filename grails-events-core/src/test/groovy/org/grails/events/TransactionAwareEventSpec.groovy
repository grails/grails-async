package org.grails.events

import grails.events.Event
import org.grails.events.bus.ExecutorEventBus
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionSynchronizationUtils
import spock.lang.Specification

/**
 * Created by graemerocher on 28/03/2017.
 */
class TransactionAwareEventSpec extends Specification {

    void 'test task executor event bus with transactional event'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on("test") {
            result = "foo $it"
        }

        when:"an event is fired with an active transaction"
        TransactionSynchronizationManager.initSynchronization()
        eventBus.notify(Event.from("test", "bar"), TransactionPhase.AFTER_COMMIT)

        then:"the event was not triggered"
        result == null

        when:"The transaction is committed"
        TransactionSynchronizationUtils.invokeAfterCommit(TransactionSynchronizationManager.getSynchronizations())

        then:"The event was triggered"
        result == "foo bar"

        cleanup:
        TransactionSynchronizationManager.clearSynchronization()
    }
}
