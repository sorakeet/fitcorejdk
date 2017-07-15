/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * Copyright 2001-2005 The Apache Software Foundation.
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
import com.sun.org.apache.xerces.internal.impl.xs.util.ShortListImpl;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xs.ShortList;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class Field{
    //
    // Data
    //
    protected XPath fXPath;
    protected IdentityConstraint fIdentityConstraint;
    //
    // Constructors
    //

    public Field(XPath xpath,
                 IdentityConstraint identityConstraint){
        fXPath=xpath;
        fIdentityConstraint=identityConstraint;
    } // <init>(Field.XPath,IdentityConstraint)
    //
    // Public methods
    //

    public com.sun.org.apache.xerces.internal.impl.xpath.XPath getXPath(){
        return fXPath;
    } // getXPath():com.sun.org.apache.xerces.internal.impl.v1.schema.identity.XPath

    public IdentityConstraint getIdentityConstraint(){
        return fIdentityConstraint;
    } // getIdentityConstraint():IdentityConstraint
    // factory method

    public XPathMatcher createMatcher(FieldActivator activator,ValueStore store){
        return new Matcher(fXPath,activator,store);
    } // createMatcher(ValueStore):XPathMatcher
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

        public XPath(String xpath,
                     SymbolTable symbolTable,
                     NamespaceContext context) throws XPathException{
            // NOTE: We have to prefix the field XPath with "./" in
            //       order to handle selectors such as "@attr" that
            //       select the attribute because the fields could be
            //       relative to the selector element. -Ac
            //       Unless xpath starts with a descendant node -Achille Fokoue
            //      ... or a / or a . - NG
            super(((xpath.trim().startsWith("/")||xpath.trim().startsWith("."))?
                            xpath:"./"+xpath),
                    symbolTable,context);
            // verify that only one attribute is selected per branch
            for(int i=0;i<fLocationPaths.length;i++){
                for(int j=0;j<fLocationPaths[i].steps.length;j++){
                    Axis axis=
                            fLocationPaths[i].steps[j].axis;
                    if(axis.type==Axis.ATTRIBUTE&&
                            (j<fLocationPaths[i].steps.length-1)){
                        throw new XPathException("c-fields-xpaths");
                    }
                }
            }
        } // <init>(String,SymbolTable,NamespacesContext)
    } // class XPath

    protected class Matcher
            extends XPathMatcher{
        //
        // Data
        //
        protected FieldActivator fFieldActivator;
        protected ValueStore fStore;
        //
        // Constructors
        //

        public Matcher(XPath xpath,FieldActivator activator,ValueStore store){
            super(xpath);
            fFieldActivator=activator;
            fStore=store;
        } // <init>(Field.XPath,ValueStore)
        //
        // XPathHandler methods
        //

        protected void handleContent(XSTypeDefinition type,boolean nillable,Object actualValue,short valueType,ShortList itemValueType){
            if(type==null||
                    type.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE&&
                            ((XSComplexTypeDefinition)type).getContentType()
                                    !=XSComplexTypeDefinition.CONTENTTYPE_SIMPLE){
                // the content must be simpleType content
                fStore.reportError("cvc-id.3",new Object[]{
                        fIdentityConstraint.getName(),
                        fIdentityConstraint.getElementName()});
            }
            fMatchedString=actualValue;
            matched(fMatchedString,valueType,itemValueType,nillable);
        } // handleContent(XSElementDecl, String)

        protected void matched(Object actualValue,short valueType,ShortList itemValueType,boolean isNil){
            super.matched(actualValue,valueType,itemValueType,isNil);
            if(isNil&&(fIdentityConstraint.getCategory()==IdentityConstraint.IC_KEY)){
                String code="KeyMatchesNillable";
                fStore.reportError(code,
                        new Object[]{fIdentityConstraint.getElementName(),fIdentityConstraint.getIdentityConstraintName()});
            }
            fStore.addValue(Field.this,actualValue,convertToPrimitiveKind(valueType),convertToPrimitiveKind(itemValueType));
            // once we've stored the value for this field, we set the mayMatch
            // member to false so that, in the same scope, we don't match any more
            // values (and throw an error instead).
            fFieldActivator.setMayMatch(Field.this,Boolean.FALSE);
        } // matched(String)

        private short convertToPrimitiveKind(short valueType){
            /** Primitive datatypes. */
            if(valueType<=XSConstants.NOTATION_DT){
                return valueType;
            }
            /** Types derived from string. */
            if(valueType<=XSConstants.ENTITY_DT){
                return XSConstants.STRING_DT;
            }
            /** Types derived from decimal. */
            if(valueType<=XSConstants.POSITIVEINTEGER_DT){
                return XSConstants.DECIMAL_DT;
            }
            /** Other types. */
            return valueType;
        }

        private ShortList convertToPrimitiveKind(ShortList itemValueType){
            if(itemValueType!=null){
                int i;
                final int length=itemValueType.getLength();
                for(i=0;i<length;++i){
                    short type=itemValueType.item(i);
                    if(type!=convertToPrimitiveKind(type)){
                        break;
                    }
                }
                if(i!=length){
                    final short[] arr=new short[length];
                    for(int j=0;j<i;++j){
                        arr[j]=itemValueType.item(j);
                    }
                    for(;i<length;++i){
                        arr[i]=convertToPrimitiveKind(itemValueType.item(i));
                    }
                    return new ShortListImpl(arr,arr.length);
                }
            }
            return itemValueType;
        }
    } // class Matcher
} // class Field
