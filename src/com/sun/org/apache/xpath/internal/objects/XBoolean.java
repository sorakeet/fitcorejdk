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
 * $Id: XBoolean.java,v 1.2.4.2 2005/09/14 20:34:45 jeffsuttor Exp $
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
 * $Id: XBoolean.java,v 1.2.4.2 2005/09/14 20:34:45 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal.objects;

public class XBoolean extends XObject{
    public static final XBoolean S_TRUE=new XBooleanStatic(true);
    public static final XBoolean S_FALSE=new XBooleanStatic(false);
    static final long serialVersionUID=-2964933058866100881L;
    private final boolean m_val;

    public XBoolean(boolean b){
        super();
        m_val=b;
    }

    public XBoolean(Boolean b){
        super();
        m_val=b.booleanValue();
        setObject(b);
    }

    public int getType(){
        return CLASS_BOOLEAN;
    }

    public String getTypeString(){
        return "#BOOLEAN";
    }

    public double num(){
        return m_val?1.0:0.0;
    }

    public boolean bool(){
        return m_val;
    }

    public String str(){
        return m_val?"true":"false";
    }

    public Object object(){
        if(null==m_obj)
            setObject(new Boolean(m_val));
        return m_obj;
    }

    public boolean equals(XObject obj2){
        // In order to handle the 'all' semantics of
        // nodeset comparisons, we always call the
        // nodeset function.
        if(obj2.getType()==XObject.CLASS_NODESET)
            return obj2.equals(this);
        try{
            return m_val==obj2.bool();
        }catch(javax.xml.transform.TransformerException te){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(te);
        }
    }
}
