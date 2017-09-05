package org.grails.events

import groovy.transform.CompileStatic

@CompileStatic
class EventIdUtils {
    static String eventIdForMethodName(String methodName) {
        if(methodName ==~ /on[A-Z]\S*/) {
            String methodNameWithoutPreffix = methodName.substring(2)
            return "${methodNameWithoutPreffix[0].toLowerCase(Locale.ENGLISH)}${methodNameWithoutPreffix.substring(1)}"
        }
        methodName
    }
}
