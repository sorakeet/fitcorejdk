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
package com.sun.org.apache.xpath.internal.objects;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xpath.internal.XPathContext;

public final class DTMXRTreeFrag{
    private DTM m_dtm;
    private int m_dtmIdentity=DTM.NULL;
    private XPathContext m_xctxt;

    public DTMXRTreeFrag(int dtmIdentity,XPathContext xctxt){
        m_xctxt=xctxt;
        m_dtmIdentity=dtmIdentity;
        m_dtm=xctxt.getDTM(dtmIdentity);
    }

    public final void destruct(){
        m_dtm=null;
        m_xctxt=null;
    }

    final DTM getDTM(){
        return m_dtm;
    }

    final XPathContext getXPathContext(){
        return m_xctxt;
    }

    public final int hashCode(){
        return m_dtmIdentity;
    }

    public final boolean equals(Object obj){
        if(obj instanceof DTMXRTreeFrag){
            return (m_dtmIdentity==((DTMXRTreeFrag)obj).getDTMIdentity());
        }
        return false;
    }

    public final int getDTMIdentity(){
        return m_dtmIdentity;
    }
}
