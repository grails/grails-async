package org.grails.async.factory.future

import grails.async.Promise
import grails.async.PromiseFactory
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
@CompileStatic
class FutureTaskPromise<T> extends FutureTask<T> implements Promise<T> {

    private volatile T boundValue = null
    private final PromiseFactory promiseFactory
    private Collection<FutureTaskChildPromise> failureCallbacks = new ConcurrentLinkedQueue<>()
    private Collection<FutureTaskChildPromise> successCallbacks = new ConcurrentLinkedQueue<>()

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
        return (T)(boundValue != null ? boundValue : super.get())
    }

    @Override
    T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return (T)(boundValue != null ? boundValue : super.get(timeout, unit))
    }

    @Override
    protected void set(T t) {
        for(callback in successCallbacks) {
            callback.accept(t)
        }
        super.set(t)
    }

    @Override
    protected void setException(Throwable t) {
        for(callback in failureCallbacks) {
            callback.accept(t)
        }
        super.setException(t)
    }

    @Override
    Promise<T> onComplete(Closure callable) {
        if(isDone()) {
            try {
                def v = get()
                return new BoundPromise<T>((T)callable.call(v))
            } catch (Throwable e) {
                return new BoundPromise (e)
            }
        }
        else {
            def newPromise = new FutureTaskChildPromise(promiseFactory,this,callable)
            successCallbacks.add(newPromise)
            return newPromise
        }
    }

    @Override
    Promise<T> onError(Closure callable) {
        if(isDone()) {
            try {
                get()
            } catch (Throwable e) {
                callable.call(e)
            }
        }
        else {
            def newPromise = new FutureTaskChildPromise(promiseFactory,this,callable)
            failureCallbacks.add(newPromise)
            return newPromise

        }
        return this
    }

    @Override
    Promise<T> then(Closure callable) {
        return onComplete(callable)
    }
}
