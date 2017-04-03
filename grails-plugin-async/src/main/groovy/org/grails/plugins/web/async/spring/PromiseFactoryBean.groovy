package org.grails.plugins.web.async.spring

import grails.async.PromiseFactory
import groovy.transform.CompileStatic
import org.grails.async.factory.PromiseFactoryBuilder
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean

/**
 * Factory bean for Spring integration
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class PromiseFactoryBean extends PromiseFactoryBuilder implements FactoryBean<PromiseFactory> {
    @Override
    PromiseFactory getObject() throws Exception {
        return build()
    }

    @Override
    Class<?> getObjectType() {
        return PromiseFactory
    }

    @Override
    boolean isSingleton() {
        return true
    }

}
