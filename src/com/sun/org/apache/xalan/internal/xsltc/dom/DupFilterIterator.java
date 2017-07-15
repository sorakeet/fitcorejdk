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
 * $Id: DupFilterIterator.java,v 1.2.4.1 2005/09/06 06:16:11 pvedula Exp $
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
 * $Id: DupFilterIterator.java,v 1.2.4.1 2005/09/06 06:16:11 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;
import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase;

public final class DupFilterIterator extends DTMAxisIteratorBase{
    private DTMAxisIterator _source;
    private IntegerArray _nodes=new IntegerArray();
    private int _current=0;
    private int _nodesSize=0;
    private int _lastNext=END;
    private int _markedLastNext=END;

    public DupFilterIterator(DTMAxisIterator source){
        _source=source;
// System.out.println("DFI source = " + source + " this = " + this);
        // Cache contents of id() or key() index right away. Necessary for
        // union expressions containing multiple calls to the same index, and
        // correct as well since start-node is irrelevant for id()/key() exrp.
        if(source instanceof KeyIndex){
            setStartNode(DTMDefaultBase.ROOTNODE);
        }
    }

    public int next(){
        while(_current<_nodesSize){
            final int next=_nodes.at(_current++);
            if(next!=_lastNext){
                return returnNode(_lastNext=next);
            }
        }
        return END;
    }

    public void setMark(){
        _markedNode=_current;
        _markedLastNext=_lastNext;    // Bugzilla 25924
    }

    public void gotoMark(){
        _current=_markedNode;
        _lastNext=_markedLastNext;    // Bugzilla 25924
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isRestartable){
            // KeyIndex iterators are always relative to the root node, so there
            // is never any point in re-reading the iterator (and we SHOULD NOT).
            boolean sourceIsKeyIndex=_source instanceof KeyIndex;
            if(sourceIsKeyIndex
                    &&_startNode==DTMDefaultBase.ROOTNODE){
                return this;
            }
            if(node!=_startNode){
                _source.setStartNode(_startNode=node);
                _nodes.clear();
                while((node=_source.next())!=END){
                    _nodes.add(node);
                }
                // Nodes produced by KeyIndex are known to be in document order.
                // Take advantage of it.
                if(!sourceIsKeyIndex){
                    _nodes.sort();
                }
                _nodesSize=_nodes.cardinality();
                _current=0;
                _lastNext=END;
                resetPosition();
            }
        }
        return this;
    }

    public DTMAxisIterator reset(){
        _current=0;
        _lastNext=END;
        return resetPosition();
    }

    public DTMAxisIterator cloneIterator(){
        try{
            final DupFilterIterator clone=
                    (DupFilterIterator)super.clone();
            clone._nodes=(IntegerArray)_nodes.clone();
            clone._source=_source.cloneIterator();
            clone._isRestartable=false;
            return clone.reset();
        }catch(CloneNotSupportedException e){
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                    e.toString());
            return null;
        }
    }

    public void setRestartable(boolean isRestartable){
        _isRestartable=isRestartable;
        _source.setRestartable(isRestartable);
    }
}
