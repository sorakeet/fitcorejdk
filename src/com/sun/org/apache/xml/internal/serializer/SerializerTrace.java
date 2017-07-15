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
 * $Id: SerializerTrace.java,v 1.2.4.1 2005/09/15 08:15:24 suresh_emailid Exp $
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
 * $Id: SerializerTrace.java,v 1.2.4.1 2005/09/15 08:15:24 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

import org.xml.sax.Attributes;

public interface SerializerTrace{
    public static final int EVENTTYPE_STARTDOCUMENT=1;
    public static final int EVENTTYPE_ENDDOCUMENT=2;
    public static final int EVENTTYPE_STARTELEMENT=3;
    public static final int EVENTTYPE_ENDELEMENT=4;
    public static final int EVENTTYPE_CHARACTERS=5;
    public static final int EVENTTYPE_IGNORABLEWHITESPACE=6;
    public static final int EVENTTYPE_PI=7;
    public static final int EVENTTYPE_COMMENT=8;
    public static final int EVENTTYPE_ENTITYREF=9;
    public static final int EVENTTYPE_CDATA=10;
    public static final int EVENTTYPE_OUTPUT_PSEUDO_CHARACTERS=11;
    public static final int EVENTTYPE_OUTPUT_CHARACTERS=12;

    public boolean hasTraceListeners();

    public void fireGenerateEvent(int eventType);

    public void fireGenerateEvent(int eventType,String name,Attributes atts);

    public void fireGenerateEvent(int eventType,char ch[],int start,int length);

    public void fireGenerateEvent(int eventType,String name,String data);

    public void fireGenerateEvent(int eventType,String data);
}
