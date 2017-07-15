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
 * $Id: DTMFilter.java,v 1.2.4.1 2005/09/15 08:14:53 suresh_emailid Exp $
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
 * $Id: DTMFilter.java,v 1.2.4.1 2005/09/15 08:14:53 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

public interface DTMFilter{
    // Constants for whatToShow.  These are used to set the node type that will
    // be traversed. These values may be ORed together before being passed to
    // the DTMIterator.
    public static final int SHOW_ALL=0xFFFFFFFF;
    public static final int SHOW_ELEMENT=0x00000001;
    public static final int SHOW_ATTRIBUTE=0x00000002;
    public static final int SHOW_TEXT=0x00000004;
    public static final int SHOW_CDATA_SECTION=0x00000008;
    public static final int SHOW_ENTITY_REFERENCE=0x00000010;
    public static final int SHOW_ENTITY=0x00000020;
    public static final int SHOW_PROCESSING_INSTRUCTION=0x00000040;
    public static final int SHOW_COMMENT=0x00000080;
    public static final int SHOW_DOCUMENT=0x00000100;
    public static final int SHOW_DOCUMENT_TYPE=0x00000200;
    public static final int SHOW_DOCUMENT_FRAGMENT=0x00000400;
    public static final int SHOW_NOTATION=0x00000800;
    public static final int SHOW_NAMESPACE=0x00001000;
    public static final int SHOW_BYFUNCTION=0x00010000;

    public short acceptNode(int nodeHandle,int whatToShow);

    public short acceptNode(int nodeHandle,int whatToShow,int expandedName);
}
