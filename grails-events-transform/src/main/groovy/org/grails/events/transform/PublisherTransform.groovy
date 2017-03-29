package org.grails.events.transform

import grails.async.events.Event
import grails.async.events.EventPublisher
import grails.events.transform.Publisher
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.trait.TraitComposer
import org.grails.datastore.gorm.transactions.transform.TransactionalTransform
import org.grails.datastore.gorm.transform.AbstractMethodDecoratingTransformation
import org.grails.datastore.mapping.core.Ordered
import org.grails.datastore.mapping.reflect.AstUtils
import org.springframework.transaction.event.TransactionPhase

import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * A transform that transforms a method publishing the result to the given event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class PublisherTransform extends AbstractMethodDecoratingTransformation implements Ordered {
    /**
     * The position of the transform. Before the transactional transform
     */
    public static final int POSITION = TransactionalTransform.POSITION - 50
    public static final Object APPLIED_MARKER = new Object()

    @Override
    int getOrder() {
        return POSITION
    }

    @Override
    protected String getRenamedMethodPrefix() {
        return '$pub__'
    }

    @Override
    protected void enhanceClassNode(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode) {
        if(!AstUtils.implementsInterface(classNode, EventPublisher.name)) {
            classNode.addInterface(ClassHelper.make(EventPublisher))
            if (compilationUnit != null) {
                TraitComposer.doExtendTraits(classNode, sourceUnit, compilationUnit)
            }
        }
    }

    @Override
    protected MethodCallExpression buildDelegatingMethodCall(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode, MethodNode methodNode, MethodCallExpression originalMethodCallExpr, BlockStatement newMethodBody) {
        Expression resultValue
        if(methodNode.returnType != ClassHelper.VOID_TYPE) {
            resultValue = originalMethodCallExpr
        }
        else {
            resultValue = ConstantExpression.NULL
        }
        Expression result = varX('$result')
        newMethodBody.addStatement(
            declS(result, resultValue)
        )
        Expression eventId = annotationNode.getMember("value")
        Expression phase = annotationNode.getMember("phase")
        MapExpression params = new MapExpression()
        for(param in methodNode.parameters) {
            params.addMapEntryExpression(
                constX(param.name),
                varX(param)
            )
        }
        Expression newEvent = ctorX(ClassHelper.make(Event), args(eventId, params, resultValue))
        def args = args(newEvent)
        if(phase != null) {
            args.addExpression(phase)
        }
        else {
            if( AstUtils.hasAnnotation(methodNode, Transactional) ) {
                args.addExpression(propX(classX(TransactionPhase), "AFTER_COMMIT"))
            }
        }

        newMethodBody.addStatement(
            ifS(notNullX(result), stmt( callThisX("publish", args) ))

        )
        return callX(result, "find")
    }

    @Override
    protected ClassNode getAnnotationType() {
        return ClassHelper.make(Publisher)
    }

    @Override
    protected Object getAppliedMarker() {
        return APPLIED_MARKER
    }
}
