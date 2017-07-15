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
 * $Id: ElemDesc.java,v 1.2.4.1 2005/09/15 08:15:15 suresh_emailid Exp $
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
 * $Id: ElemDesc.java,v 1.2.4.1 2005/09/15 08:15:15 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import com.sun.org.apache.xml.internal.serializer.utils.StringToIntTable;

public final class ElemDesc{
    public static final int ATTRURL=(1<<1);
    public static final int ATTREMPTY=(1<<2);
    static final int EMPTY=(1<<1);
    static final int BLOCK=(1<<3);
    static final int BLOCKFORM=(1<<4);
    static final int BLOCKFORMFIELDSET=(1<<5);
    static final int RAW=(1<<8);
    static final int INLINELABEL=(1<<11);
    static final int FONTSTYLE=(1<<12);
    static final int PHRASE=(1<<13);
    static final int FORMCTRL=(1<<14);
    static final int SPECIAL=(1<<15);
    static final int ASPECIAL=(1<<16);
    static final int HEADMISC=(1<<17);
    static final int HEAD=(1<<18);
    static final int LIST=(1<<19);
    static final int PREFORMATTED=(1<<20);
    static final int WHITESPACESENSITIVE=(1<<21);
    static final int HEADELEM=(1<<22);
    private static final int FLOW=(1<<2);
    private static final int CDATA=(1<<6);
    private static final int PCDATA=(1<<7);
    private static final int INLINE=(1<<9);
    private static final int INLINEA=(1<<10);
    private static final int HTMLELEM=(1<<23);
    private int m_flags;
    private StringToIntTable m_attrs=null;

    ElemDesc(int flags){
        m_flags=flags;
    }

    private boolean is(int flags){
        // int which = (m_flags & flags);
        return (m_flags&flags)!=0;
    }

    int getFlags(){
        return m_flags;
    }

    void setAttr(String name,int flags){
        if(null==m_attrs)
            m_attrs=new StringToIntTable();
        m_attrs.put(name,flags);
    }

    public boolean isAttrFlagSet(String name,int flags){
        return (null!=m_attrs)
                ?((m_attrs.getIgnoreCase(name)&flags)!=0)
                :false;
    }
}
