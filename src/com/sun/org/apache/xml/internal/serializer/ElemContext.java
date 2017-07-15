/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: ElemContext.java,v 1.2.4.1 2005/09/15 08:15:15 suresh_emailid Exp $
 */
/**
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * $Id: ElemContext.java,v 1.2.4.1 2005/09/15 08:15:15 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

final class ElemContext{
    // Fields that form the context of the element
    final int m_currentElemDepth;
    final ElemContext m_prev;
    ElemDesc m_elementDesc=null;
    String m_elementLocalName=null;
    String m_elementName=null;
    String m_elementURI=null;
    boolean m_isCdataSection;
    boolean m_isRaw=false;
    boolean m_startTagOpen=false;
    private ElemContext m_next;

    ElemContext(){
        // this assignment means can never pop this context off
        m_prev=this;
        // depth 0 because it doesn't correspond to any element
        m_currentElemDepth=0;
    }

    private ElemContext(final ElemContext previous){
        m_prev=previous;
        m_currentElemDepth=previous.m_currentElemDepth+1;
    }

    final ElemContext pop(){
        /** a very simple pop.  No clean up is done of the deeper
         * stack frame.  All deeper stack frames are still attached
         * but dormant, just waiting to be re-used.
         */
        return this.m_prev;
    }

    final ElemContext push(){
        ElemContext frame=this.m_next;
        if(frame==null){
            /** We have never been at this depth yet, and there is no
             * stack frame to re-use, so we now make a new one.
             */
            frame=new ElemContext(this);
            this.m_next=frame;
        }
        /**
         * We shouldn't need to set this true because we should just
         * be pushing a dummy stack frame that will be instantly popped.
         * Yet we need to be ready in case this element does have
         * unexpected children.
         */
        frame.m_startTagOpen=true;
        return frame;
    }

    final ElemContext push(
            final String uri,
            final String localName,
            final String qName){
        ElemContext frame=this.m_next;
        if(frame==null){
            /** We have never been at this depth yet, and there is no
             * stack frame to re-use, so we now make a new one.
             */
            frame=new ElemContext(this);
            this.m_next=frame;
        }
        // Initialize, or reset values in the new or re-used stack frame.
        frame.m_elementName=qName;
        frame.m_elementLocalName=localName;
        frame.m_elementURI=uri;
        frame.m_isCdataSection=false;
        frame.m_startTagOpen=true;
        // is_Raw is already set in the HTML startElement() method
        // frame.m_isRaw = false;
        return frame;
    }
}
