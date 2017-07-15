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
 * $Id: FunctionDef1Arg.java,v 1.2.4.1 2005/09/14 20:18:42 jeffsuttor Exp $
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
 * $Id: FunctionDef1Arg.java,v 1.2.4.1 2005/09/14 20:18:42 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XString;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

public class FunctionDef1Arg extends FunctionOneArg{
    static final long serialVersionUID=2325189412814149264L;

    protected int getArg0AsNode(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return (null==m_arg0)
                ?xctxt.getCurrentNode():m_arg0.asNode(xctxt);
    }

    public boolean Arg0IsNodesetExpr(){
        return (null==m_arg0)?true:m_arg0.isNodesetExpr();
    }

    protected XMLString getArg0AsString(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        if(null==m_arg0){
            int currentNode=xctxt.getCurrentNode();
            if(DTM.NULL==currentNode)
                return XString.EMPTYSTRING;
            else{
                DTM dtm=xctxt.getDTM(currentNode);
                return dtm.getStringValue(currentNode);
            }
        }else
            return m_arg0.execute(xctxt).xstr();
    }

    protected double getArg0AsNumber(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        if(null==m_arg0){
            int currentNode=xctxt.getCurrentNode();
            if(DTM.NULL==currentNode)
                return 0;
            else{
                DTM dtm=xctxt.getDTM(currentNode);
                XMLString str=dtm.getStringValue(currentNode);
                return str.toDouble();
            }
        }else
            return m_arg0.execute(xctxt).num();
    }

    public void checkNumberArgs(int argNum) throws WrongNumberArgsException{
        if(argNum>1)
            reportWrongNumberArgs();
    }

    protected void reportWrongNumberArgs() throws WrongNumberArgsException{
        throw new WrongNumberArgsException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_ZERO_OR_ONE,null)); //"0 or 1");
    }

    public boolean canTraverseOutsideSubtree(){
        return (null==m_arg0)?false:super.canTraverseOutsideSubtree();
    }
}
