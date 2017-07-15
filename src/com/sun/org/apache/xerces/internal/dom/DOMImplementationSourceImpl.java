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

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.DOMImplementationSource;

import java.util.StringTokenizer;
import java.util.Vector;

public class DOMImplementationSourceImpl
        implements DOMImplementationSource{
    public DOMImplementation getDOMImplementation(String features){
        // first check whether the CoreDOMImplementation would do
        DOMImplementation impl=
                CoreDOMImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            return impl;
        }
        // if not try the DOMImplementation
        impl=DOMImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            return impl;
        }
        return null;
    }

    public DOMImplementationList getDOMImplementationList(String features){
        // first check whether the CoreDOMImplementation would do
        DOMImplementation impl=CoreDOMImplementationImpl.getDOMImplementation();
        final Vector implementations=new Vector();
        if(testImpl(impl,features)){
            implementations.addElement(impl);
        }
        impl=DOMImplementationImpl.getDOMImplementation();
        if(testImpl(impl,features)){
            implementations.addElement(impl);
        }
        return new DOMImplementationListImpl(implementations);
    }

    boolean testImpl(DOMImplementation impl,String features){
        StringTokenizer st=new StringTokenizer(features);
        String feature=null;
        String version=null;
        if(st.hasMoreTokens()){
            feature=st.nextToken();
        }
        while(feature!=null){
            boolean isVersion=false;
            if(st.hasMoreTokens()){
                char c;
                version=st.nextToken();
                c=version.charAt(0);
                switch(c){
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        isVersion=true;
                }
            }else{
                version=null;
            }
            if(isVersion){
                if(!impl.hasFeature(feature,version)){
                    return false;
                }
                if(st.hasMoreTokens()){
                    feature=st.nextToken();
                }else{
                    feature=null;
                }
            }else{
                if(!impl.hasFeature(feature,null)){
                    return false;
                }
                feature=version;
            }
        }
        return true;
    }
}
