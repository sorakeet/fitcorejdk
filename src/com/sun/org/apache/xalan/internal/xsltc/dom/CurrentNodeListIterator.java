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
 * $Id: CurrentNodeListIterator.java,v 1.2.4.1 2005/09/06 06:04:45 pvedula Exp $
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
 * $Id: CurrentNodeListIterator.java,v 1.2.4.1 2005/09/06 06:04:45 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class CurrentNodeListIterator extends DTMAxisIteratorBase{
    private final CurrentNodeListFilter _filter;
    private final int _currentNode;
    private boolean _docOrder;
    private DTMAxisIterator _source;
    private IntegerArray _nodes=new IntegerArray();
    private int _currentIndex;
    private AbstractTranslet _translet;

    public CurrentNodeListIterator(DTMAxisIterator source,
                                   CurrentNodeListFilter filter,
                                   int currentNode,
                                   AbstractTranslet translet){
        this(source,!source.isReverse(),filter,currentNode,translet);
    }

    public CurrentNodeListIterator(DTMAxisIterator source,boolean docOrder,
                                   CurrentNodeListFilter filter,
                                   int currentNode,
                                   AbstractTranslet translet){
        _source=source;
        _filter=filter;
        _translet=translet;
        _docOrder=docOrder;
        _currentNode=currentNode;
    }

    public DTMAxisIterator forceNaturalOrder(){
        _docOrder=true;
        return this;
    }

    public DTMAxisIterator reset(){
        _currentIndex=0;
        return resetPosition();
    }

    public int getLast(){
        if(_last==-1){
            _last=computePositionOfLast();
        }
        return _last;
    }

    public boolean isReverse(){
        return !_docOrder;
    }

    public DTMAxisIterator cloneIterator(){
        try{
            final CurrentNodeListIterator clone=
                    (CurrentNodeListIterator)super.clone();
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

    private int computePositionOfLast(){
        final int last=_nodes.cardinality();
        final int currNode=_currentNode;
        final AbstractTranslet translet=_translet;
        int lastPosition=_position;
        for(int index=_currentIndex;index<last;){
            final int position=_docOrder?index+1:last-index;
            int nodeIndex=_nodes.at(index++);         // note increment
            if(_filter.test(nodeIndex,position,last,currNode,translet,
                    this)){
                lastPosition++;
            }
        }
        return lastPosition;
    }

    public int next(){
        final int last=_nodes.cardinality();
        final int currentNode=_currentNode;
        final AbstractTranslet translet=_translet;
        for(int index=_currentIndex;index<last;){
            final int position=_docOrder?index+1:last-index;
            final int node=_nodes.at(index++);        // note increment
            if(_filter.test(node,position,last,currentNode,translet,
                    this)){
                _currentIndex=index;
                return returnNode(node);
            }
        }
        return END;
    }

    public void setMark(){
        _markedNode=_currentIndex;
    }

    public void gotoMark(){
        _currentIndex=_markedNode;
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isRestartable){
            _source.setStartNode(_startNode=node);
            _nodes.clear();
            while((node=_source.next())!=END){
                _nodes.add(node);
            }
            _currentIndex=0;
            resetPosition();
        }
        return this;
    }
}
