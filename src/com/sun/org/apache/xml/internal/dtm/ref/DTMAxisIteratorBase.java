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
 * $Id: DTMAxisIteratorBase.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
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
 * $Id: DTMAxisIteratorBase.java,v 1.2.4.1 2005/09/15 08:14:59 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public abstract class DTMAxisIteratorBase implements DTMAxisIterator{
    protected int _last=-1;
    protected int _position=0;
    protected int _markedNode;
    protected int _startNode=DTMAxisIterator.END;
    protected boolean _includeSelf=false;
    protected boolean _isRestartable=true;

    public DTMAxisIterator includeSelf(){
        _includeSelf=true;
        return this;
    }    public int getStartNode(){
        return _startNode;
    }

    protected final int returnNode(final int node){
        _position++;
        return node;
    }    public DTMAxisIterator reset(){
        final boolean temp=_isRestartable;
        _isRestartable=true;
        setStartNode(_startNode);
        _isRestartable=temp;
        return this;
    }

    protected final DTMAxisIterator resetPosition(){
        _position=0;
        return this;
    }

    public boolean isDocOrdered(){
        return true;
    }    public int getLast(){
        if(_last==-1)            // Not previously established
        {
            // Note that we're doing both setMark() -- which saves _currentChild
            // -- and explicitly saving our position counter (number of nodes
            // yielded so far).
            //
            // %REVIEW% Should position also be saved by setMark()?
            // (It wasn't in the XSLTC version, but I don't understand why not.)
            final int temp=_position; // Save state
            setMark();
            reset();                  // Count the nodes found by this iterator
            do{
                _last++;
            }
            while(next()!=END);
            gotoMark();               // Restore saved state
            _position=temp;
        }
        return _last;
    }

    public int getAxis(){
        return -1;
    }    public int getPosition(){
        return _position==0?1:_position;
    }

    public boolean isReverse(){
        return false;
    }

    public DTMAxisIterator cloneIterator(){
        try{
            final DTMAxisIteratorBase clone=(DTMAxisIteratorBase)super.clone();
            clone._isRestartable=false;
            // return clone.reset();
            return clone;
        }catch(CloneNotSupportedException e){
            throw new com.sun.org.apache.xml.internal.utils.WrappedRuntimeException(e);
        }
    }









    public void setRestartable(boolean isRestartable){
        _isRestartable=isRestartable;
    }

    public int getNodeByPosition(int position){
        if(position>0){
            final int pos=isReverse()?getLast()-position+1
                    :position;
            int node;
            while((node=next())!=DTMAxisIterator.END){
                if(pos==getPosition()){
                    return node;
                }
            }
        }
        return END;
    }
}
