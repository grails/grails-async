package grails.events.subscriber

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Method

/**
 * Invokes a method to trigger an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@Slf4j
@EqualsAndHashCode(includes = ['target', 'method'])
@ToString(includes = ['method'])
class MethodSubscriber implements Subscriber {

    final Object target
    final Method method
    final Class[] parameterTypes
    final int parameterLength

    ConversionService conversionService = new DefaultConversionService()

    MethodSubscriber(Object target, Method method) {
        this.target = target
        this.method = method
        this.parameterTypes = method.parameterTypes
        this.parameterLength = parameterTypes.length

        if(target.getClass() != method.getDeclaringClass()) {
            throw new IllegalArgumentException("The target must be an instance of the declaring class for method $method")
        }
    }

    @Override
    Object call(Object arg) {
        switch (parameterLength) {
            case 0:
                return ReflectionUtils.invokeMethod(method, target)
            case 1:
                Class parameterType = parameterTypes[0]
                if(parameterType.isInstance(arg)) {
                    return ReflectionUtils.invokeMethod(method, target, arg)
                }
                else {
                    def converted = conversionService.canConvert(arg.getClass(), parameterType) ? conversionService.convert(arg, parameterType) : null
                    if(converted != null) {
                        return ReflectionUtils.invokeMethod(method, target, converted)
                    }
                    else {
                        log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                        break
                    }
                }
            default:
                if(arg != null && arg.getClass().isArray()) {
                    Object[] array = (Object[]) arg

                    if(array.length == parameterLength) {
                        Object[] converted = new Object[array.length]
                        int i = 0
                        for(o in array) {
                            Class parameterType = parameterTypes[i]
                            if(parameterType.isInstance(o)) {
                                converted[i] = array[i]
                            }
                            else {
                                converted[i] = conversionService.convert(o, parameterType)
                            }
                            i++
                        }
                        return ReflectionUtils.invokeMethod(method, target, converted)
                    }
                    else {
                        log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                        break
                    }
                }
                else {
                    log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                    break
                }
        }

    }
}
