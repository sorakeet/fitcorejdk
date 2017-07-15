/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;

public class XMLResourceIdentifierImpl
        implements XMLResourceIdentifier{
    //
    // Data
    //
    protected String fPublicId;
    protected String fLiteralSystemId;
    protected String fBaseSystemId;
    protected String fExpandedSystemId;
    protected String fNamespace;
    //
    // Constructors
    //

    public XMLResourceIdentifierImpl(){
    } // <init>()

    public XMLResourceIdentifierImpl(String publicId,
                                     String literalSystemId,String baseSystemId,
                                     String expandedSystemId){
        setValues(publicId,literalSystemId,baseSystemId,
                expandedSystemId,null);
    } // <init>(String,String,String,String)

    public void setValues(String publicId,String literalSystemId,
                          String baseSystemId,String expandedSystemId,
                          String namespace){
        fPublicId=publicId;
        fLiteralSystemId=literalSystemId;
        fBaseSystemId=baseSystemId;
        fExpandedSystemId=expandedSystemId;
        fNamespace=namespace;
    } // setValues(String,String,String,String,String)
    //
    // Public methods
    //

    public XMLResourceIdentifierImpl(String publicId,String literalSystemId,
                                     String baseSystemId,String expandedSystemId,
                                     String namespace){
        setValues(publicId,literalSystemId,baseSystemId,
                expandedSystemId,namespace);
    } // <init>(String,String,String,String,String)

    public void setValues(String publicId,String literalSystemId,
                          String baseSystemId,String expandedSystemId){
        setValues(publicId,literalSystemId,baseSystemId,
                expandedSystemId,null);
    } // setValues(String,String,String,String)

    public void clear(){
        fPublicId=null;
        fLiteralSystemId=null;
        fBaseSystemId=null;
        fExpandedSystemId=null;
        fNamespace=null;
    } // clear()

    public int hashCode(){
        int code=0;
        if(fPublicId!=null){
            code+=fPublicId.hashCode();
        }
        if(fLiteralSystemId!=null){
            code+=fLiteralSystemId.hashCode();
        }
        if(fBaseSystemId!=null){
            code+=fBaseSystemId.hashCode();
        }
        if(fExpandedSystemId!=null){
            code+=fExpandedSystemId.hashCode();
        }
        if(fNamespace!=null){
            code+=fNamespace.hashCode();
        }
        return code;
    } // hashCode():int    public void setPublicId(String publicId){
        fPublicId=publicId;
    } // setPublicId(String)

    public String toString(){
        StringBuffer str=new StringBuffer();
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
    } // toString():String    public void setLiteralSystemId(String literalSystemId){
        fLiteralSystemId=literalSystemId;
    } // setLiteralSystemId(String)

    public void setBaseSystemId(String baseSystemId){
        fBaseSystemId=baseSystemId;
    } // setBaseSystemId(String)

    public void setExpandedSystemId(String expandedSystemId){
        fExpandedSystemId=expandedSystemId;
    } // setExpandedSystemId(String)

    public void setNamespace(String namespace){
        fNamespace=namespace;
    } // setNamespace(String)
    //
    // XMLResourceIdentifier methods
    //

    public String getPublicId(){
        return fPublicId;
    } // getPublicId():String

    public String getLiteralSystemId(){
        return fLiteralSystemId;
    } // getLiteralSystemId():String

    public String getBaseSystemId(){
        return fBaseSystemId;
    } // getBaseSystemId():String

    public String getExpandedSystemId(){
        return fExpandedSystemId;
    } // getExpandedSystemId():String

    public String getNamespace(){
        return fNamespace;
    } // getNamespace():String
    //
    // Object methods
    //




} // class XMLResourceIdentifierImpl
