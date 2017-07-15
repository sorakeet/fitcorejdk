/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Copyright 2001-2004 The Apache Software Foundation.
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

import com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.xs.*;

public abstract class IdentityConstraint implements XSIDCDefinition{
    //
    // Data
    //
    protected short type;
    protected String fNamespace;
    protected String fIdentityConstraintName;
    protected String fElementName;
    protected Selector fSelector;
    protected int fFieldCount;
    protected Field[] fFields;
    // optional annotations
    protected XSAnnotationImpl[] fAnnotations=null;
    // number of annotations in this identity constraint
    protected int fNumAnnotations;
    //
    // Constructors
    //

    protected IdentityConstraint(String namespace,String identityConstraintName,String elemName){
        fNamespace=namespace;
        fIdentityConstraintName=identityConstraintName;
        fElementName=elemName;
    } // <init>(String,String)
    //
    // Public methods
    //

    public String getIdentityConstraintName(){
        return fIdentityConstraintName;
    } // getIdentityConstraintName():String

    public Selector getSelector(){
        return fSelector;
    } // getSelector():Selector

    public void setSelector(Selector selector){
        fSelector=selector;
    } // setSelector(Selector)

    public void addField(Field field){
        if(fFields==null)
            fFields=new Field[4];
        else if(fFieldCount==fFields.length)
            fFields=resize(fFields,fFieldCount*2);
        fFields[fFieldCount++]=field;
    } // addField(Field)

    static final Field[] resize(Field[] oldArray,int newSize){
        Field[] newArray=new Field[newSize];
        System.arraycopy(oldArray,0,newArray,0,oldArray.length);
        return newArray;
    }

    public int getFieldCount(){
        return fFieldCount;
    } // getFieldCount():int

    public Field getFieldAt(int index){
        return fFields[index];
    } // getFieldAt(int):Field
    //
    // Object methods
    //

    // get the name of the owning element
    public String getElementName(){
        return fElementName;
    } // getElementName(): String

    public String toString(){
        String s=super.toString();
        int index1=s.lastIndexOf('$');
        if(index1!=-1){
            return s.substring(index1+1);
        }
        int index2=s.lastIndexOf('.');
        if(index2!=-1){
            return s.substring(index2+1);
        }
        return s;
    } // toString():String

    // equals:  returns true if and only if the String
    // representations of all members of both objects (except for
    // the elenemtName field) are equal.
    public boolean equals(IdentityConstraint id){
        boolean areEqual=fIdentityConstraintName.equals(id.fIdentityConstraintName);
        if(!areEqual) return false;
        areEqual=fSelector.toString().equals(id.fSelector.toString());
        if(!areEqual) return false;
        areEqual=(fFieldCount==id.fFieldCount);
        if(!areEqual) return false;
        for(int i=0;i<fFieldCount;i++)
            if(!fFields[i].toString().equals(id.fFields[i].toString())) return false;
        return true;
    } // equals

    public short getType(){
        return XSConstants.IDENTITY_CONSTRAINT;
    }

    public String getName(){
        return fIdentityConstraintName;
    }

    public String getNamespace(){
        return fNamespace;
    }

    public XSNamespaceItem getNamespaceItem(){
        // REVISIT: implement
        return null;
    }

    public short getCategory(){
        return type;
    }

    public String getSelectorStr(){
        return (fSelector!=null)?fSelector.toString():null;
    }

    public StringList getFieldStrs(){
        String[] strs=new String[fFieldCount];
        for(int i=0;i<fFieldCount;i++)
            strs[i]=fFields[i].toString();
        return new StringListImpl(strs,fFieldCount);
    }

    public XSIDCDefinition getRefKey(){
        return null;
    }

    public XSObjectList getAnnotations(){
        return new XSObjectListImpl(fAnnotations,fNumAnnotations);
    }

    public void addAnnotation(XSAnnotationImpl annotation){
        if(annotation==null)
            return;
        if(fAnnotations==null){
            fAnnotations=new XSAnnotationImpl[2];
        }else if(fNumAnnotations==fAnnotations.length){
            XSAnnotationImpl[] newArray=new XSAnnotationImpl[fNumAnnotations<<1];
            System.arraycopy(fAnnotations,0,newArray,0,fNumAnnotations);
            fAnnotations=newArray;
        }
        fAnnotations[fNumAnnotations++]=annotation;
    }
} // class IdentityConstraint
