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
 * $Id: XObjectFactory.java,v 1.1.2.1 2005/08/01 01:29:30 jeffsuttor Exp $
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
 * $Id: XObjectFactory.java,v 1.1.2.1 2005/08/01 01:29:30 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.axes.OneStepIterator;

public class XObjectFactory{
    static public XObject create(Object val){
        XObject result;
        if(val instanceof XObject){
            result=(XObject)val;
        }else if(val instanceof String){
            result=new XString((String)val);
        }else if(val instanceof Boolean){
            result=new XBoolean((Boolean)val);
        }else if(val instanceof Double){
            result=new XNumber(((Double)val));
        }else{
            result=new XObject(val);
        }
        return result;
    }

    static public XObject create(Object val,XPathContext xctxt){
        XObject result;
        if(val instanceof XObject){
            result=(XObject)val;
        }else if(val instanceof String){
            result=new XString((String)val);
        }else if(val instanceof Boolean){
            result=new XBoolean((Boolean)val);
        }else if(val instanceof Number){
            result=new XNumber(((Number)val));
        }else if(val instanceof DTM){
            DTM dtm=(DTM)val;
            try{
                int dtmRoot=dtm.getDocument();
                DTMAxisIterator iter=dtm.getAxisIterator(Axis.SELF);
                iter.setStartNode(dtmRoot);
                DTMIterator iterator=new OneStepIterator(iter,Axis.SELF);
                iterator.setRoot(dtmRoot,xctxt);
                result=new XNodeSet(iterator);
            }catch(Exception ex){
                throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(ex);
            }
        }else if(val instanceof DTMAxisIterator){
            DTMAxisIterator iter=(DTMAxisIterator)val;
            try{
                DTMIterator iterator=new OneStepIterator(iter,Axis.SELF);
                iterator.setRoot(iter.getStartNode(),xctxt);
                result=new XNodeSet(iterator);
            }catch(Exception ex){
                throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(ex);
            }
        }else if(val instanceof DTMIterator){
            result=new XNodeSet((DTMIterator)val);
        }
        // This next three instanceofs are a little worrysome, since a NodeList
        // might also implement a Node!
        else if(val instanceof org.w3c.dom.Node){
            result=new XNodeSetForDOM((org.w3c.dom.Node)val,xctxt);
        }
        // This must come after org.w3c.dom.Node, since many Node implementations
        // also implement NodeList.
        else if(val instanceof org.w3c.dom.NodeList){
            result=new XNodeSetForDOM((org.w3c.dom.NodeList)val,xctxt);
        }else if(val instanceof org.w3c.dom.traversal.NodeIterator){
            result=new XNodeSetForDOM((org.w3c.dom.traversal.NodeIterator)val,xctxt);
        }else{
            result=new XObject(val);
        }
        return result;
    }
}
