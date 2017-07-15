/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2006 The Apache Software Foundation.
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
 * $Id: UnionIterator.java 337874 2004-02-16 23:06:53Z minchau $
 */
/**
 * Copyright 2001-2006 The Apache Software Foundation.
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
 * $Id: UnionIterator.java 337874 2004-02-16 23:06:53Z minchau $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public abstract class MultiValuedNodeHeapIterator extends DTMAxisIteratorBase{
    private static final int InitSize=8;
    private int _heapSize=0;
    private int _size=InitSize;
    private HeapNode[] _heap=new HeapNode[InitSize];
    private int _free=0;
    // Last node returned by this MultiValuedNodeHeapIterator to the caller of
    // next; used to prune duplicates
    private int _returnedLast;
    // cached returned last for use in gotoMark
    private int _cachedReturnedLast=END;
    // cached heap size for use in gotoMark
    private int _cachedHeapSize;

    protected void addHeapNode(HeapNode node){
        if(_free==_size){
            HeapNode[] newArray=new HeapNode[_size*=2];
            System.arraycopy(_heap,0,newArray,0,_free);
            _heap=newArray;
        }
        _heapSize++;
        _heap[_free++]=node;
    }

    public int next(){
        while(_heapSize>0){
            final int smallest=_heap[0]._node;
            if(smallest==END){ // iterator _heap[0] is done
                if(_heapSize>1){
                    // Swap first and last (iterator must be restartable)
                    final HeapNode temp=_heap[0];
                    _heap[0]=_heap[--_heapSize];
                    _heap[_heapSize]=temp;
                }else{
                    return END;
                }
            }else if(smallest==_returnedLast){       // duplicate
                _heap[0].step(); // value consumed
            }else{
                _heap[0].step(); // value consumed
                heapify(0);
                return returnNode(_returnedLast=smallest);
            }
            // fallthrough if not returned above
            heapify(0);
        }
        return END;
    }

    private void heapify(int i){
        for(int r, l, smallest;;){
            r=(i+1)<<1;
            l=r-1;
            smallest=l<_heapSize
                    &&_heap[l].isLessThan(_heap[i])?l:i;
            if(r<_heapSize&&_heap[r].isLessThan(_heap[smallest])){
                smallest=r;
            }
            if(smallest!=i){
                final HeapNode temp=_heap[smallest];
                _heap[smallest]=_heap[i];
                _heap[i]=temp;
                i=smallest;
            }else{
                break;
            }
        }
    }

    public void setMark(){
        for(int i=0;i<_free;i++){
            _heap[i].setMark();
        }
        _cachedReturnedLast=_returnedLast;
        _cachedHeapSize=_heapSize;
    }

    public void gotoMark(){
        for(int i=0;i<_free;i++){
            _heap[i].gotoMark();
        }
        // rebuild heap after call last() function. fix for bug 20913
        for(int i=(_heapSize=_cachedHeapSize)/2;i>=0;i--){
            heapify(i);
        }
        _returnedLast=_cachedReturnedLast;
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isRestartable){
            _startNode=node;
            for(int i=0;i<_free;i++){
                if(!_heap[i]._isStartSet){
                    _heap[i].setStartNode(node);
                    _heap[i].step();     // to get the first node
                    _heap[i]._isStartSet=true;
                }
            }
            // build heap
            for(int i=(_heapSize=_free)/2;i>=0;i--){
                heapify(i);
            }
            _returnedLast=END;
            return resetPosition();
        }
        return this;
    }

    protected void init(){
        for(int i=0;i<_free;i++){
            _heap[i]=null;
        }
        _heapSize=0;
        _free=0;
    }

    public DTMAxisIterator reset(){
        for(int i=0;i<_free;i++){
            _heap[i].reset();
            _heap[i].step();
        }
        // build heap
        for(int i=(_heapSize=_free)/2;i>=0;i--){
            heapify(i);
        }
        _returnedLast=END;
        return resetPosition();
    }

    public DTMAxisIterator cloneIterator(){
        _isRestartable=false;
        final HeapNode[] heapCopy=new HeapNode[_heap.length];
        try{
            MultiValuedNodeHeapIterator clone=
                    (MultiValuedNodeHeapIterator)super.clone();
            for(int i=0;i<_free;i++){
                heapCopy[i]=_heap[i].cloneHeapNode();
            }
            clone.setRestartable(false);
            clone._heap=heapCopy;
            return clone.reset();
        }catch(CloneNotSupportedException e){
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                    e.toString());
            return null;
        }
    }

    public abstract class HeapNode implements Cloneable{
        protected int _node, _markedNode;
        protected boolean _isStartSet=false;

        public abstract int step();

        public HeapNode cloneHeapNode(){
            HeapNode clone;
            try{
                clone=(HeapNode)super.clone();
            }catch(CloneNotSupportedException e){
                BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                        e.toString());
                return null;
            }
            clone._node=_node;
            clone._markedNode=_node;
            return clone;
        }

        public void setMark(){
            _markedNode=_node;
        }

        public void gotoMark(){
            _node=_markedNode;
        }

        public abstract boolean isLessThan(HeapNode heapNode);

        public abstract HeapNode setStartNode(int node);

        public abstract HeapNode reset();
    } // end of HeapNode
}
