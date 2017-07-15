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
 * $Id: FuncDoclocation.java,v 1.2.4.1 2005/09/14 19:53:44 jeffsuttor Exp $
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
 * $Id: FuncDoclocation.java,v 1.2.4.1 2005/09/14 19:53:44 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.objects.XString;

public class FuncDoclocation extends FunctionDef1Arg{
    static final long serialVersionUID=7469213946343568769L;

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        int whereNode=getArg0AsNode(xctxt);
        String fileLocation=null;
        if(DTM.NULL!=whereNode){
            DTM dtm=xctxt.getDTM(whereNode);
            // %REVIEW%
            if(DTM.DOCUMENT_FRAGMENT_NODE==dtm.getNodeType(whereNode)){
                whereNode=dtm.getFirstChild(whereNode);
            }
            if(DTM.NULL!=whereNode){
                fileLocation=dtm.getDocumentBaseURI();
//        int owner = dtm.getDocument();
//        fileLocation = xctxt.getSourceTreeManager().findURIFromDoc(owner);
            }
        }
        return new XString((null!=fileLocation)?fileLocation:"");
    }
}
