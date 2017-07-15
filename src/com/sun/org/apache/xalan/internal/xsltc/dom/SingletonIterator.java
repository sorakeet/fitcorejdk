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
 * $Id: SingletonIterator.java,v 1.2.4.1 2005/09/06 10:15:18 pvedula Exp $
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
 * $Id: SingletonIterator.java,v 1.2.4.1 2005/09/06 10:15:18 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.dom;

import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMAxisIteratorBase;

public class SingletonIterator extends DTMAxisIteratorBase{
    private final boolean _isConstant;
    private int _node;

    public SingletonIterator(){
        this(Integer.MIN_VALUE,false);
    }

    public SingletonIterator(int node,boolean constant){
        _node=_startNode=node;
        _isConstant=constant;
    }

    public SingletonIterator(int node){
        this(node,false);
    }

    public DTMAxisIterator reset(){
        if(_isConstant){
            _node=_startNode;
            return resetPosition();
        }else{
            final boolean temp=_isRestartable;
            _isRestartable=true;
            setStartNode(_startNode);
            _isRestartable=temp;
        }
        return this;
    }

    public int next(){
        final int result=_node;
        _node=DTMAxisIterator.END;
        return returnNode(result);
    }

    public void setMark(){
        _markedNode=_node;
    }

    public void gotoMark(){
        _node=_markedNode;
    }

    public DTMAxisIterator setStartNode(int node){
        if(_isConstant){
            _node=_startNode;
            return resetPosition();
        }else if(_isRestartable){
            if(_node<=0)
                _node=_startNode=node;
            return resetPosition();
        }
        return this;
    }
}
