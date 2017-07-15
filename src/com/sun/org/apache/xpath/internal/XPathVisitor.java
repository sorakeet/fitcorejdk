/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: XPathVisitor.java,v 1.1.2.1 2005/08/01 01:30:11 jeffsuttor Exp $
 */
/**
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: XPathVisitor.java,v 1.1.2.1 2005/08/01 01:30:11 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xpath.internal.axes.LocPathIterator;
import com.sun.org.apache.xpath.internal.axes.UnionPathIterator;
import com.sun.org.apache.xpath.internal.functions.Function;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XString;
import com.sun.org.apache.xpath.internal.operations.Operation;
import com.sun.org.apache.xpath.internal.operations.UnaryOperation;
import com.sun.org.apache.xpath.internal.operations.Variable;
import com.sun.org.apache.xpath.internal.patterns.NodeTest;
import com.sun.org.apache.xpath.internal.patterns.StepPattern;
import com.sun.org.apache.xpath.internal.patterns.UnionPattern;

public class XPathVisitor{
    public boolean visitLocationPath(ExpressionOwner owner,LocPathIterator path){
        return true;
    }

    public boolean visitUnionPath(ExpressionOwner owner,UnionPathIterator path){
        return true;
    }

    public boolean visitStep(ExpressionOwner owner,NodeTest step){
        return true;
    }

    public boolean visitPredicate(ExpressionOwner owner,Expression pred){
        return true;
    }

    public boolean visitBinaryOperation(ExpressionOwner owner,Operation op){
        return true;
    }

    public boolean visitUnaryOperation(ExpressionOwner owner,UnaryOperation op){
        return true;
    }

    public boolean visitVariableRef(ExpressionOwner owner,Variable var){
        return true;
    }

    public boolean visitFunction(ExpressionOwner owner,Function func){
        return true;
    }

    public boolean visitMatchPattern(ExpressionOwner owner,StepPattern pattern){
        return true;
    }

    public boolean visitUnionPattern(ExpressionOwner owner,UnionPattern pattern){
        return true;
    }

    public boolean visitStringLiteral(ExpressionOwner owner,XString str){
        return true;
    }

    public boolean visitNumberLiteral(ExpressionOwner owner,XNumber num){
        return true;
    }
}
