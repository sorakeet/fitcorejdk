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
 * $Id: NSInfo.java,v 1.2.4.1 2005/09/15 08:15:48 suresh_emailid Exp $
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
 * $Id: NSInfo.java,v 1.2.4.1 2005/09/15 08:15:48 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.utils;

public class NSInfo{
    public static final int ANCESTORXMLNSUNPROCESSED=0;
    // Unused at the moment
    public static final int ANCESTORHASXMLNS=1;
    public static final int ANCESTORNOXMLNS=2;
    public String m_namespace;
    public boolean m_hasXMLNSAttrs;
    public boolean m_hasProcessedNS;
    public int m_ancestorHasXMLNSAttrs;
    public NSInfo(boolean hasProcessedNS,boolean hasXMLNSAttrs){
        m_hasProcessedNS=hasProcessedNS;
        m_hasXMLNSAttrs=hasXMLNSAttrs;
        m_namespace=null;
        m_ancestorHasXMLNSAttrs=ANCESTORXMLNSUNPROCESSED;
    }
    public NSInfo(boolean hasProcessedNS,boolean hasXMLNSAttrs,
                  int ancestorHasXMLNSAttrs){
        m_hasProcessedNS=hasProcessedNS;
        m_hasXMLNSAttrs=hasXMLNSAttrs;
        m_ancestorHasXMLNSAttrs=ancestorHasXMLNSAttrs;
        m_namespace=null;
    }
    public NSInfo(String namespace,boolean hasXMLNSAttrs){
        m_hasProcessedNS=true;
        m_hasXMLNSAttrs=hasXMLNSAttrs;
        m_namespace=namespace;
        m_ancestorHasXMLNSAttrs=ANCESTORXMLNSUNPROCESSED;
    }
}
