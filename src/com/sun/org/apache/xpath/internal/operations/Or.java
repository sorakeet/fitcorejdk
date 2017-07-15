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
 * $Id: Or.java,v 1.2.4.1 2005/09/14 21:31:41 jeffsuttor Exp $
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
 * $Id: Or.java,v 1.2.4.1 2005/09/14 21:31:41 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.operations;

import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.objects.XBoolean;
import com.sun.org.apache.xpath.internal.objects.XObject;

public class Or extends Operation{
    static final long serialVersionUID=-644107191353853079L;

    public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException{
        XObject expr1=m_left.execute(xctxt);
        if(!expr1.bool()){
            XObject expr2=m_right.execute(xctxt);
            return expr2.bool()?XBoolean.S_TRUE:XBoolean.S_FALSE;
        }else
            return XBoolean.S_TRUE;
    }

    public boolean bool(XPathContext xctxt)
            throws javax.xml.transform.TransformerException{
        return (m_left.bool(xctxt)||m_right.bool(xctxt));
    }
}
