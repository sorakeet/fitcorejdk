/**
 * Copyright (c) 2007, 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: KeyIndex.java,v 1.6 2006/06/19 19:49:02 spericas Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * $Id: KeyIndex.java,v 1.6 2006/06/19 19:49:02 spericas Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xml.internal.dtm.Axis;
import com.sun.org.apache.xml.internal.dtm.DTM;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class KeyIndex extends DTMAxisIteratorBase{
    final private static IntegerArray EMPTY_NODES=new IntegerArray(0);
    private Map<String,IntegerArray> _index;
    private int _currentDocumentNode=DTM.NULL;
    private Map<Integer,Map> _rootToIndexMap=new HashMap<>();
    private IntegerArray _nodes=null;
    private DOM _dom;
    private DOMEnhancedForDTM _enhancedDOM;
    private int _markedPosition=0;

    public KeyIndex(int dummy){
    }    public void setRestartable(boolean flag){
    }

    public void add(String value,int node,int rootNode){
        if(_currentDocumentNode!=rootNode){
            _currentDocumentNode=rootNode;
            _index=new HashMap<>();
            _rootToIndexMap.put(rootNode,_index);
        }
        IntegerArray nodes=_index.get(value);
        if(nodes==null){
            nodes=new IntegerArray();
            _index.put(value,nodes);
            nodes.add(node);
            // Because nodes are added in document order,
            // duplicates can be eliminated easily at this stage.
        }else if(node!=nodes.at(nodes.cardinality()-1)){
            nodes.add(node);
        }
    }

    public void merge(KeyIndex other){
        if(other==null) return;
        if(other._nodes!=null){
            if(_nodes==null){
                _nodes=(IntegerArray)other._nodes.clone();
            }else{
                _nodes.merge(other._nodes);
            }
        }
    }

    public void lookupId(Object value){
        // Clear _nodes array
        _nodes=null;
        final StringTokenizer values=new StringTokenizer((String)value,
                " \n\t");
        while(values.hasMoreElements()){
            final String token=(String)values.nextElement();
            IntegerArray nodes=_index.get(token);
            if(nodes==null&&_enhancedDOM!=null
                    &&_enhancedDOM.hasDOMSource()){
                nodes=getDOMNodeById(token);
            }
            if(nodes==null) continue;
            if(_nodes==null){
                nodes=(IntegerArray)nodes.clone();
                _nodes=nodes;
            }else{
                _nodes.merge(nodes);
            }
        }
    }

    public IntegerArray getDOMNodeById(String id){
        IntegerArray nodes=null;
        if(_enhancedDOM!=null){
            int ident=_enhancedDOM.getElementById(id);
            if(ident!=DTM.NULL){
                Integer root=new Integer(_enhancedDOM.getDocument());
                Map<String,IntegerArray> index=_rootToIndexMap.get(root);
                if(index==null){
                    index=new HashMap<>();
                    _rootToIndexMap.put(root,index);
                }else{
                    nodes=index.get(id);
                }
                if(nodes==null){
                    nodes=new IntegerArray();
                    index.put(id,nodes);
                }
                nodes.add(_enhancedDOM.getNodeHandle(ident));
            }
        }
        return nodes;
    }

    public void lookupKey(Object value){
        IntegerArray nodes=_index.get(value);
        _nodes=(nodes!=null)?(IntegerArray)nodes.clone():null;
        _position=0;
    }

    public int next(){
        if(_nodes==null) return DTMAxisIterator.END;
        return (_position<_nodes.cardinality())?
                _dom.getNodeHandle(_nodes.at(_position++)):DTMAxisIterator.END;
    }

    public void setMark(){
        _markedPosition=_position;
    }

    public void gotoMark(){
        _position=_markedPosition;
    }

    public DTMAxisIterator setStartNode(int start){
        if(start==DTMAxisIterator.END){
            _nodes=null;
        }else if(_nodes!=null){
            _position=0;
        }
        return (DTMAxisIterator)this;
    }

    public int containsID(int node,Object value){
        final String string=(String)value;
        int rootHandle=_dom.getAxisIterator(Axis.ROOT)
                .setStartNode(node).next();
        // Get the mapping table for the document containing the context node
        Map<String,IntegerArray> index=
                _rootToIndexMap.get(rootHandle);
        // Split argument to id function into XML whitespace separated tokens
        final StringTokenizer values=new StringTokenizer(string," \n\t");
        while(values.hasMoreElements()){
            final String token=(String)values.nextElement();
            IntegerArray nodes=null;
            if(index!=null){
                nodes=index.get(token);
            }
            // If input was from W3C DOM, use DOM's getElementById to do
            // the look-up.
            if(nodes==null&&_enhancedDOM!=null
                    &&_enhancedDOM.hasDOMSource()){
                nodes=getDOMNodeById(token);
            }
            // Did we find the context node in the set of nodes?
            if(nodes!=null&&nodes.indexOf(node)>=0){
                return 1;
            }
        }
        // Didn't find the context node in the set of nodes returned by id
        return 0;
    }

    public int containsKey(int node,Object value){
        int rootHandle=_dom.getAxisIterator(Axis.ROOT)
                .setStartNode(node).next();
        // Get the mapping table for the document containing the context node
        Map<String,IntegerArray> index=
                _rootToIndexMap.get(new Integer(rootHandle));
        // Check whether the context node is present in the set of nodes
        // returned by the key function
        if(index!=null){
            final IntegerArray nodes=index.get(value);
            return (nodes!=null&&nodes.indexOf(node)>=0)?1:0;
        }
        // The particular key name identifies no nodes in this document
        return 0;
    }    public int getPosition(){
        return _position;
    }

    public int getStartNode(){
        return 0;
    }

    public DTMAxisIterator reset(){
        _position=0;
        return this;
    }

    public int getLast(){
        return (_nodes==null)?0:_nodes.cardinality();
    }

    public void setDom(DOM dom,int node){
        _dom=dom;
        // If a MultiDOM, ensure _enhancedDOM is correctly set
        // so that getElementById() works in lookupNodes below
        if(dom instanceof MultiDOM){
            dom=((MultiDOM)dom).getDTM(node);
        }
        if(dom instanceof DOMEnhancedForDTM){
            _enhancedDOM=(DOMEnhancedForDTM)dom;
        }else if(dom instanceof DOMAdapter){
            DOM idom=((DOMAdapter)dom).getDOMImpl();
            if(idom instanceof DOMEnhancedForDTM){
                _enhancedDOM=(DOMEnhancedForDTM)idom;
            }
        }
    }

    public KeyIndexIterator getKeyIndexIterator(Object keyValue,
                                                boolean isKeyCall){
        if(keyValue instanceof DTMAxisIterator){
            return getKeyIndexIterator((DTMAxisIterator)keyValue,isKeyCall);
        }else{
            return getKeyIndexIterator(BasisLibrary.stringF(keyValue,_dom),
                    isKeyCall);
        }
    }    public boolean isReverse(){
        return (false);
    }

    public KeyIndexIterator getKeyIndexIterator(String keyValue,
                                                boolean isKeyCall){
        return new KeyIndexIterator(keyValue,isKeyCall);
    }    public DTMAxisIterator cloneIterator(){
        KeyIndex other=new KeyIndex(0);
        other._index=_index;
        other._rootToIndexMap=_rootToIndexMap;
        other._nodes=_nodes;
        other._position=_position;
        return (DTMAxisIterator)other;
    }

    public KeyIndexIterator getKeyIndexIterator(DTMAxisIterator keyValue,
                                                boolean isKeyCall){
        return new KeyIndexIterator(keyValue,isKeyCall);
    }

    public class KeyIndexIterator extends MultiValuedNodeHeapIterator{
        private IntegerArray _nodes;
        private DTMAxisIterator _keyValueIterator;
        private String _keyValue;
        private boolean _isKeyIterator;

        KeyIndexIterator(String keyValue,boolean isKeyIterator){
            _isKeyIterator=isKeyIterator;
            _keyValue=keyValue;
        }

        KeyIndexIterator(DTMAxisIterator keyValues,boolean isKeyIterator){
            _keyValueIterator=keyValues;
            _isKeyIterator=isKeyIterator;
        }

        public int next(){
            int nodeHandle;
            // If at most one key value or at most one string argument to id
            // resulted in nodes being returned, use the IntegerArray
            // stored at _nodes directly.  This relies on the fact that the
            // IntegerArray never includes duplicate nodes and is always stored
            // in document order.
            if(_nodes!=null){
                if(_position<_nodes.cardinality()){
                    nodeHandle=returnNode(_nodes.at(_position));
                }else{
                    nodeHandle=DTMAxisIterator.END;
                }
            }else{
                nodeHandle=super.next();
            }
            return nodeHandle;
        }

        public DTMAxisIterator setStartNode(int node){
            _startNode=node;
            // If the arugment to the function is a node set, set the
            // context node on it.
            if(_keyValueIterator!=null){
                _keyValueIterator=_keyValueIterator.setStartNode(node);
            }
            init();
            return super.setStartNode(node);
        }

        protected void init(){
            super.init();
            _position=0;
            // All nodes retrieved are in the same document
            int rootHandle=_dom.getAxisIterator(Axis.ROOT)
                    .setStartNode(_startNode).next();
            // Is the argument not a node set?
            if(_keyValueIterator==null){
                // Look up nodes returned for the single string argument
                _nodes=lookupNodes(rootHandle,_keyValue);
                if(_nodes==null){
                    _nodes=EMPTY_NODES;
                }
            }else{
                DTMAxisIterator keyValues=_keyValueIterator.reset();
                int retrievedKeyValueIdx=0;
                boolean foundNodes=false;
                _nodes=null;
                // For each node in the node set argument, get the string value
                // and look up the nodes returned by key or id for that string
                // value.  If at most one string value has nodes associated,
                // the nodes will be stored in _nodes; otherwise, the nodes
                // will be placed in a heap.
                for(int keyValueNode=keyValues.next();
                    keyValueNode!=DTMAxisIterator.END;
                    keyValueNode=keyValues.next()){
                    String keyValue=BasisLibrary.stringF(keyValueNode,_dom);
                    IntegerArray nodes=lookupNodes(rootHandle,keyValue);
                    if(nodes!=null){
                        if(!foundNodes){
                            _nodes=nodes;
                            foundNodes=true;
                        }else{
                            if(_nodes!=null){
                                addHeapNode(new KeyIndexHeapNode(_nodes));
                                _nodes=null;
                            }
                            addHeapNode(new KeyIndexHeapNode(nodes));
                        }
                    }
                }
                if(!foundNodes){
                    _nodes=EMPTY_NODES;
                }
            }
        }

        protected IntegerArray lookupNodes(int root,String keyValue){
            IntegerArray result=null;
            // Get mapping from key values/IDs to DTM nodes for this document
            Map<String,IntegerArray> index=_rootToIndexMap.get(root);
            if(!_isKeyIterator){
                // For id function, tokenize argument as whitespace separated
                // list of values and look up nodes identified by each ID.
                final StringTokenizer values=
                        new StringTokenizer(keyValue," \n\t");
                while(values.hasMoreElements()){
                    final String token=(String)values.nextElement();
                    IntegerArray nodes=null;
                    // Does the ID map to any node in the document?
                    if(index!=null){
                        nodes=index.get(token);
                    }
                    // If input was from W3C DOM, use DOM's getElementById to do
                    // the look-up.
                    if(nodes==null&&_enhancedDOM!=null
                            &&_enhancedDOM.hasDOMSource()){
                        nodes=getDOMNodeById(token);
                    }
                    // If we found any nodes, merge them into the cumulative
                    // result
                    if(nodes!=null){
                        if(result==null){
                            result=(IntegerArray)nodes.clone();
                        }else{
                            result.merge(nodes);
                        }
                    }
                }
            }else if(index!=null){
                // For key function, map key value to nodes
                result=index.get(keyValue);
            }
            return result;
        }

        public DTMAxisIterator reset(){
            if(_nodes==null){
                init();
            }else{
                super.reset();
            }
            return resetPosition();
        }

        protected class KeyIndexHeapNode
                extends HeapNode{
            private IntegerArray _nodes;
            private int _position=0;
            private int _markPosition=-1;

            KeyIndexHeapNode(IntegerArray nodes){
                _nodes=nodes;
            }

            public int step(){
                if(_position<_nodes.cardinality()){
                    _node=_nodes.at(_position);
                    _position++;
                }else{
                    _node=DTMAxisIterator.END;
                }
                return _node;
            }

            public HeapNode cloneHeapNode(){
                KeyIndexHeapNode clone=
                        (KeyIndexHeapNode)super.cloneHeapNode();
                clone._nodes=_nodes;
                clone._position=_position;
                clone._markPosition=_markPosition;
                return clone;
            }

            public void setMark(){
                _markPosition=_position;
            }

            public void gotoMark(){
                _position=_markPosition;
            }

            public boolean isLessThan(HeapNode heapNode){
                return _node<heapNode._node;
            }

            public HeapNode setStartNode(int node){
                return this;
            }

            public HeapNode reset(){
                _position=0;
                return this;
            }
        }

        public int getLast(){
            // If nodes are stored in _nodes, take advantage of the fact that
            // there are no duplicates.  Otherwise, fall back to the base heap
            // implementaiton and hope it does a good job with this.
            return (_nodes!=null)?_nodes.cardinality():super.getLast();
        }

        public int getNodeByPosition(int position){
            int node=DTMAxisIterator.END;
            // If nodes are stored in _nodes, take advantage of the fact that
            // there are no duplicates and they are stored in document order.
            // Otherwise, fall back to the base heap implementation to do a
            // good job with this.
            if(_nodes!=null){
                if(position>0){
                    if(position<=_nodes.cardinality()){
                        _position=position;
                        node=_nodes.at(position-1);
                    }else{
                        _position=_nodes.cardinality();
                    }
                }
            }else{
                node=super.getNodeByPosition(position);
            }
            return node;
        }
    }








}
