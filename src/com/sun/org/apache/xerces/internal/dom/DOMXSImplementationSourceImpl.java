/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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
 */
/**
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xerces.internal.dom;

import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;

import java.util.Vector;

public class DOMXSImplementationSourceImpl
        extends DOMImplementationSourceImpl{
    public DOMImplementation getDOMImplementation(String features){
        DOMImplementation impl=super.getDOMImplementation(features);
        if(impl!=null){
            return impl;
        }
        // if not try the PSVIDOMImplementation
        impl=PSVIDOMImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            return impl;
        }
        // if not try the XSImplementation
        impl=XSImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            return impl;
        }
        return null;
    }

    public DOMImplementationList getDOMImplementationList(String features){
        final Vector implementations=new Vector();
        // first check whether the CoreDOMImplementation would do
        DOMImplementationList list=super.getDOMImplementationList(features);
        //Add core DOMImplementations
        for(int i=0;i<list.getLength();i++){
            implementations.addElement(list.item(i));
        }
        DOMImplementation impl=PSVIDOMImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            implementations.addElement(impl);
        }
        impl=XSImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            implementations.addElement(impl);
        }
        return new DOMImplementationListImpl(implementations);
    }
}
