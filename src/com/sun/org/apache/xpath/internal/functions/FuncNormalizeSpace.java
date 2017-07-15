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
 * $Id: FuncNormalizeSpace.java,v 1.2.4.1 2005/09/14 20:18:46 jeffsuttor Exp $
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
 * $Id: FuncNormalizeSpace.java,v 1.2.4.1 2005/09/14 20:18:46 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.utils.XMLString;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.objects.XString;
import org.xml.sax.ContentHandler;

public class FuncNormalizeSpace extends FunctionDef1Arg{
    static final long serialVersionUID=-3377956872032190880L;

    public void executeCharsToContentHandler(XPathContext xctxt,
                                             ContentHandler handler)
            throws javax.xml.transform.TransformerException,
            org.xml.sax.SAXException{
        if(Arg0IsNodesetExpr()){
            int node=getArg0AsNode(xctxt);
            if(DTM.NULL!=node){
                DTM dtm=xctxt.getDTM(node);
                dtm.dispatchCharactersEvents(node,handler,true);
            }
        }else{
            XObject obj=execute(xctxt);
            obj.dispatchCharactersEvents(handler);
        }
    }

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        XMLString s1=getArg0AsString(xctxt);
        return (XString)s1.fixWhiteSpace(true,true,false);
    }
}
