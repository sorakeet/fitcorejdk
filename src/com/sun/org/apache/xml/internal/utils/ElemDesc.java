/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: ElemDesc.java,v 1.2.4.1 2005/09/15 08:15:44 suresh_emailid Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: ElemDesc.java,v 1.2.4.1 2005/09/15 08:15:44 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

import java.util.HashMap;
import java.util.Map;

class ElemDesc{
    static final int EMPTY=(1<<1);
    static final int FLOW=(1<<2);
    static final int BLOCK=(1<<3);
    static final int BLOCKFORM=(1<<4);
    static final int BLOCKFORMFIELDSET=(1<<5);
    static final int CDATA=(1<<6);
    static final int PCDATA=(1<<7);
    static final int RAW=(1<<8);
    static final int INLINE=(1<<9);
    static final int INLINEA=(1<<10);
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
    static final int ATTRURL=(1<<1);
    static final int ATTREMPTY=(1<<2);
    Map<String,Integer> m_attrs=null;
    int m_flags;

    ElemDesc(int flags){
        m_flags=flags;
    }

    boolean is(int flags){
        // int which = (m_flags & flags);
        return (m_flags&flags)!=0;
    }

    void setAttr(String name,int flags){
        if(null==m_attrs)
            m_attrs=new HashMap<>();
        m_attrs.put(name,flags);
    }

    boolean isAttrFlagSet(String name,int flags){
        if(null!=m_attrs){
            Integer _flags=m_attrs.get(name);
            if(null!=_flags){
                return (_flags&flags)!=0;
            }
        }
        return false;
    }
}
