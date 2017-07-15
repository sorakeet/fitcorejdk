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
import com.sun.org.apache.xml.internal.security.utils.HelperNodeList;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPath2FilterContainer extends ElementProxy implements TransformParam{
    public static final String _TAG_XPATH2="XPath";
    public static final String XPathFilter2NS=
            "http://www.w3.org/2002/06/xmldsig-filter2";
    private static final String _ATT_FILTER="Filter";
    private static final String _ATT_FILTER_VALUE_INTERSECT="intersect";
    public static final String INTERSECT=
            XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT;
    private static final String _ATT_FILTER_VALUE_SUBTRACT="subtract";
    public static final String SUBTRACT=
            XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT;
    private static final String _ATT_FILTER_VALUE_UNION="union";
    public static final String UNION=
            XPath2FilterContainer._ATT_FILTER_VALUE_UNION;

    private XPath2FilterContainer(){
        // no instantiation
    }

    private XPath2FilterContainer(Document doc,String xpath2filter,String filterType){
        super(doc);
        this.constructionElement.setAttributeNS(
                null,XPath2FilterContainer._ATT_FILTER,filterType);
        this.constructionElement.appendChild(doc.createTextNode(xpath2filter));
    }

    private XPath2FilterContainer(Element element,String BaseURI) throws XMLSecurityException{
        super(element,BaseURI);
        String filterStr=
                this.constructionElement.getAttributeNS(null,XPath2FilterContainer._ATT_FILTER);
        if(!filterStr.equals(XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT)
                &&!filterStr.equals(XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT)
                &&!filterStr.equals(XPath2FilterContainer._ATT_FILTER_VALUE_UNION)){
            Object exArgs[]={XPath2FilterContainer._ATT_FILTER,filterStr,
                    XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT
                            +", "
                            +XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT
                            +" or "
                            +XPath2FilterContainer._ATT_FILTER_VALUE_UNION};
            throw new XMLSecurityException("attributeValueIllegal",exArgs);
        }
    }

    public static XPath2FilterContainer newInstanceIntersect(
            Document doc,String xpath2filter
    ){
        return new XPath2FilterContainer(
                doc,xpath2filter,XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT);
    }

    public static XPath2FilterContainer newInstanceSubtract(Document doc,String xpath2filter){
        return new XPath2FilterContainer(
                doc,xpath2filter,XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT);
    }

    public static XPath2FilterContainer newInstanceUnion(Document doc,String xpath2filter){
        return new XPath2FilterContainer(
                doc,xpath2filter,XPath2FilterContainer._ATT_FILTER_VALUE_UNION);
    }

    public static NodeList newInstances(Document doc,String[][] params){
        HelperNodeList nl=new HelperNodeList();
        XMLUtils.addReturnToElement(doc,nl);
        for(int i=0;i<params.length;i++){
            String type=params[i][0];
            String xpath=params[i][1];
            if(!(type.equals(XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT)
                    ||type.equals(XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT)
                    ||type.equals(XPath2FilterContainer._ATT_FILTER_VALUE_UNION))){
                throw new IllegalArgumentException("The type("+i+")=\""+type
                        +"\" is illegal");
            }
            XPath2FilterContainer c=new XPath2FilterContainer(doc,xpath,type);
            nl.appendChild(c.getElement());
            XMLUtils.addReturnToElement(doc,nl);
        }
        return nl;
    }

    public static XPath2FilterContainer newInstance(
            Element element,String BaseURI
    ) throws XMLSecurityException{
        return new XPath2FilterContainer(element,BaseURI);
    }

    public boolean isIntersect(){
        return this.constructionElement.getAttributeNS(
                null,XPath2FilterContainer._ATT_FILTER
        ).equals(XPath2FilterContainer._ATT_FILTER_VALUE_INTERSECT);
    }

    public boolean isSubtract(){
        return this.constructionElement.getAttributeNS(
                null,XPath2FilterContainer._ATT_FILTER
        ).equals(XPath2FilterContainer._ATT_FILTER_VALUE_SUBTRACT);
    }

    public boolean isUnion(){
        return this.constructionElement.getAttributeNS(
                null,XPath2FilterContainer._ATT_FILTER
        ).equals(XPath2FilterContainer._ATT_FILTER_VALUE_UNION);
    }

    public String getXPathFilterStr(){
        return this.getTextFromTextChild();
    }

    public Node getXPathFilterTextNode(){
        NodeList children=this.constructionElement.getChildNodes();
        int length=children.getLength();
        for(int i=0;i<length;i++){
            if(children.item(i).getNodeType()==Node.TEXT_NODE){
                return children.item(i);
            }
        }
        return null;
    }

    public final String getBaseNamespace(){
        return XPath2FilterContainer.XPathFilter2NS;
    }

    public final String getBaseLocalName(){
        return XPath2FilterContainer._TAG_XPATH2;
    }
}
