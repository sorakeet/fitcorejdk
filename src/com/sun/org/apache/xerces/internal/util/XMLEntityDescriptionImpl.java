/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * Copyright 2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.impl.XMLEntityDescription;

public class XMLEntityDescriptionImpl
        extends XMLResourceIdentifierImpl
        implements XMLEntityDescription{
    //
    // Constructors
    //

    //
    // Data
    //
    protected String fEntityName;

    public XMLEntityDescriptionImpl(){
    } // <init>()

    public XMLEntityDescriptionImpl(String entityName,String publicId,String literalSystemId,
                                    String baseSystemId,String expandedSystemId){
        setDescription(entityName,publicId,literalSystemId,baseSystemId,expandedSystemId);
    } // <init>(String,String,String,String,String)

    public void setDescription(String entityName,String publicId,String literalSystemId,
                               String baseSystemId,String expandedSystemId){
        setDescription(entityName,publicId,literalSystemId,baseSystemId,expandedSystemId,null);
    } // setDescription(String,String,String,String,String)
    //
    // Public methods
    //

    public void setDescription(String entityName,String publicId,String literalSystemId,
                               String baseSystemId,String expandedSystemId,String namespace){
        fEntityName=entityName;
        setValues(publicId,literalSystemId,baseSystemId,expandedSystemId,namespace);
    } // setDescription(String,String,String,String,String,String)    public void setEntityName(String name){
        fEntityName=name;
    } // setEntityName(String)

    public XMLEntityDescriptionImpl(String entityName,String publicId,String literalSystemId,
                                    String baseSystemId,String expandedSystemId,String namespace){
        setDescription(entityName,publicId,literalSystemId,baseSystemId,expandedSystemId,namespace);
    } // <init>(String,String,String,String,String,String)    public String getEntityName(){
        return fEntityName;
    } // getEntityName():String

    public void clear(){
        super.clear();
        fEntityName=null;
    } // clear()

    public int hashCode(){
        int code=super.hashCode();
        if(fEntityName!=null){
            code+=fEntityName.hashCode();
        }
        return code;
    } // hashCode():int

    public String toString(){
        StringBuffer str=new StringBuffer();
        if(fEntityName!=null){
            str.append(fEntityName);
        }
        str.append(':');
        if(fPublicId!=null){
            str.append(fPublicId);
        }
        str.append(':');
        if(fLiteralSystemId!=null){
            str.append(fLiteralSystemId);
        }
        str.append(':');
        if(fBaseSystemId!=null){
            str.append(fBaseSystemId);
        }
        str.append(':');
        if(fExpandedSystemId!=null){
            str.append(fExpandedSystemId);
        }
        str.append(':');
        if(fNamespace!=null){
            str.append(fNamespace);
        }
        return str.toString();
    } // toString():String
    //
    // Object methods
    //




} // XMLEntityDescriptionImpl
