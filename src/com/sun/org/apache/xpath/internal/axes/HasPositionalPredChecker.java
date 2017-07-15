/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: HasPositionalPredChecker.java,v 1.1.2.1 2005/08/01 01:30:24 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id: HasPositionalPredChecker.java,v 1.1.2.1 2005/08/01 01:30:24 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.axes;

import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.ExpressionOwner;
import com.sun.org.apache.xpath.internal.XPathVisitor;
import com.sun.org.apache.xpath.internal.functions.FuncLast;
import com.sun.org.apache.xpath.internal.functions.FuncPosition;
import com.sun.org.apache.xpath.internal.functions.Function;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.operations.*;

public class HasPositionalPredChecker extends XPathVisitor{
    private boolean m_hasPositionalPred=false;
    private int m_predDepth=0;

    public static boolean check(LocPathIterator path){
        HasPositionalPredChecker hppc=new HasPositionalPredChecker();
        path.callVisitors(null,hppc);
        return hppc.m_hasPositionalPred;
    }

    public boolean visitPredicate(ExpressionOwner owner,Expression pred){
        m_predDepth++;
        if(m_predDepth==1){
            if((pred instanceof Variable)||
                    (pred instanceof XNumber)||
                    (pred instanceof Div)||
                    (pred instanceof Plus)||
                    (pred instanceof Minus)||
                    (pred instanceof Mod)||
                    (pred instanceof Quo)||
                    (pred instanceof Mult)||
                    (pred instanceof com.sun.org.apache.xpath.internal.operations.Number)||
                    (pred instanceof Function))
                m_hasPositionalPred=true;
            else
                pred.callVisitors(owner,this);
        }
        m_predDepth--;
        // Don't go have the caller go any further down the subtree.
        return false;
    }
//      /**
//       * Visit a variable reference.
//       * @param owner The owner of the expression, to which the expression can
//       *              be reset if rewriting takes place.
//       * @param var The variable reference object.
//       * @return true if the sub expressions should be traversed.
//       */
//      public boolean visitVariableRef(ExpressionOwner owner, Variable var)
//      {
//              m_hasPositionalPred = true;
//              return true;
//      }

    public boolean visitFunction(ExpressionOwner owner,Function func){
        if((func instanceof FuncPosition)||
                (func instanceof FuncLast))
            m_hasPositionalPred=true;
        return true;
    }
}
