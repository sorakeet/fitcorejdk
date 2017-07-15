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
 * $Id: StepIterator.java,v 1.2.4.1 2005/09/06 10:26:47 pvedula Exp $
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
 * $Id: StepIterator.java,v 1.2.4.1 2005/09/06 10:26:47 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public class StepIterator extends DTMAxisIteratorBase{
    protected DTMAxisIterator _source;
    protected DTMAxisIterator _iterator;
    private int _pos=-1;

    public StepIterator(DTMAxisIterator source,DTMAxisIterator iterator){
        _source=source;
        _iterator=iterator;
// System.out.println("SI source = " + source + " this = " + this);
// System.out.println("SI iterator = " + iterator + " this = " + this);
    }

    public DTMAxisIterator reset(){
        _source.reset();
        // Special case for //* path - see ParentLocationPath
        _iterator.setStartNode(_includeSelf?_startNode:_source.next());
        return resetPosition();
    }

    public DTMAxisIterator cloneIterator(){
        _isRestartable=false;
        try{
            final StepIterator clone=(StepIterator)super.clone();
            clone._source=_source.cloneIterator();
            clone._iterator=_iterator.cloneIterator();
            clone._iterator.setRestartable(true);       // must be restartable
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
        _iterator.setRestartable(true);         // must be restartable
    }

    public int next(){
        for(int node;;){
            // Try to get another node from the right-hand iterator
            if((node=_iterator.next())!=END){
                return returnNode(node);
            }
            // If not, get the next starting point from left-hand iterator...
            else if((node=_source.next())==END){
                return END;
            }
            // ...and pass it on to the right-hand iterator
            else{
                _iterator.setStartNode(node);
            }
        }
    }

    public void setMark(){
        _source.setMark();
        _iterator.setMark();
        //_pos = _position;
    }

    public void gotoMark(){
        _source.gotoMark();
        _iterator.gotoMark();
        //_position = _pos;
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isRestartable){
            // Set start node for left-hand iterator...
            _source.setStartNode(_startNode=node);
            // ... and get start node for right-hand iterator from left-hand,
            // with special case for //* path - see ParentLocationPath
            _iterator.setStartNode(_includeSelf?_startNode:_source.next());
            return resetPosition();
        }
        return this;
    }
}
