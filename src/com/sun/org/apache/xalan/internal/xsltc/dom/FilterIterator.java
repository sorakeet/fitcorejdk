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
 * $Id: FilterIterator.java,v 1.2.4.1 2005/09/06 06:21:10 pvedula Exp $
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
 * $Id: FilterIterator.java,v 1.2.4.1 2005/09/06 06:21:10 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.DTMFilter;
import com.sun.org.apache.xml.internal.dtm.DTMIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public final class FilterIterator extends DTMAxisIteratorBase{
    private final DTMFilter _filter;
    private final boolean _isReverse;
    private DTMAxisIterator _source;

    public FilterIterator(DTMAxisIterator source,DTMFilter filter){
        _source=source;
// System.out.println("FI souce = " + source + " this = " + this);
        _filter=filter;
        _isReverse=source.isReverse();
    }

    public DTMAxisIterator reset(){
        _source.reset();
        return resetPosition();
    }

    public boolean isReverse(){
        return _isReverse;
    }

    public DTMAxisIterator cloneIterator(){
        try{
            final FilterIterator clone=(FilterIterator)super.clone();
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

    public int next(){
        int node;
        while((node=_source.next())!=END){
            if(_filter.acceptNode(node,DTMFilter.SHOW_ALL)==DTMIterator.FILTER_ACCEPT){
                return returnNode(node);
            }
        }
        return END;
    }

    public void setMark(){
        _source.setMark();
    }

    public void gotoMark(){
        _source.gotoMark();
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isRestartable){
            _source.setStartNode(_startNode=node);
            return resetPosition();
        }
        return this;
    }
}
