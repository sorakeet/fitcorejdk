/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.tree;

@jdk.Exported
public interface TreeVisitor<R,P>{
    R visitAnnotatedType(AnnotatedTypeTree node,P p);

    R visitAnnotation(AnnotationTree node,P p);

    R visitMethodInvocation(MethodInvocationTree node,P p);

    R visitAssert(AssertTree node,P p);

    R visitAssignment(AssignmentTree node,P p);

    R visitCompoundAssignment(CompoundAssignmentTree node,P p);

    R visitBinary(BinaryTree node,P p);

    R visitBlock(BlockTree node,P p);

    R visitBreak(BreakTree node,P p);

    R visitCase(CaseTree node,P p);

    R visitCatch(CatchTree node,P p);

    R visitClass(ClassTree node,P p);

    R visitConditionalExpression(ConditionalExpressionTree node,P p);

    R visitContinue(ContinueTree node,P p);

    R visitDoWhileLoop(DoWhileLoopTree node,P p);

    R visitErroneous(ErroneousTree node,P p);

    R visitExpressionStatement(ExpressionStatementTree node,P p);

    R visitEnhancedForLoop(EnhancedForLoopTree node,P p);

    R visitForLoop(ForLoopTree node,P p);

    R visitIdentifier(IdentifierTree node,P p);

    R visitIf(IfTree node,P p);

    R visitImport(ImportTree node,P p);

    R visitArrayAccess(ArrayAccessTree node,P p);

    R visitLabeledStatement(LabeledStatementTree node,P p);

    R visitLiteral(LiteralTree node,P p);

    R visitMethod(MethodTree node,P p);

    R visitModifiers(ModifiersTree node,P p);

    R visitNewArray(NewArrayTree node,P p);

    R visitNewClass(NewClassTree node,P p);

    R visitLambdaExpression(LambdaExpressionTree node,P p);

    R visitParenthesized(ParenthesizedTree node,P p);

    R visitReturn(ReturnTree node,P p);

    R visitMemberSelect(MemberSelectTree node,P p);

    R visitMemberReference(MemberReferenceTree node,P p);

    R visitEmptyStatement(EmptyStatementTree node,P p);

    R visitSwitch(SwitchTree node,P p);

    R visitSynchronized(SynchronizedTree node,P p);

    R visitThrow(ThrowTree node,P p);

    R visitCompilationUnit(CompilationUnitTree node,P p);

    R visitTry(TryTree node,P p);

    R visitParameterizedType(ParameterizedTypeTree node,P p);

    R visitUnionType(UnionTypeTree node,P p);

    R visitIntersectionType(IntersectionTypeTree node,P p);

    R visitArrayType(ArrayTypeTree node,P p);

    R visitTypeCast(TypeCastTree node,P p);

    R visitPrimitiveType(PrimitiveTypeTree node,P p);

    R visitTypeParameter(TypeParameterTree node,P p);

    R visitInstanceOf(InstanceOfTree node,P p);

    R visitUnary(UnaryTree node,P p);

    R visitVariable(VariableTree node,P p);

    R visitWhileLoop(WhileLoopTree node,P p);

    R visitWildcard(WildcardTree node,P p);

    R visitOther(Tree node,P p);
}
