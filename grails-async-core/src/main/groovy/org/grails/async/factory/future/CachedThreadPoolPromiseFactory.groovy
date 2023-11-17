package org.grails.async.factory.future

import grails.async.Promise
import grails.async.PromiseList
import grails.async.factory.AbstractPromiseFactory
import groovy.transform.CompileStatic
import org.grails.async.factory.BoundPromise

import javax.annotation.PreDestroy
import java.util.concurrent.*

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
                new FutureTaskPromise<T>(pf,callable)
            }

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                new FutureTaskPromise<T>(pf,runnable, value)
            }
        }
    }

    @Override
    <T> Promise<T> createPromise(Class<T> returnType) {
        return new BoundPromise<T>(null)
    }

    @Override
    Promise<Object> createPromise() {
        return new BoundPromise<Object>(null)
    }

    @Override
    <T> Promise<T> createPromise(Closure<T>... closures) {
        if(closures.length == 1) {
            def callable = closures[0]
            applyDecorators(callable, null)
            (Promise<T>)executorService.submit((Callable)callable)
        }
        else {
            PromiseList<T> list = new PromiseList<>()
            for(c in closures) {
                list.add(c)
            }
            return list as Promise<T>
        }
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises) {
        return promises.collect() { Promise<T> p -> p.get() }
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises, long timeout, TimeUnit units) {
        return promises.collect() { Promise<T> p -> p.get(timeout, units) }
    }

    @Override
    <T> Promise<List<T>> onComplete(List<Promise<T>> promises, Closure<?> callable) {
        executorService.submit((Callable) {
            while(promises.every() { Promise p -> !p.isDone() }) {
                // wait
            }
            List<T> values = promises.collect { Promise<T> p -> p.get() }
            callable.call(values)
        }) as Promise<List<T>>
    }

    @Override
    <T> Promise<List<T>> onError(List<Promise<T>> promises, Closure<?> callable) {
        executorService.submit((Callable) {
            while(promises.every() { Promise p -> !p.isDone() }) {
                // wait
            }
            try {
                promises.each() { Promise<T> p -> p.get()  }
            } catch (Throwable e) {
                callable.call(e)
                return e
            }
        }) as Promise<List<T>>
    }

    @Override
    @PreDestroy
    void close() {
        if(!executorService.isShutdown()) {
            executorService.shutdown()
        }
    }
}
