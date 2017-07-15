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
 * $Id: XBooleanStatic.java,v 1.2.4.2 2005/09/14 20:34:46 jeffsuttor Exp $
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
 * $Id: XBooleanStatic.java,v 1.2.4.2 2005/09/14 20:34:46 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

public class XBooleanStatic extends XBoolean{
    static final long serialVersionUID=-8064147275772687409L;
    private final boolean m_val;

    public XBooleanStatic(boolean b){
        super(b);
        m_val=b;
    }

    public boolean equals(XObject obj2){
        try{
            return m_val==obj2.bool();
        }catch(javax.xml.transform.TransformerException te){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
        }
    }
}
