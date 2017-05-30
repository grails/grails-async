/*
 * Copyright 2013 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.async

import spock.lang.Specification

import java.util.concurrent.ExecutionException

/**
 * @author Graeme Rocher
 * @since 2.3
 */
class PromiseListSpec extends Specification{


    void "Test promise list handling"() {
        when:"A list of promises is created"
            def list = new PromiseList()
            list << { 1 }
            list << { 2 }
            list << { 3 }
            def res
            list.onComplete { List results ->
                res = results
            }
            sleep 500

        then:'then the result from onComplete is correct'
            res == [1,2,3]
    }

    void "Test promise list handling with some async operations and some values"() {
        when:"A list of promises is created"
            def list = new PromiseList()
            list << { 1 }
            list <<  2
            list << { 3 }
            def res
            list.onComplete { List results ->
                res = results
            }
            sleep 500

        then:'then the result from onComplete is correct'
            res == [1,2,3]
    }

    void "Test promise list with then chaining"() {
        when:"A promise list is used with then chaining"
            def list = new PromiseList<Integer>()
            list << { 1 } << { 2 } << {3}
            def result = list   .get()
        then:"An appropriately populated list is produced"
            result == [1,2,3]

    }

    void "Test promise list with an exception"() {
        when:"A promise list with a promise that throws an exception"
            def list = new PromiseList()
            list << {
                1
            }
            list << {
                throw new RuntimeException("bad")
            }
            list << {
                3
            }
            def result = list.get()

        then:'the onError handler is invoked with the exception'
            def err = thrown(ExecutionException)
            err != null
            err.message == "java.lang.RuntimeException: bad"
    }
}
