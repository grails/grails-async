package org.grails.async.factory.future

import grails.async.Promise
import grails.async.PromiseFactory
import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.grails.async.factory.BoundPromise

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * A Promise that is a {@link FutureTask}
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@AutoFinal
@CompileStatic
class FutureTaskPromise<T> extends FutureTask<T> implements Promise<T> {

    private T boundValue = null

    private final PromiseFactory promiseFactory
    private final Collection<FutureTaskChildPromise> failureCallbacks = new ConcurrentLinkedQueue<>()
    private final Collection<FutureTaskChildPromise> successCallbacks = new ConcurrentLinkedQueue<>()

    FutureTaskPromise(PromiseFactory promiseFactory, Callable<T> callable) {
        super(callable)
        this.promiseFactory = promiseFactory
    }

    FutureTaskPromise(PromiseFactory promiseFactory, Runnable runnable, T value) {
        super(runnable, value)
        boundValue = value
        this.promiseFactory = promiseFactory
    }

    @Override
    Promise<T> accept(T value) {
        boundValue = value
        return this
    }

    @Override
    boolean isDone() {
        return boundValue != null || super.isDone()
    }

    @Override
    T get() throws InterruptedException, ExecutionException {
        return (boundValue ?: super.get()) as T
    }

    @Override
    T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return (boundValue ?: super.get(timeout, unit)) as T
    }

    @Override
    protected void set(T t) {
        super.set(t)
        synchronized (successCallbacks) {
            for (FutureTaskChildPromise callback : successCallbacks) {
                callback.accept(t)
            }
        }
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t)
        synchronized (failureCallbacks) {
            for (FutureTaskChildPromise callback : failureCallbacks) {
                callback.accept(t)
            }
        }
    }

    @Override
    Promise<?> onComplete(Closure<?> callable) {
        synchronized (successCallbacks) {
            if (done) {
                try {
                    T value = get()
                    return new BoundPromise<T>(value).onComplete(callable)
                } catch (Throwable ignored) {
                    return this
                }
            }
            else {
                Promise<T> newPromise = new FutureTaskChildPromise(promiseFactory, this, callable)
                successCallbacks.add(newPromise)
                return newPromise
            }
        }
    }

    @Override
    Promise<?> onError(Closure<?> callable) {
        synchronized (failureCallbacks) {
            if (done) {
                try {
                    get()
                    return this
                } catch (Throwable e) {
                    return new BoundPromise(callable.call(e))
                }
            }
            else {
                def newPromise = new FutureTaskChildPromise(promiseFactory, this, callable)
                failureCallbacks.add(newPromise)
                return newPromise
            }
        }
    }

    @Override
    Promise<?> then(Closure callable) {
        return onComplete(callable)
    }
}
