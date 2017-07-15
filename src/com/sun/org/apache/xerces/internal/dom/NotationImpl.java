/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.util.URI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;

public class NotationImpl
        extends NodeImpl
        implements Notation{
    //
    // Constants
    //
    static final long serialVersionUID=-764632195890658402L;
    //
    // Data
    //
    protected String name;
    protected String publicId;
    protected String systemId;
    protected String baseURI;
    //
    // Constructors
    //

    public NotationImpl(CoreDocumentImpl ownerDoc,String name){
        super(ownerDoc);
        this.name=name;
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return Node.NOTATION_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }
    //
    // Notation methods
    //

    public String getBaseURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        if(baseURI!=null&&baseURI.length()!=0){// attribute value is always empty string
            try{
                return new URI(baseURI).toString();
            }catch(URI.MalformedURIException e){
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        return baseURI;
    }

    public void setBaseURI(String uri){
        if(needsSyncData()){
            synchronizeData();
        }
        baseURI=uri;
    }
    //
    // Public methods
    //

    public String getPublicId(){
        if(needsSyncData()){
            synchronizeData();
        }
        return publicId;
    } // getPublicId():String

    public String getSystemId(){
        if(needsSyncData()){
            synchronizeData();
        }
        return systemId;
    } // getSystemId():String

    public void setSystemId(String id){
        if(isReadOnly()){
            throw new DOMException(
                    DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null));
        }
        if(needsSyncData()){
            synchronizeData();
        }
        systemId=id;
    } // setSystemId(String)

    public void setPublicId(String id){
        if(isReadOnly()){
            throw new DOMException(
                    DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null));
        }
        if(needsSyncData()){
            synchronizeData();
        }
        publicId=id;
    } // setPublicId(String)
} // class NotationImpl
