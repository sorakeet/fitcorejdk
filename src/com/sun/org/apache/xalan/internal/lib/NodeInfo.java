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
 * $Id: NodeInfo.java,v 1.2.4.1 2005/09/10 18:54:37 jeffsuttor Exp $
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
 * $Id: NodeInfo.java,v 1.2.4.1 2005/09/10 18:54:37 jeffsuttor Exp $
 */
package com.sun.org.apache.xalan.internal.lib;

import com.sun.org.apache.xalan.internal.extensions.ExpressionContext;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.SourceLocator;

public class NodeInfo{
    public static String systemId(ExpressionContext context){
        Node contextNode=context.getContextNode();
        int nodeHandler=((DTMNodeProxy)contextNode).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)contextNode).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getSystemId();
        else
            return null;
    }

    public static String systemId(NodeList nodeList){
        if(nodeList==null||nodeList.getLength()==0)
            return null;
        Node node=nodeList.item(0);
        int nodeHandler=((DTMNodeProxy)node).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)node).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getSystemId();
        else
            return null;
    }

    public static String publicId(ExpressionContext context){
        Node contextNode=context.getContextNode();
        int nodeHandler=((DTMNodeProxy)contextNode).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)contextNode).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getPublicId();
        else
            return null;
    }

    public static String publicId(NodeList nodeList){
        if(nodeList==null||nodeList.getLength()==0)
            return null;
        Node node=nodeList.item(0);
        int nodeHandler=((DTMNodeProxy)node).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)node).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getPublicId();
        else
            return null;
    }

    public static int lineNumber(ExpressionContext context){
        Node contextNode=context.getContextNode();
        int nodeHandler=((DTMNodeProxy)contextNode).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)contextNode).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getLineNumber();
        else
            return -1;
    }

    public static int lineNumber(NodeList nodeList){
        if(nodeList==null||nodeList.getLength()==0)
            return -1;
        Node node=nodeList.item(0);
        int nodeHandler=((DTMNodeProxy)node).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)node).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getLineNumber();
        else
            return -1;
    }

    public static int columnNumber(ExpressionContext context){
        Node contextNode=context.getContextNode();
        int nodeHandler=((DTMNodeProxy)contextNode).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)contextNode).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getColumnNumber();
        else
            return -1;
    }

    public static int columnNumber(NodeList nodeList){
        if(nodeList==null||nodeList.getLength()==0)
            return -1;
        Node node=nodeList.item(0);
        int nodeHandler=((DTMNodeProxy)node).getDTMNodeNumber();
        SourceLocator locator=((DTMNodeProxy)node).getDTM()
                .getSourceLocatorFor(nodeHandler);
        if(locator!=null)
            return locator.getColumnNumber();
        else
            return -1;
    }
}
