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
 * $Id: DTMChildIterNodeList.java,v 1.2.4.1 2005/09/15 08:15:00 suresh_emailid Exp $
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
 * $Id: DTMChildIterNodeList.java,v 1.2.4.1 2005/09/15 08:15:00 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;
import org.w3c.dom.Node;

public class DTMChildIterNodeList extends DTMNodeListBase{
    private int m_firstChild;
    private DTM m_parentDTM;

    //================================================================
    // Methods unique to this class
    private DTMChildIterNodeList(){
    }

    public DTMChildIterNodeList(DTM parentDTM,int parentHandle){
        m_parentDTM=parentDTM;
        m_firstChild=parentDTM.getFirstChild(parentHandle);
    }
    //================================================================
    // org.w3c.dom.NodeList API follows

    public Node item(int index){
        int handle=m_firstChild;
        while(--index>=0&&handle!=DTM.NULL){
            handle=m_parentDTM.getNextSibling(handle);
        }
        if(handle==DTM.NULL){
            return null;
        }
        return m_parentDTM.getNode(handle);
    }

    public int getLength(){
        int count=0;
        for(int handle=m_firstChild;
            handle!=DTM.NULL;
            handle=m_parentDTM.getNextSibling(handle)){
            ++count;
        }
        return count;
    }
}
