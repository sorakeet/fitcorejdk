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

import org.w3c.dom.DOMException;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;

public class EntityImpl
        extends ParentNode
        implements Entity{
    //
    // Constants
    //
    static final long serialVersionUID=-3575760943444303423L;
    //
    // Data
    //
    protected String name;
    protected String publicId;
    protected String systemId;
    protected String encoding;
    protected String inputEncoding;
    protected String version;
    protected String notationName;
    protected String baseURI;
    //
    // Constructors
    //

    public EntityImpl(CoreDocumentImpl ownerDoc,String name){
        super(ownerDoc);
        this.name=name;
        isReadOnly(true);
    }
    //
    // Node methods
    //

    public short getNodeType(){
        return Node.ENTITY_NODE;
    }

    public String getNodeName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return name;
    }

    public void setNodeValue(String x)
            throws DOMException{
        if(ownerDocument.errorChecking&&isReadOnly()){
            String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
        }
    }

    public void setPrefix(String prefix)
            throws DOMException{
        if(ownerDocument.errorChecking&&isReadOnly()){
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                            "NO_MODIFICATION_ALLOWED_ERR",null));
        }
    }

    public String getBaseURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        return (baseURI!=null)?baseURI:((CoreDocumentImpl)getOwnerDocument()).getBaseURI();
    }
    //
    // Entity methods
    //

    public void setBaseURI(String uri){
        if(needsSyncData()){
            synchronizeData();
        }
        baseURI=uri;
    }

    public Node cloneNode(boolean deep){
        EntityImpl newentity=(EntityImpl)super.cloneNode(deep);
        newentity.setReadOnly(true,deep);
        return newentity;
    }

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

    public String getNotationName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return notationName;
    } // getNotationName():String
    //
    // Public methods
    //

    public String getInputEncoding(){
        if(needsSyncData()){
            synchronizeData();
        }
        return inputEncoding;
    }

    public String getXmlEncoding(){
        if(needsSyncData()){
            synchronizeData();
        }
        return encoding;
    } // getVersion():String

    public String getXmlVersion(){
        if(needsSyncData()){
            synchronizeData();
        }
        return version;
    } // getVersion():String

    public void setXmlVersion(String value){
        if(needsSyncData()){
            synchronizeData();
        }
        version=value;
    } // setVersion (String)

    public void setXmlEncoding(String value){
        if(needsSyncData()){
            synchronizeData();
        }
        encoding=value;
    } // setEncoding (String)

    public void setInputEncoding(String inputEncoding){
        if(needsSyncData()){
            synchronizeData();
        }
        this.inputEncoding=inputEncoding;
    }

    public void setNotationName(String name){
        if(needsSyncData()){
            synchronizeData();
        }
        notationName=name;
    } // setNotationName(String)

    public void setSystemId(String id){
        if(needsSyncData()){
            synchronizeData();
        }
        systemId=id;
    } // setSystemId(String)

    public void setPublicId(String id){
        if(needsSyncData()){
            synchronizeData();
        }
        publicId=id;
    } // setPublicId(String)
} // class EntityImpl
