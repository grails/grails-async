package reactor.bus

import reactor.bus.registry.Subscription
import reactor.bus.selector.Selector
import reactor.fn.Consumer

/**
 * @deprecated Here for compatibility only. Do not use directly
 */
@Deprecated
class EventBus  implements Bus {

    @Delegate grails.async.events.bus.EventBus eventBus

    @Override
    boolean respondsToKey(Object key) {
        throw new UnsupportedOperationException()
    }

    @Override
    Bus notify(Object key, Object ev) {
        eventBus.notify(key.toString(), ev)
        return this
    }

    @Override
    Subscription<Object, Consumer> on(Selector selector, Consumer consumer) {
        throw new UnsupportedOperationException("Events of type [Selector] are no longer supported. Use string ids")
    }
}
