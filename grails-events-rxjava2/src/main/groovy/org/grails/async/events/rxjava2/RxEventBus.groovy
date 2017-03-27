package org.grails.async.events.rxjava2

import grails.async.events.Event
import grails.async.events.emitter.EventEmitter
import grails.async.events.registry.EventRegistry
import grails.async.events.registry.Subscription
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.grails.async.events.bus.AbstractEventBus

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
    protected final Map<CharSequence, Collection<Disposable>> subscriptions = new ConcurrentHashMap<CharSequence, Collection<Disposable>>().withDefault {
        new ConcurrentLinkedQueue<Disposable>()
    }


    final Scheduler scheduler

    RxEventBus(Scheduler scheduler = Schedulers.io()) {
        this.scheduler = scheduler
    }

    @Override
    Subscription on(CharSequence event, Closure listener) {
        String eventKey = event.toString()
        int argCount = listener.parameterTypes?.length ?: 1
        Disposable sub = subjects.get(eventKey)
                .observeOn(scheduler)
                .subscribe( { data ->

            Closure reply = null
            if(data  instanceof EventWithReply) {
                def eventWithReply = (EventWithReply) data
                data = eventWithReply.event.data
                reply = eventWithReply.reply
            }

            try {
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
            } catch (Throwable e) {
                if(reply != null && reply.parameterTypes && reply.parameterTypes[0].isInstance(e)) {
                    reply.call(e)
                }
                else {
                    throw e
                }
            }

        }  as Consumer, { Throwable t ->
            log.error("Error occurred triggering event listener for event [$event]: ${t.message}", t)
        } as Consumer<Throwable>)
        Collection<Disposable> subs = subscriptions.get(eventKey)
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
        Collection<Disposable> subs = subscriptions.get(eventKey)
        for(sub in subs) {
            if(!sub.isDisposed()) {
                sub.dispose()
            }
        }
        subs.clear()
        return this
    }

    @Override
    EventEmitter notify(Event event) {
        PublishSubject sub = subjects.get(event.id)
        if(sub.hasObservers() && !sub.hasComplete()) {
            sub.onNext(event.data)
        }
        return this
    }

    @Override
    EventEmitter sendAndReceive(Event event, Closure reply) {
        PublishSubject sub = subjects.get(event.id)
        if(sub.hasObservers() && !sub.hasComplete()) {
            sub.onNext(new EventWithReply(event, reply))
        }
        return this
    }

    private static class RxSubscription implements Subscription {
        final Disposable subscription
        final Collection<Disposable> subscriptions

        RxSubscription(Disposable subscription, Collection<Disposable> subscriptions) {
            this.subscription = subscription
            this.subscriptions = subscriptions
        }

        @Override
        Subscription cancel() {
            if(!subscription.isDisposed()) {
                subscription.dispose()
            }
            subscriptions.remove(subscription)
            return this
        }

        @Override
        boolean isCancelled() {
            return subscription.isDisposed()
        }
    }
}
