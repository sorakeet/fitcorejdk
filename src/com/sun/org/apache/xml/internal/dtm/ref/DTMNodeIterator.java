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
 * $Id: DTMNodeIterator.java,v 1.2.4.1 2005/09/15 08:15:03 suresh_emailid Exp $
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
 * $Id: DTMNodeIterator.java,v 1.2.4.1 2005/09/15 08:15:03 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMDOMException;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

public class DTMNodeIterator implements org.w3c.dom.traversal.NodeIterator{
    private DTMIterator dtm_iter;
    private boolean valid=true;
    //================================================================
    // Methods unique to this class

    public DTMNodeIterator(DTMIterator dtmIterator){
        try{
            dtm_iter=(DTMIterator)dtmIterator.clone();
        }catch(CloneNotSupportedException cnse){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(cnse);
        }
    }

    public DTMIterator getDTMIterator(){
        return dtm_iter;
    }
    //================================================================
    // org.w3c.dom.traversal.NodeFilter API follows

    public Node getRoot(){
        int handle=dtm_iter.getRoot();
        return dtm_iter.getDTM(handle).getNode(handle);
    }

    public int getWhatToShow(){
        return dtm_iter.getWhatToShow();
    }

    public NodeFilter getFilter(){
        throw new DTMDOMException(DOMException.NOT_SUPPORTED_ERR);
    }

    public boolean getExpandEntityReferences(){
        return false;
    }

    public Node nextNode() throws DOMException{
        if(!valid)
            throw new DTMDOMException(DOMException.INVALID_STATE_ERR);
        int handle=dtm_iter.nextNode();
        if(handle==DTM.NULL)
            return null;
        return dtm_iter.getDTM(handle).getNode(handle);
    }

    public Node previousNode(){
        if(!valid)
            throw new DTMDOMException(DOMException.INVALID_STATE_ERR);
        int handle=dtm_iter.previousNode();
        if(handle==DTM.NULL)
            return null;
        return dtm_iter.getDTM(handle).getNode(handle);
    }

    public void detach(){
        // Theoretically, we could release dtm_iter at this point. But
        // some of the operations may still want to consult it even though
        // navigation is now invalid.
        valid=false;
    }
}
