/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * Copyright 1999-2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import org.w3c.dom.DOMException;

public class AttrNSImpl
        extends AttrImpl{
    //
    // Constants
    //
    static final long serialVersionUID=-781906615369795414L;
    static final String xmlnsURI="http://www.w3.org/2000/xmlns/";
    static final String xmlURI="http://www.w3.org/XML/1998/namespace";
    //
    // Data
    //
    protected String namespaceURI;
    protected String localName;

    public AttrNSImpl(){
    }

    protected AttrNSImpl(CoreDocumentImpl ownerDocument,
                         String namespaceURI,
                         String qualifiedName){
        super(ownerDocument,qualifiedName);
        setName(namespaceURI,qualifiedName);
    }

    private void setName(String namespaceURI,String qname){
        CoreDocumentImpl ownerDocument=ownerDocument();
        String prefix;
        // DOM Level 3: namespace URI is never empty string.
        this.namespaceURI=namespaceURI;
        if(namespaceURI!=null){
            this.namespaceURI=(namespaceURI.length()==0)?null
                    :namespaceURI;
        }
        int colon1=qname.indexOf(':');
        int colon2=qname.lastIndexOf(':');
        ownerDocument.checkNamespaceWF(qname,colon1,colon2);
        if(colon1<0){
            // there is no prefix
            localName=qname;
            if(ownerDocument.errorChecking){
                ownerDocument.checkQName(null,localName);
                if(qname.equals("xmlns")&&(namespaceURI==null
                        ||!namespaceURI.equals(NamespaceContext.XMLNS_URI))
                        ||(namespaceURI!=null&&namespaceURI.equals(NamespaceContext.XMLNS_URI)
                        &&!qname.equals("xmlns"))){
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "NAMESPACE_ERR",
                                    null);
                    throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                }
            }
        }else{
            prefix=qname.substring(0,colon1);
            localName=qname.substring(colon2+1);
            ownerDocument.checkQName(prefix,localName);
            ownerDocument.checkDOMNSErr(prefix,namespaceURI);
        }
    }

    // when local name is known
    public AttrNSImpl(CoreDocumentImpl ownerDocument,
                      String namespaceURI,
                      String qualifiedName,
                      String localName){
        super(ownerDocument,qualifiedName);
        this.localName=localName;
        this.namespaceURI=namespaceURI;
    }

    // for DeferredAttrImpl
    protected AttrNSImpl(CoreDocumentImpl ownerDocument,
                         String value){
        super(ownerDocument,value);
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. It is expected to be
    // called after the Attr has been detached for one thing.
    // CoreDocumentImpl does all the work.
    void rename(String namespaceURI,String qualifiedName){
        if(needsSyncData()){
            synchronizeData();
        }
        this.name=qualifiedName;
        setName(namespaceURI,qualifiedName);
    }

    public void setValues(CoreDocumentImpl ownerDocument,
                          String namespaceURI,
                          String qualifiedName,
                          String localName){
        super.textNode=null;
        super.flags=0;
        isSpecified(true);
        hasStringValue(true);
        super.setOwnerDocument(ownerDocument);
        this.localName=localName;
        this.namespaceURI=namespaceURI;
        super.name=qualifiedName;
        super.value=null;
    }
    //
    // DOM2: Namespace methods
    //

    public String getNamespaceURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        // REVIST: This code could/should be done at a lower-level, such that
        // the namespaceURI is set properly upon creation. However, there still
        // seems to be some DOM spec interpretation grey-area.
        return namespaceURI;
    }

    public String getPrefix(){
        if(needsSyncData()){
            synchronizeData();
        }
        int index=name.indexOf(':');
        return index<0?null:name.substring(0,index);
    }

    public void setPrefix(String prefix)
            throws DOMException{
        if(needsSyncData()){
            synchronizeData();
        }
        if(ownerDocument().errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,msg);
            }
            if(prefix!=null&&prefix.length()!=0){
                if(!CoreDocumentImpl.isXMLName(prefix,ownerDocument().isXML11Version())){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
                    throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
                }
                if(namespaceURI==null||prefix.indexOf(':')>=0){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                    throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                }
                if(prefix.equals("xmlns")){
                    if(!namespaceURI.equals(xmlnsURI)){
                        String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                        throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                    }
                }else if(prefix.equals("xml")){
                    if(!namespaceURI.equals(xmlURI)){
                        String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                        throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                    }
                }else if(name.equals("xmlns")){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                    throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                }
            }
        }
        // update node name with new qualifiedName
        if(prefix!=null&&prefix.length()!=0){
            name=prefix+":"+localName;
        }else{
            name=localName;
        }
    }

    public String getLocalName(){
        if(needsSyncData()){
            synchronizeData();
        }
        return localName;
    }

    public String getTypeName(){
        if(type!=null){
            if(type instanceof XSSimpleTypeDecl){
                return ((XSSimpleTypeDecl)type).getName();
            }
            return (String)type;
        }
        return null;
    }

    public String getTypeNamespace(){
        if(type!=null){
            if(type instanceof XSSimpleTypeDecl){
                return ((XSSimpleTypeDecl)type).getNamespace();
            }
            return DTD_URI;
        }
        return null;
    }

    public boolean isDerivedFrom(String typeNamespaceArg,
                                 String typeNameArg,
                                 int derivationMethod){
        if(type!=null){
            if(type instanceof XSSimpleTypeDecl){
                return ((XSSimpleTypeDecl)type).isDOMDerivedFrom(
                        typeNamespaceArg,typeNameArg,derivationMethod);
            }
        }
        return false;
    }
}
