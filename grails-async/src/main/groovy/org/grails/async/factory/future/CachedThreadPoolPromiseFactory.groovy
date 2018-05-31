package org.grails.async.factory.future

import grails.async.Promise
import grails.async.PromiseList
import grails.async.factory.AbstractPromiseFactory
import groovy.transform.CompileStatic
import org.grails.async.factory.BoundPromise

import javax.annotation.PreDestroy
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.RunnableFuture
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * A promise factory that uses an ExecutorService by default
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class CachedThreadPoolPromiseFactory extends AbstractPromiseFactory implements Closeable, ExecutorPromiseFactory {

    final @Delegate ExecutorService executorService

    CachedThreadPoolPromiseFactory(int maxPoolSize = Integer.MAX_VALUE, long timeout = 60L, TimeUnit unit = TimeUnit.SECONDS) {
        CachedThreadPoolPromiseFactory pf = this
        this.executorService = new ThreadPoolExecutor(0, maxPoolSize, timeout, unit, new SynchronousQueue<Runnable>()) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                return new FutureTaskPromise<T>(pf,callable)
            }

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                return new FutureTaskPromise<T>(pf,runnable, value)
            }
        }
    }

    @Override
    def <T> Promise<T> createPromise(Class<T> returnType) {
        return new BoundPromise<T>(null)
    }

    @Override
    Promise<Object> createPromise() {
        return new BoundPromise<Object>(null)
    }

    @Override
    def <T> Promise<T> createPromise(Closure<T>... closures) {
        if(closures.length == 1) {
            def callable = closures[0]
            applyDecorators(callable, null)
            (Promise<T>)executorService.submit((Callable)callable)
        }
        else {
            def list = new PromiseList<>()
            for(c in closures) {
                list.add(c)
            }
            return list
        }
    }

    @Override
    def <T> List<T> waitAll(List<Promise<T>> promises) {
        return promises.collect() { Promise<T> p -> p.get() }
    }

    @Override
    def <T> List<T> waitAll(List<Promise<T>> promises, long timeout, TimeUnit units) {
        return promises.collect() { Promise<T> p -> p.get(timeout, units) }
    }

    @Override
    def <T> Promise<List<T>> onComplete(List<Promise<T>> promises, Closure<?> callable) {
        return (Promise<List<T>>)executorService.submit( (Callable) {
            while(promises.every() { Promise p -> !p.isDone() }) {
                // wait
            }
            def values = promises.collect() { Promise<T> p -> p.get() }
            callable.call(values)
        })
    }

    @Override
    def <T> Promise<List<T>> onError(List<Promise<T>> promises, Closure<?> callable) {
        return (Promise<List<T>>)executorService.submit((Callable) {
            while(promises.every() { Promise p -> !p.isDone() }) {
                // wait
            }
            try {
                promises.each() { Promise<T> p -> p.get()  }
            } catch (Throwable e) {
                callable.call(e)
                return e
            }
        })
    }

    @Override
    @PreDestroy
    void close() {
        if(!executorService.isShutdown()) {
            executorService.shutdown()
        }
    }

}
