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
 * $Id: FuncCount.java,v 1.2.4.1 2005/09/14 19:53:45 jeffsuttor Exp $
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
 * $Id: FuncCount.java,v 1.2.4.1 2005/09/14 19:53:45 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.functions;

import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XNumber;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class FuncCount extends FunctionOneArg{
    static final long serialVersionUID=-7116225100474153751L;

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
//    DTMIterator nl = m_arg0.asIterator(xctxt, xctxt.getCurrentNode());
//    // We should probably make a function on the iterator for this,
//    // as a given implementation could optimize.
//    int i = 0;
//
//    while (DTM.NULL != nl.nextNode())
//    {
//      i++;
//    }
//    nl.detach();
        DTMIterator nl=m_arg0.asIterator(xctxt,xctxt.getCurrentNode());
        int i=nl.getLength();
        nl.detach();
        return new XNumber((double)i);
    }
}