/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package reactor.fn;

/**
 * Implementations accept a given value and perform work on the argument.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 *
 * @param <T> the type of values to accept
 * @deprecated Here for compatibility only. Do not use directly
 */
@Deprecated
public interface Consumer<T> {

    /**
     * Execute the logic of the action, accepting the given parameter.
     *
     * @param t The parameter to pass to the consumer.
     */
    void accept(T t);

}
