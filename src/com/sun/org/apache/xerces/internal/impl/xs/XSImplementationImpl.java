/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Copyright 2003,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import com.sun.org.apache.xerces.internal.xs.StringList;
import com.sun.org.apache.xerces.internal.xs.XSException;
import com.sun.org.apache.xerces.internal.xs.XSImplementation;
import com.sun.org.apache.xerces.internal.xs.XSLoader;
import org.w3c.dom.DOMImplementation;

public class XSImplementationImpl extends CoreDOMImplementationImpl
        implements XSImplementation{
    //
    // Data
    //
    // static
    static XSImplementationImpl singleton=new XSImplementationImpl();
    //
    // Public methods
    //

    public static DOMImplementation getDOMImplementation(){
        return singleton;
    }
    //
    // DOMImplementation methods
    //

    public boolean hasFeature(String feature,String version){
        return (feature.equalsIgnoreCase("XS-Loader")&&(version==null||version.equals("1.0"))||
                super.hasFeature(feature,version));
    } // hasFeature(String,String):boolean

    public StringList getRecognizedVersions(){
        StringListImpl list=new StringListImpl(new String[]{"1.0"},1);
        return list;
    }

    public XSLoader createXSLoader(StringList versions) throws XSException{
        XSLoader loader=new XSLoaderImpl();
        if(versions==null){
            return loader;
        }
        for(int i=0;i<versions.getLength();i++){
            if(!versions.item(i).equals("1.0")){
                String msg=
                        DOMMessageFormatter.formatMessage(
                                DOMMessageFormatter.DOM_DOMAIN,
                                "FEATURE_NOT_SUPPORTED",
                                new Object[]{versions.item(i)});
                throw new XSException(XSException.NOT_SUPPORTED_ERR,msg);
            }
        }
        return loader;
    }
} // class XSImplementationImpl
