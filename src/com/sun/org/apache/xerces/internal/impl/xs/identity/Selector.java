/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001, 2002,2004,2005 The Apache Software Foundation.
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
 * Copyright 2001, 2002,2004,2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.identity;

import com.sun.org.apache.xerces.internal.impl.xpath.XPathException;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xs.ShortList;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class Selector{
    //
    // Data
    //
    protected final XPath fXPath;
    protected final IdentityConstraint fIdentityConstraint;
    // the Identity constraint we're the matcher for.  Only
    // used for selectors!
    protected IdentityConstraint fIDConstraint;
    //
    // Constructors
    //

    public Selector(XPath xpath,
                    IdentityConstraint identityConstraint){
        fXPath=xpath;
        fIdentityConstraint=identityConstraint;
    } // <init>(Selector.XPath,IdentityConstraint)
    //
    // Public methods
    //

    public com.sun.org.apache.xerces.internal.impl.xpath.XPath getXPath(){
        return fXPath;
    } // getXPath():com.sun.org.apache.xerces.internal.v1.schema.identity.XPath

    public IdentityConstraint getIDConstraint(){
        return fIdentityConstraint;
    } // getIDConstraint():IdentityConstraint
    // factory method

    public XPathMatcher createMatcher(FieldActivator activator,int initialDepth){
        return new Matcher(fXPath,activator,initialDepth);
    } // createMatcher(FieldActivator):XPathMatcher
    //
    // Object methods
    //

    public String toString(){
        return fXPath.toString();
    } // toString():String
    //
    // Classes
    //

    public static class XPath
            extends com.sun.org.apache.xerces.internal.impl.xpath.XPath{
        //
        // Constructors
        //

        public XPath(String xpath,SymbolTable symbolTable,
                     NamespaceContext context) throws XPathException{
            super(normalize(xpath),symbolTable,context);
            // verify that an attribute is not selected
            for(int i=0;i<fLocationPaths.length;i++){
                Axis axis=
                        fLocationPaths[i].steps[fLocationPaths[i].steps.length-1].axis;
                if(axis.type==Axis.ATTRIBUTE){
                    throw new XPathException("c-selector-xpath");
                }
            }
        } // <init>(String,SymbolTable,NamespacesScope)

        private static String normalize(String xpath){
            // NOTE: We have to prefix the selector XPath with "./" in
            //       order to handle selectors such as "." that select
            //       the element container because the fields could be
            //       relative to that element. -Ac
            //       Unless xpath starts with a descendant node -Achille Fokoue
            //      ... or a '.' or a '/' - NG
            //  And we also need to prefix exprs to the right of | with ./ - NG
            StringBuffer modifiedXPath=new StringBuffer(xpath.length()+5);
            int unionIndex=-1;
            do{
                if(!(XMLChar.trim(xpath).startsWith("/")||XMLChar.trim(xpath).startsWith("."))){
                    modifiedXPath.append("./");
                }
                unionIndex=xpath.indexOf('|');
                if(unionIndex==-1){
                    modifiedXPath.append(xpath);
                    break;
                }
                modifiedXPath.append(xpath.substring(0,unionIndex+1));
                xpath=xpath.substring(unionIndex+1,xpath.length());
            }while(true);
            return modifiedXPath.toString();
        }
    } // class Selector.XPath

    public class Matcher
            extends XPathMatcher{
        //
        // Data
        //
        protected final FieldActivator fFieldActivator;
        protected final int fInitialDepth;
        protected int fElementDepth;
        protected int fMatchedDepth;
        //
        // Constructors
        //

        public Matcher(XPath xpath,FieldActivator activator,
                       int initialDepth){
            super(xpath);
            fFieldActivator=activator;
            fInitialDepth=initialDepth;
        } // <init>(Selector.XPath,FieldActivator)
        //
        // XMLDocumentFragmentHandler methods
        //

        public void startDocumentFragment(){
            super.startDocumentFragment();
            fElementDepth=0;
            fMatchedDepth=-1;
        } // startDocumentFragment()

        public void startElement(QName element,XMLAttributes attributes){
            super.startElement(element,attributes);
            fElementDepth++;
            // activate the fields, if selector is matched
            //int matched = isMatched();
            if(isMatched()){
/**            (fMatchedDepth == -1 && ((matched & MATCHED) == MATCHED)) ||
 ((matched & MATCHED_DESCENDANT) == MATCHED_DESCENDANT)) { */
                fMatchedDepth=fElementDepth;
                fFieldActivator.startValueScopeFor(fIdentityConstraint,fInitialDepth);
                int count=fIdentityConstraint.getFieldCount();
                for(int i=0;i<count;i++){
                    Field field=fIdentityConstraint.getFieldAt(i);
                    XPathMatcher matcher=fFieldActivator.activateField(field,fInitialDepth);
                    matcher.startElement(element,attributes);
                }
            }
        } // startElement(QName,XMLAttrList,int)

        public void endElement(QName element,XSTypeDefinition type,boolean nillable,Object actualValue,short valueType,ShortList itemValueType){
            super.endElement(element,type,nillable,actualValue,valueType,itemValueType);
            if(fElementDepth--==fMatchedDepth){
                fMatchedDepth=-1;
                fFieldActivator.endValueScopeFor(fIdentityConstraint,fInitialDepth);
            }
        }

        public IdentityConstraint getIdentityConstraint(){
            return fIdentityConstraint;
        } // getIdentityConstraint():IdentityConstraint

        public int getInitialDepth(){
            return fInitialDepth;
        } // getInitialDepth():  int
    } // class Matcher
} // class Selector
