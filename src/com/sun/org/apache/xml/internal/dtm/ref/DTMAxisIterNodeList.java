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
 * $Id: DTMAxisIterNodeList.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
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
 * $Id: DTMAxisIterNodeList.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.utils.IntVector;
import org.w3c.dom.Node;

public class DTMAxisIterNodeList extends DTMNodeListBase{
    private DTM m_dtm;
    private DTMAxisIterator m_iter;
    private IntVector m_cachedNodes;
    private int m_last=-1;

    //================================================================
    // Methods unique to this class
    private DTMAxisIterNodeList(){
    }

    public DTMAxisIterNodeList(DTM dtm,DTMAxisIterator dtmAxisIterator){
        if(dtmAxisIterator==null){
            m_last=0;
        }else{
            m_cachedNodes=new IntVector();
            m_dtm=dtm;
        }
        m_iter=dtmAxisIterator;
    }

    public DTMAxisIterator getDTMAxisIterator(){
        return m_iter;
    }
    //================================================================
    // org.w3c.dom.NodeList API follows

    public Node item(int index){
        if(m_iter!=null){
            int node=0;
            int count=m_cachedNodes.size();
            if(count>index){
                node=m_cachedNodes.elementAt(index);
                return m_dtm.getNode(node);
            }else if(m_last==-1){
                while(count<=index
                        &&((node=m_iter.next())!=DTMAxisIterator.END)){
                    m_cachedNodes.addElement(node);
                    count++;
                }
                if(node==DTMAxisIterator.END){
                    m_last=count;
                }else{
                    return m_dtm.getNode(node);
                }
            }
        }
        return null;
    }

    public int getLength(){
        if(m_last==-1){
            int node;
            while((node=m_iter.next())!=DTMAxisIterator.END){
                m_cachedNodes.addElement(node);
            }
            m_last=m_cachedNodes.size();
        }
        return m_last;
    }
}
