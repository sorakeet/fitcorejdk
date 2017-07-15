/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ClonedNodeListIterator.java,v 1.2.4.1 2005/09/06 06:02:12 pvedula Exp $
 */
/**
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * $Id: ClonedNodeListIterator.java,v 1.2.4.1 2005/09/06 06:02:12 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class ClonedNodeListIterator extends DTMAxisIteratorBase{
    private CachedNodeListIterator _source;
    private int _index=0;

    public ClonedNodeListIterator(CachedNodeListIterator source){
        _source=source;
    }

    public int next(){
        return _source.getNode(_index++);
    }

    public void setMark(){
        _source.setMark();
    }

    public void gotoMark(){
        _source.gotoMark();
    }

    public DTMAxisIterator setStartNode(int node){
        return this;
    }

    public DTMAxisIterator reset(){
        _index=0;
        return this;
    }

    public int getPosition(){
        return _index==0?1:_index;
    }

    public DTMAxisIterator cloneIterator(){
        return _source.cloneIterator();
    }

    public void setRestartable(boolean isRestartable){
        //_isRestartable = isRestartable;
        //_source.setRestartable(isRestartable);
    }

    public int getNodeByPosition(int pos){
        return _source.getNode(pos);
    }
}
