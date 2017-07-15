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
 * $Id: FuncId.java,v 1.2.4.1 2005/09/14 20:18:45 jeffsuttor Exp $
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
 * $Id: FuncId.java,v 1.2.4.1 2005/09/14 20:18:45 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.utils.StringVector;
import com.sun.org.apache.xpath.internal.NodeSetDTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XNodeSet;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import java.util.StringTokenizer;

public class FuncId extends FunctionOneArg{
    static final long serialVersionUID=8930573966143567310L;

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        int context=xctxt.getCurrentNode();
        DTM dtm=xctxt.getDTM(context);
        int docContext=dtm.getDocument();
        if(DTM.NULL==docContext)
            error(xctxt,XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC,null);
        XObject arg=m_arg0.execute(xctxt);
        int argType=arg.getType();
        XNodeSet nodes=new XNodeSet(xctxt.getDTMManager());
        NodeSetDTM nodeSet=nodes.mutableNodeset();
        if(XObject.CLASS_NODESET==argType){
            DTMIterator ni=arg.iter();
            StringVector usedrefs=null;
            int pos=ni.nextNode();
            while(DTM.NULL!=pos){
                DTM ndtm=ni.getDTM(pos);
                String refval=ndtm.getStringValue(pos).toString();
                pos=ni.nextNode();
                usedrefs=getNodesByID(xctxt,docContext,refval,usedrefs,nodeSet,
                        DTM.NULL!=pos);
            }
            // ni.detach();
        }else if(XObject.CLASS_NULL==argType){
            return nodes;
        }else{
            String refval=arg.str();
            getNodesByID(xctxt,docContext,refval,null,nodeSet,false);
        }
        return nodes;
    }

    private StringVector getNodesByID(XPathContext xctxt,int docContext,
                                      String refval,StringVector usedrefs,
                                      NodeSetDTM nodeSet,boolean mayBeMore){
        if(null!=refval){
            String ref=null;
//      DOMHelper dh = xctxt.getDOMHelper();
            StringTokenizer tokenizer=new StringTokenizer(refval);
            boolean hasMore=tokenizer.hasMoreTokens();
            DTM dtm=xctxt.getDTM(docContext);
            while(hasMore){
                ref=tokenizer.nextToken();
                hasMore=tokenizer.hasMoreTokens();
                if((null!=usedrefs)&&usedrefs.contains(ref)){
                    ref=null;
                    continue;
                }
                int node=dtm.getElementById(ref);
                if(DTM.NULL!=node)
                    nodeSet.addNodeInDocOrder(node,xctxt);
                if((null!=ref)&&(hasMore||mayBeMore)){
                    if(null==usedrefs)
                        usedrefs=new StringVector();
                    usedrefs.addElement(ref);
                }
            }
        }
        return usedrefs;
    }
}
