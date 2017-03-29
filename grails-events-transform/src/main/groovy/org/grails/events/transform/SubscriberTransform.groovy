package org.grails.events.transform

import grails.events.transform.Subscriber
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.trait.Traits
import org.grails.datastore.gorm.transform.AbstractTraitApplyingGormASTTransformation

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import static org.grails.datastore.mapping.reflect.AstUtils.*
import static org.codehaus.groovy.ast.tools.GeneralUtils.*
/**
 * An AST transformation that adds the {@link MethodRegisteringSubscriber}
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class SubscriberTransform extends AbstractTraitApplyingGormASTTransformation {

    public static final Object APPLIED_MARKER = new Object()


    @Override
    protected Class getTraitClass() {
        return MethodRegisteringSubscriber
    }

    @Override
    protected ClassNode getAnnotationType() {
        return ClassHelper.make(Subscriber)
    }

    @Override
    void visit(SourceUnit source, AnnotationNode annotationNode, AnnotatedNode annotatedNode) {
        if(annotatedNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)annotatedNode
            ClassNode declaringClass = methodNode.getDeclaringClass()
            if ( shouldWeave(annotationNode, declaringClass) ) {
                weaveTrait(declaringClass, source, traitClass)
            }

            MethodNode getSubscribersMethod = declaringClass.getDeclaredMethod("getSubscribedMethods")
            ListExpression listExpression
            if(getSubscribersMethod == null) {
                def listOfMethodType = GenericsUtils.makeClassSafeWithGenerics(List, ClassHelper.make(Method))
                listExpression = new ListExpression()
                ExpressionStatement body = new ExpressionStatement(listExpression)
                declaringClass.addMethod("getSubscribedMethods", Modifier.PUBLIC, listOfMethodType, ZERO_PARAMETERS, null, body)
            }
            else if(getSubscribersMethod.getAnnotations(ClassHelper.make(Traits.TraitBridge))) {
                def currentCode = getSubscribersMethod.code
                if(currentCode instanceof ExpressionStatement) {
                    ExpressionStatement body = (ExpressionStatement) currentCode

                    def expression = body.getExpression()
                    if(expression instanceof ListExpression) {
                        listExpression  = (ListExpression) expression
                    }
                    else {
                        listExpression = new ListExpression()
                        body.setExpression(listExpression)
                    }
                }
                else {
                    listExpression = new ListExpression()
                    ExpressionStatement body = new ExpressionStatement(listExpression)
                    getSubscribersMethod.setCode(body)
                }
            }
            else {
                ExpressionStatement body = (ExpressionStatement) getSubscribersMethod.getCode()
                listExpression  = (ListExpression) body.getExpression()
            }
            ArgumentListExpression methodArgs = args(
                    constX(methodNode.getName())
            )
            for(param in methodNode.parameters) {
                methodArgs.addExpression( classX(param.type) )
            }
            listExpression.addExpression(callX(
                    callThisX("getClass"), "getMethod", methodArgs)
            )
        }
    }

    @Override
    protected Object getAppliedMarker() {
        return APPLIED_MARKER
    }
}
