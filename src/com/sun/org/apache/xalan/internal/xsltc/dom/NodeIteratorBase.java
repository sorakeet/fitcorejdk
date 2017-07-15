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
 * $Id: NodeIteratorBase.java,v 1.2.4.1 2005/09/06 09:37:02 pvedula Exp $
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
 * $Id: NodeIteratorBase.java,v 1.2.4.1 2005/09/06 09:37:02 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.NodeIterator;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;

public abstract class NodeIteratorBase implements NodeIterator{
    protected int _last=-1;
    protected int _position=0;
    protected int _markedNode;
    protected int _startNode=NodeIterator.END;
    protected boolean _includeSelf=false;
    protected boolean _isRestartable=true;

    public NodeIterator includeSelf(){
        _includeSelf=true;
        return this;
    }    public void setRestartable(boolean isRestartable){
        _isRestartable=isRestartable;
    }

    protected final int returnNode(final int node){
        _position++;
        return node;
    }    abstract public NodeIterator setStartNode(int node);

    protected final NodeIterator resetPosition(){
        _position=0;
        return this;
    }    public NodeIterator reset(){
        final boolean temp=_isRestartable;
        _isRestartable=true;
        // Must adjust _startNode if self is included
        setStartNode(_includeSelf?_startNode+1:_startNode);
        _isRestartable=temp;
        return this;
    }



    public int getLast(){
        if(_last==-1){
            final int temp=_position;
            setMark();
            reset();
            do{
                _last++;
            }while(next()!=END);
            gotoMark();
            _position=temp;
        }
        return _last;
    }

    public int getPosition(){
        return _position==0?1:_position;
    }

    public boolean isReverse(){
        return false;
    }

    public NodeIterator cloneIterator(){
        try{
            final NodeIteratorBase clone=(NodeIteratorBase)super.clone();
            clone._isRestartable=false;
            return clone.reset();
        }catch(CloneNotSupportedException e){
            BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR,
                    e.toString());
            return null;
        }
    }




}
