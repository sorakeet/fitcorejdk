/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sun.org.apache.xml.internal.security.utils;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import org.w3c.dom.*;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ElementProxy{
    protected static final java.util.logging.Logger log=
            java.util.logging.Logger.getLogger(ElementProxy.class.getName());
    private static Map<String,String> prefixMappings=new ConcurrentHashMap<String,String>();
    protected Element constructionElement=null;
    protected String baseURI=null;
    protected Document doc=null;

    public ElementProxy(){
    }

    public ElementProxy(Document doc){
        if(doc==null){
            throw new RuntimeException("Document is null");
        }
        this.doc=doc;
        this.constructionElement=
                createElementForFamilyLocal(this.doc,this.getBaseNamespace(),this.getBaseLocalName());
    }

    public abstract String getBaseLocalName();

    protected Element createElementForFamilyLocal(
            Document doc,String namespace,String localName
    ){
        Element result=null;
        if(namespace==null){
            result=doc.createElementNS(null,localName);
        }else{
            String baseName=this.getBaseNamespace();
            String prefix=ElementProxy.getDefaultPrefix(baseName);
            if((prefix==null)||(prefix.length()==0)){
                result=doc.createElementNS(namespace,localName);
                result.setAttributeNS(Constants.NamespaceSpecNS,"xmlns",namespace);
            }else{
                result=doc.createElementNS(namespace,prefix+":"+localName);
                result.setAttributeNS(Constants.NamespaceSpecNS,"xmlns:"+prefix,namespace);
            }
        }
        return result;
    }

    public abstract String getBaseNamespace();

    public static String getDefaultPrefix(String namespace){
        return prefixMappings.get(namespace);
    }

    public ElementProxy(Element element,String BaseURI) throws XMLSecurityException{
        if(element==null){
            throw new XMLSecurityException("ElementProxy.nullElement");
        }
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"setElement(\""+element.getTagName()+"\", \""+BaseURI+"\")");
        }
        this.doc=element.getOwnerDocument();
        this.constructionElement=element;
        this.baseURI=BaseURI;
        this.guaranteeThatElementInCorrectSpace();
    }

    void guaranteeThatElementInCorrectSpace() throws XMLSecurityException{
        String expectedLocalName=this.getBaseLocalName();
        String expectedNamespaceUri=this.getBaseNamespace();
        String actualLocalName=this.constructionElement.getLocalName();
        String actualNamespaceUri=this.constructionElement.getNamespaceURI();
        if(!expectedNamespaceUri.equals(actualNamespaceUri)
                &&!expectedLocalName.equals(actualLocalName)){
            Object exArgs[]={actualNamespaceUri+":"+actualLocalName,
                    expectedNamespaceUri+":"+expectedLocalName};
            throw new XMLSecurityException("xml.WrongElement",exArgs);
        }
    }

    public static Element createElementForFamily(Document doc,String namespace,String localName){
        Element result=null;
        String prefix=ElementProxy.getDefaultPrefix(namespace);
        if(namespace==null){
            result=doc.createElementNS(null,localName);
        }else{
            if((prefix==null)||(prefix.length()==0)){
                result=doc.createElementNS(namespace,localName);
                result.setAttributeNS(Constants.NamespaceSpecNS,"xmlns",namespace);
            }else{
                result=doc.createElementNS(namespace,prefix+":"+localName);
                result.setAttributeNS(Constants.NamespaceSpecNS,"xmlns:"+prefix,namespace);
            }
        }
        return result;
    }

    public static void registerDefaultPrefixes() throws XMLSecurityException{
        setDefaultPrefix("http://www.w3.org/2000/09/xmldsig#","ds");
        setDefaultPrefix("http://www.w3.org/2001/04/xmlenc#","xenc");
        setDefaultPrefix("http://www.w3.org/2009/xmlenc11#","xenc11");
        setDefaultPrefix("http://www.xmlsecurity.org/experimental#","experimental");
        setDefaultPrefix("http://www.w3.org/2002/04/xmldsig-filter2","dsig-xpath-old");
        setDefaultPrefix("http://www.w3.org/2002/06/xmldsig-filter2","dsig-xpath");
        setDefaultPrefix("http://www.w3.org/2001/10/xml-exc-c14n#","ec");
        setDefaultPrefix(
                "http://www.nue.et-inf.uni-siegen.de/~geuer-pollmann/#xpathFilter","xx"
        );
    }

    public static void setDefaultPrefix(String namespace,String prefix)
            throws XMLSecurityException{
        JavaUtils.checkRegisterPermission();
        if(prefixMappings.containsValue(prefix)){
            String storedPrefix=prefixMappings.get(namespace);
            if(!storedPrefix.equals(prefix)){
                Object exArgs[]={prefix,namespace,storedPrefix};
                throw new XMLSecurityException("prefix.AlreadyAssigned",exArgs);
            }
        }
        if(Constants.SignatureSpecNS.equals(namespace)){
            XMLUtils.setDsPrefix(prefix);
        }
        if(EncryptionConstants.EncryptionSpecNS.equals(namespace)){
            XMLUtils.setXencPrefix(prefix);
        }
        prefixMappings.put(namespace,prefix);
    }

    public void setElement(Element element,String BaseURI) throws XMLSecurityException{
        if(element==null){
            throw new XMLSecurityException("ElementProxy.nullElement");
        }
        if(log.isLoggable(java.util.logging.Level.FINE)){
            log.log(java.util.logging.Level.FINE,"setElement("+element.getTagName()+", \""+BaseURI+"\"");
        }
        this.doc=element.getOwnerDocument();
        this.constructionElement=element;
        this.baseURI=BaseURI;
    }

    public final NodeList getElementPlusReturns(){
        HelperNodeList nl=new HelperNodeList();
        nl.appendChild(this.doc.createTextNode("\n"));
        nl.appendChild(this.getElement());
        nl.appendChild(this.doc.createTextNode("\n"));
        return nl;
    }

    public final Element getElement(){
        return this.constructionElement;
    }

    public Document getDocument(){
        return this.doc;
    }

    public String getBaseURI(){
        return this.baseURI;
    }

    public void addBigIntegerElement(BigInteger bi,String localname){
        if(bi!=null){
            Element e=XMLUtils.createElementInSignatureSpace(this.doc,localname);
            Base64.fillElementWithBigInteger(e,bi);
            this.constructionElement.appendChild(e);
            XMLUtils.addReturnToElement(this.constructionElement);
        }
    }

    public void addBase64Element(byte[] bytes,String localname){
        if(bytes!=null){
            Element e=Base64.encodeToElement(this.doc,localname,bytes);
            this.constructionElement.appendChild(e);
            if(!XMLUtils.ignoreLineBreaks()){
                this.constructionElement.appendChild(this.doc.createTextNode("\n"));
            }
        }
    }

    public void addTextElement(String text,String localname){
        Element e=XMLUtils.createElementInSignatureSpace(this.doc,localname);
        Text t=this.doc.createTextNode(text);
        e.appendChild(t);
        this.constructionElement.appendChild(e);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addBase64Text(byte[] bytes){
        if(bytes!=null){
            Text t=XMLUtils.ignoreLineBreaks()
                    ?this.doc.createTextNode(Base64.encode(bytes))
                    :this.doc.createTextNode("\n"+Base64.encode(bytes)+"\n");
            this.constructionElement.appendChild(t);
        }
    }

    public void addText(String text){
        if(text!=null){
            Text t=this.doc.createTextNode(text);
            this.constructionElement.appendChild(t);
        }
    }

    public BigInteger getBigIntegerFromChildElement(
            String localname,String namespace
    ) throws Base64DecodingException{
        return Base64.decodeBigIntegerFromText(
                XMLUtils.selectNodeText(
                        this.constructionElement.getFirstChild(),namespace,localname,0
                )
        );
    }

    @Deprecated
    public byte[] getBytesFromChildElement(String localname,String namespace)
            throws XMLSecurityException{
        Element e=
                XMLUtils.selectNode(
                        this.constructionElement.getFirstChild(),namespace,localname,0
                );
        return Base64.decode(e);
    }

    public String getTextFromChildElement(String localname,String namespace){
        return XMLUtils.selectNode(
                this.constructionElement.getFirstChild(),
                namespace,
                localname,
                0).getTextContent();
    }

    public byte[] getBytesFromTextChild() throws XMLSecurityException{
        return Base64.decode(XMLUtils.getFullTextChildrenFromElement(this.constructionElement));
    }

    public String getTextFromTextChild(){
        return XMLUtils.getFullTextChildrenFromElement(this.constructionElement);
    }

    public int length(String namespace,String localname){
        int number=0;
        Node sibling=this.constructionElement.getFirstChild();
        while(sibling!=null){
            if(localname.equals(sibling.getLocalName())
                    &&namespace.equals(sibling.getNamespaceURI())){
                number++;
            }
            sibling=sibling.getNextSibling();
        }
        return number;
    }

    public void setXPathNamespaceContext(String prefix,String uri)
            throws XMLSecurityException{
        String ns;
        if((prefix==null)||(prefix.length()==0)){
            throw new XMLSecurityException("defaultNamespaceCannotBeSetHere");
        }else if(prefix.equals("xmlns")){
            throw new XMLSecurityException("defaultNamespaceCannotBeSetHere");
        }else if(prefix.startsWith("xmlns:")){
            ns=prefix;//"xmlns:" + prefix.substring("xmlns:".length());
        }else{
            ns="xmlns:"+prefix;
        }
        Attr a=this.constructionElement.getAttributeNodeNS(Constants.NamespaceSpecNS,ns);
        if(a!=null){
            if(!a.getNodeValue().equals(uri)){
                Object exArgs[]={ns,this.constructionElement.getAttributeNS(null,ns)};
                throw new XMLSecurityException("namespacePrefixAlreadyUsedByOtherURI",exArgs);
            }
            return;
        }
        this.constructionElement.setAttributeNS(Constants.NamespaceSpecNS,ns,uri);
    }
}
