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
package com.sun.org.apache.xml.internal.security.transforms.params;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.transforms.TransformParam;
import com.sun.org.apache.xml.internal.security.utils.ElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XPathFilterCHGPContainer extends ElementProxy implements TransformParam{
    public static final String TRANSFORM_XPATHFILTERCHGP=
            "http://www.nue.et-inf.uni-siegen.de/~geuer-pollmann/#xpathFilter";
    public static final String _TAG_XPATHCHGP="XPathAlternative";
    public static final String _ATT_INCLUDESLASH="IncludeSlashPolicy";
    public static final boolean IncludeSlash=true;
    public static final boolean ExcludeSlash=false;
    private static final String _TAG_INCLUDE_BUT_SEARCH="IncludeButSearch";
    private static final String _TAG_EXCLUDE_BUT_SEARCH="ExcludeButSearch";
    private static final String _TAG_EXCLUDE="Exclude";

    private XPathFilterCHGPContainer(){
        // no instantiation
    }

    private XPathFilterCHGPContainer(
            Document doc,boolean includeSlashPolicy,String includeButSearch,
            String excludeButSearch,String exclude
    ){
        super(doc);
        if(includeSlashPolicy){
            this.constructionElement.setAttributeNS(
                    null,XPathFilterCHGPContainer._ATT_INCLUDESLASH,"true"
            );
        }else{
            this.constructionElement.setAttributeNS(
                    null,XPathFilterCHGPContainer._ATT_INCLUDESLASH,"false"
            );
        }
        if((includeButSearch!=null)&&(includeButSearch.trim().length()>0)){
            Element includeButSearchElem=
                    ElementProxy.createElementForFamily(
                            doc,this.getBaseNamespace(),XPathFilterCHGPContainer._TAG_INCLUDE_BUT_SEARCH
                    );
            includeButSearchElem.appendChild(
                    this.doc.createTextNode(indentXPathText(includeButSearch))
            );
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(includeButSearchElem);
        }
        if((excludeButSearch!=null)&&(excludeButSearch.trim().length()>0)){
            Element excludeButSearchElem=
                    ElementProxy.createElementForFamily(
                            doc,this.getBaseNamespace(),XPathFilterCHGPContainer._TAG_EXCLUDE_BUT_SEARCH
                    );
            excludeButSearchElem.appendChild(
                    this.doc.createTextNode(indentXPathText(excludeButSearch)));
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(excludeButSearchElem);
        }
        if((exclude!=null)&&(exclude.trim().length()>0)){
            Element excludeElem=
                    ElementProxy.createElementForFamily(
                            doc,this.getBaseNamespace(),XPathFilterCHGPContainer._TAG_EXCLUDE);
            excludeElem.appendChild(this.doc.createTextNode(indentXPathText(exclude)));
            XMLUtils.addReturnToElement(this.constructionElement);
            this.constructionElement.appendChild(excludeElem);
        }
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    static String indentXPathText(String xp){
        if((xp.length()>2)&&(!Character.isWhitespace(xp.charAt(0)))){
            return "\n"+xp+"\n";
        }
        return xp;
    }

    public final String getBaseNamespace(){
        return TRANSFORM_XPATHFILTERCHGP;
    }

    public final String getBaseLocalName(){
        return XPathFilterCHGPContainer._TAG_XPATHCHGP;
    }

    private XPathFilterCHGPContainer(Element element,String BaseURI)
            throws XMLSecurityException{
        super(element,BaseURI);
    }

    public static XPathFilterCHGPContainer getInstance(
            Document doc,boolean includeSlashPolicy,String includeButSearch,
            String excludeButSearch,String exclude
    ){
        return new XPathFilterCHGPContainer(
                doc,includeSlashPolicy,includeButSearch,excludeButSearch,exclude);
    }

    public static XPathFilterCHGPContainer getInstance(
            Element element,String BaseURI
    ) throws XMLSecurityException{
        return new XPathFilterCHGPContainer(element,BaseURI);
    }

    public String getIncludeButSearch(){
        return this.getXStr(XPathFilterCHGPContainer._TAG_INCLUDE_BUT_SEARCH);
    }

    private String getXStr(String type){
        if(this.length(this.getBaseNamespace(),type)!=1){
            return "";
        }
        Element xElem=
                XMLUtils.selectNode(
                        this.constructionElement.getFirstChild(),this.getBaseNamespace(),type,0
                );
        return XMLUtils.getFullTextChildrenFromElement(xElem);
    }

    public String getExcludeButSearch(){
        return this.getXStr(XPathFilterCHGPContainer._TAG_EXCLUDE_BUT_SEARCH);
    }

    public String getExclude(){
        return this.getXStr(XPathFilterCHGPContainer._TAG_EXCLUDE);
    }

    public boolean getIncludeSlashPolicy(){
        return this.constructionElement.getAttributeNS(
                null,XPathFilterCHGPContainer._ATT_INCLUDESLASH).equals("true");
    }

    public Node getHereContextNodeIncludeButSearch(){
        return this.getHereContextNode(XPathFilterCHGPContainer._TAG_INCLUDE_BUT_SEARCH);
    }

    private Node getHereContextNode(String type){
        if(this.length(this.getBaseNamespace(),type)!=1){
            return null;
        }
        return XMLUtils.selectNodeText(
                this.constructionElement.getFirstChild(),this.getBaseNamespace(),type,0
        );
    }

    public Node getHereContextNodeExcludeButSearch(){
        return this.getHereContextNode(XPathFilterCHGPContainer._TAG_EXCLUDE_BUT_SEARCH);
    }

    public Node getHereContextNodeExclude(){
        return this.getHereContextNode(XPathFilterCHGPContainer._TAG_EXCLUDE);
    }
}
