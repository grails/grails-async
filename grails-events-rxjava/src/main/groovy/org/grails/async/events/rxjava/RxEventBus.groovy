package org.grails.async.events.rxjava

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.async.events.bus.AbstractEventBus
import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An EventBus implementation that uses RxJava
 *
 * @author Graeme Rocher
 * @since 3.3
 *
 */
@CompileStatic
@Slf4j
class RxEventBus extends AbstractEventBus {
    protected final Map<CharSequence, PublishSubject> subjects = new ConcurrentHashMap<CharSequence, PublishSubject>().withDefault {
        PublishSubject.create()
    }
    protected final Map<CharSequence, Collection<Subscription>> subscriptions = new ConcurrentHashMap<CharSequence, Collection<Subscription>>().withDefault {
        new ConcurrentLinkedQueue<Subscription>()
    }


    final Scheduler scheduler

    RxEventBus(Scheduler scheduler = Schedulers.io()) {
        this.scheduler = scheduler
    }

    @Override
    grails.async.events.registry.Subscription on(CharSequence event, Closure listener) {
        String eventKey = event.toString()
        int argCount = listener.parameterTypes?.length ?: 1
        Subscription sub = subjects.get(eventKey)
                                        .observeOn(scheduler)
                                        .subscribe( { data ->

            Closure reply = null
            if(data  instanceof EventWithReply) {
                def eventWithReply = (EventWithReply) data
                data = eventWithReply.event.data
                reply = eventWithReply.reply
            }

            def result
            if(data.getClass().isArray() && argCount == ((Object[])data).length) {
                result = invokeListener(listener, data)
            }
            else {
                result = listener.call(data)
            }
            if(reply != null) {
                reply.call(result)
            }

        }  as Action1, { Throwable t ->
            log.error("Error occurred triggering event listener for event [$event]: ${t.message}", t)
        } as Action1<Throwable>)
        Collection<Subscription> subs = subscriptions.get(eventKey)
        subs.add(sub)
        return new RxSubscription(sub, subs)
    }

    @CompileDynamic
    protected Object invokeListener(Closure listener, data) {
        listener.call(*data)
    }

    @Override
    EventRegistry unsubscribeAll(CharSequence event) {
        String eventKey = event.toString()
        Collection<Subscription> subs = subscriptions.get(eventKey)
        for(sub in subs) {
            if(!sub.isUnsubscribed()) {
                sub.unsubscribe()
            }
        }
        subs.clear()
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        PublishSubject sub = subjects.get(event.id)
        if(sub.hasObservers() && !sub.hasCompleted()) {
            sub.onNext(event.data)
        }
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        PublishSubject sub = subjects.get(event.id)
        if(sub.hasObservers() && !sub.hasCompleted()) {
            sub.onNext(new EventWithReply(event, reply))
        }
        return this
    }

    private static class RxSubscription implements grails.async.events.registry.Subscription {
        final Subscription subscription
        final Collection<Subscription> subscriptions

        RxSubscription(Subscription subscription, Collection<Subscription> subscriptions) {
            this.subscription = subscription
            this.subscriptions = subscriptions
        }

        @Override
        grails.async.events.registry.Subscription cancel() {
            if(!subscription.isUnsubscribed()) {
                subscription.unsubscribe()
            }
            subscriptions.remove(subscription)
            return this
        }

        @Override
        boolean isCancelled() {
            return subscription.isUnsubscribed()
        }
    }
}
