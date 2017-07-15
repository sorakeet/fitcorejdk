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
 * $Id: DTMNodeList.java,v 1.2.4.1 2005/09/15 08:15:04 suresh_emailid Exp $
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
 * $Id: DTMNodeList.java,v 1.2.4.1 2005/09/15 08:15:04 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import org.w3c.dom.Node;

public class DTMNodeList extends DTMNodeListBase{
    private DTMIterator m_iter;

    //================================================================
    // Methods unique to this class
    private DTMNodeList(){
    }

    public DTMNodeList(DTMIterator dtmIterator){
        if(dtmIterator!=null){
            int pos=dtmIterator.getCurrentPos();
            try{
                m_iter=(DTMIterator)dtmIterator.cloneWithReset();
            }catch(CloneNotSupportedException cnse){
                m_iter=dtmIterator;
            }
            m_iter.setShouldCacheNodes(true);
            m_iter.runTo(-1);
            m_iter.setCurrentPos(pos);
        }
    }

    public DTMIterator getDTMIterator(){
        return m_iter;
    }
    //================================================================
    // org.w3c.dom.NodeList API follows

    public Node item(int index){
        if(m_iter!=null){
            int handle=m_iter.item(index);
            if(handle==DTM.NULL){
                return null;
            }
            return m_iter.getDTM(handle).getNode(handle);
        }else{
            return null;
        }
    }

    public int getLength(){
        return (m_iter!=null)?m_iter.getLength():0;
    }
}
