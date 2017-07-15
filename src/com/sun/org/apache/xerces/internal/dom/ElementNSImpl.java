/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
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
 * Copyright 1999-2002,2004,2005 The Apache Software Foundation.
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
import com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

public class ElementNSImpl
        extends ElementImpl{
    //
    // Constants
    //
    static final long serialVersionUID=-9142310625494392642L;
    static final String xmlURI="http://www.w3.org/XML/1998/namespace";
    //
    // Data
    //
    protected String namespaceURI;
    protected String localName;
    // REVISIT: we are losing the type information in DOM during serialization
    transient XSTypeDefinition type;

    protected ElementNSImpl(){
        super();
    }

    protected ElementNSImpl(CoreDocumentImpl ownerDocument,
                            String namespaceURI,
                            String qualifiedName)
            throws DOMException{
        super(ownerDocument,qualifiedName);
        setName(namespaceURI,qualifiedName);
    }

    private void setName(String namespaceURI,String qname){
        String prefix;
        // DOM Level 3: namespace URI is never empty string.
        this.namespaceURI=namespaceURI;
        if(namespaceURI!=null){
            //convert the empty string to 'null'
            this.namespaceURI=(namespaceURI.length()==0)?null:namespaceURI;
        }
        int colon1, colon2;
        //NAMESPACE_ERR:
        //1. if the qualified name is 'null' it is malformed.
        //2. or if the qualifiedName is null and the namespaceURI is different from null,
        // We dont need to check for namespaceURI != null, if qualified name is null throw DOMException.
        if(qname==null){
            String msg=
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "NAMESPACE_ERR",
                            null);
            throw new DOMException(DOMException.NAMESPACE_ERR,msg);
        }else{
            colon1=qname.indexOf(':');
            colon2=qname.lastIndexOf(':');
        }
        ownerDocument.checkNamespaceWF(qname,colon1,colon2);
        if(colon1<0){
            // there is no prefix
            localName=qname;
            if(ownerDocument.errorChecking){
                ownerDocument.checkQName(null,localName);
                if(qname.equals("xmlns")
                        &&(namespaceURI==null
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
        }//there is a prefix
        else{
            prefix=qname.substring(0,colon1);
            localName=qname.substring(colon2+1);
            //NAMESPACE_ERR:
            //1. if the qualifiedName has a prefix and the namespaceURI is null,
            //2. or if the qualifiedName has a prefix that is "xml" and the namespaceURI
            //is different from " http://www.w3.org/XML/1998/namespace"
            if(ownerDocument.errorChecking){
                if(namespaceURI==null||(prefix.equals("xml")&&!namespaceURI.equals(NamespaceContext.XML_URI))){
                    String msg=
                            DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "NAMESPACE_ERR",
                                    null);
                    throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                }
                ownerDocument.checkQName(prefix,localName);
                ownerDocument.checkDOMNSErr(prefix,namespaceURI);
            }
        }
    }

    // when local name is known
    protected ElementNSImpl(CoreDocumentImpl ownerDocument,
                            String namespaceURI,String qualifiedName,
                            String localName)
            throws DOMException{
        super(ownerDocument,qualifiedName);
        this.localName=localName;
        this.namespaceURI=namespaceURI;
    }

    // for DeferredElementImpl
    protected ElementNSImpl(CoreDocumentImpl ownerDocument,
                            String value){
        super(ownerDocument,value);
    }

    // Support for DOM Level 3 renameNode method.
    // Note: This only deals with part of the pb. CoreDocumentImpl
    // does all the work.
    void rename(String namespaceURI,String qualifiedName){
        if(needsSyncData()){
            synchronizeData();
        }
        this.name=qualifiedName;
        setName(namespaceURI,qualifiedName);
        reconcileDefaultAttributes();
    }

    protected void setValues(CoreDocumentImpl ownerDocument,
                             String namespaceURI,String qualifiedName,
                             String localName){
        // remove children first
        firstChild=null;
        previousSibling=null;
        nextSibling=null;
        fNodeListCache=null;
        // set owner document
        attributes=null;
        super.flags=0;
        setOwnerDocument(ownerDocument);
        // synchronizeData will initialize attributes
        needsSyncData(true);
        super.name=qualifiedName;
        this.localName=localName;
        this.namespaceURI=namespaceURI;
    }
    //
    // Node methods
    //
    //
    //DOM2: Namespace methods.
    //

    public String getNamespaceURI(){
        if(needsSyncData()){
            synchronizeData();
        }
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
        if(ownerDocument.errorChecking){
            if(isReadOnly()){
                String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NO_MODIFICATION_ALLOWED_ERR",null);
                throw new DOMException(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR,
                        msg);
            }
            if(prefix!=null&&prefix.length()!=0){
                if(!CoreDocumentImpl.isXMLName(prefix,ownerDocument.isXML11Version())){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"INVALID_CHARACTER_ERR",null);
                    throw new DOMException(DOMException.INVALID_CHARACTER_ERR,msg);
                }
                if(namespaceURI==null||prefix.indexOf(':')>=0){
                    String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                    throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                }else if(prefix.equals("xml")){
                    if(!namespaceURI.equals(xmlURI)){
                        String msg=DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,"NAMESPACE_ERR",null);
                        throw new DOMException(DOMException.NAMESPACE_ERR,msg);
                    }
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

    public String getBaseURI(){
        if(needsSyncData()){
            synchronizeData();
        }
        // Absolute base URI is computed according to XML Base (http://www.w3.org/TR/xmlbase/#granularity)
        // 1.  the base URI specified by an xml:base attribute on the element, if one exists
        if(attributes!=null){
            Attr attrNode=(Attr)attributes.getNamedItemNS("http://www.w3.org/XML/1998/namespace","base");
            if(attrNode!=null){
                String uri=attrNode.getNodeValue();
                if(uri.length()!=0){// attribute value is always empty string
                    try{
                        uri=new URI(uri).toString();
                    }catch(URI.MalformedURIException e){
                        // This may be a relative URI.
                        // Start from the base URI of the parent, or if this node has no parent, the owner node.
                        NodeImpl parentOrOwner=(parentNode()!=null)?parentNode():ownerNode;
                        // Make any parentURI into a URI object to use with the URI(URI, String) constructor.
                        String parentBaseURI=(parentOrOwner!=null)?parentOrOwner.getBaseURI():null;
                        if(parentBaseURI!=null){
                            try{
                                uri=new URI(new URI(parentBaseURI),uri).toString();
                            }catch(URI.MalformedURIException ex){
                                // This should never happen: parent should have checked the URI and returned null if invalid.
                                return null;
                            }
                            return uri;
                        }
                        // REVISIT: what should happen in this case?
                        return null;
                    }
                    return uri;
                }
            }
        }
        //2.the base URI of the element's parent element within the document or external entity,
        //if one exists
        String parentElementBaseURI=(this.parentNode()!=null)?this.parentNode().getBaseURI():null;
        //base URI of parent element is not null
        if(parentElementBaseURI!=null){
            try{
                //return valid absolute base URI
                return new URI(parentElementBaseURI).toString();
            }catch(URI.MalformedURIException e){
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        //3. the base URI of the document entity or external entity containing the element
        String baseURI=(this.ownerNode!=null)?this.ownerNode.getBaseURI():null;
        if(baseURI!=null){
            try{
                //return valid absolute base URI
                return new URI(baseURI).toString();
            }catch(URI.MalformedURIException e){
                // REVISIT: what should happen in this case?
                return null;
            }
        }
        return null;
    }

    public String getTypeName(){
        if(type!=null){
            if(type instanceof XSSimpleTypeDecl){
                return ((XSSimpleTypeDecl)type).getTypeName();
            }else if(type instanceof XSComplexTypeDecl){
                return ((XSComplexTypeDecl)type).getTypeName();
            }
        }
        return null;
    }

    public String getTypeNamespace(){
        if(type!=null){
            return type.getNamespace();
        }
        return null;
    }

    public boolean isDerivedFrom(String typeNamespaceArg,String typeNameArg,
                                 int derivationMethod){
        if(needsSyncData()){
            synchronizeData();
        }
        if(type!=null){
            if(type instanceof XSSimpleTypeDecl){
                return ((XSSimpleTypeDecl)type).isDOMDerivedFrom(
                        typeNamespaceArg,typeNameArg,derivationMethod);
            }else if(type instanceof XSComplexTypeDecl){
                return ((XSComplexTypeDecl)type).isDOMDerivedFrom(
                        typeNamespaceArg,typeNameArg,derivationMethod);
            }
        }
        return false;
    }

    public void setType(XSTypeDefinition type){
        this.type=type;
    }
}
