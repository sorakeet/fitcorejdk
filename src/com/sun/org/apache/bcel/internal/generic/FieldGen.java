/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.bcel.internal.generic;
/** ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.*;

import java.util.ArrayList;
import java.util.Iterator;

public class FieldGen extends FieldGenOrMethodGen{
    private Object value=null;
    private ArrayList observers;

    public FieldGen(Field field,ConstantPoolGen cp){
        this(field.getAccessFlags(),Type.getType(field.getSignature()),field.getName(),cp);
        Attribute[] attrs=field.getAttributes();
        for(int i=0;i<attrs.length;i++){
            if(attrs[i] instanceof ConstantValue)
                setValue(((ConstantValue)attrs[i]).getConstantValueIndex());
            else
                addAttribute(attrs[i]);
        }
    }

    public FieldGen(int access_flags,Type type,String name,ConstantPoolGen cp){
        setAccessFlags(access_flags);
        setType(type);
        setName(name);
        setConstantPool(cp);
    }

    private void setValue(int index){
        ConstantPool cp=this.cp.getConstantPool();
        Constant c=cp.getConstant(index);
        value=((ConstantObject)c).getConstantValue(cp);
    }

    public void setInitValue(String str){
        checkType(new ObjectType("java.lang.String"));
        if(str!=null)
            value=str;
    }

    private void checkType(Type atype){
        if(type==null)
            throw new ClassGenException("You haven't defined the type of the field yet");
        if(!isFinal())
            throw new ClassGenException("Only final fields may have an initial value!");
        if(!type.equals(atype))
            throw new ClassGenException("Types are not compatible: "+type+" vs. "+atype);
    }

    public void setInitValue(long l){
        checkType(Type.LONG);
        if(l!=0L)
            value=new Long(l);
    }

    public void setInitValue(int i){
        checkType(Type.INT);
        if(i!=0)
            value=new Integer(i);
    }

    public void setInitValue(short s){
        checkType(Type.SHORT);
        if(s!=0)
            value=new Integer(s);
    }

    public void setInitValue(char c){
        checkType(Type.CHAR);
        if(c!=0)
            value=new Integer(c);
    }

    public void setInitValue(byte b){
        checkType(Type.BYTE);
        if(b!=0)
            value=new Integer(b);
    }

    public void setInitValue(boolean b){
        checkType(Type.BOOLEAN);
        if(b)
            value=new Integer(1);
    }

    public void setInitValue(float f){
        checkType(Type.FLOAT);
        if(f!=0.0)
            value=new Float(f);
    }

    public void cancelInitValue(){
        value=null;
    }

    public Field getField(){
        String signature=getSignature();
        int name_index=cp.addUtf8(name);
        int signature_index=cp.addUtf8(signature);
        if(value!=null){
            checkType(type);
            int index=addConstant();
            addAttribute(new ConstantValue(cp.addUtf8("ConstantValue"),
                    2,index,cp.getConstantPool()));
        }
        return new Field(access_flags,name_index,signature_index,getAttributes(),
                cp.getConstantPool());
    }

    private int addConstant(){
        switch(type.getType()){
            case Constants.T_INT:
            case Constants.T_CHAR:
            case Constants.T_BYTE:
            case Constants.T_BOOLEAN:
            case Constants.T_SHORT:
                return cp.addInteger(((Integer)value).intValue());
            case Constants.T_FLOAT:
                return cp.addFloat(((Float)value).floatValue());
            case Constants.T_DOUBLE:
                return cp.addDouble(((Double)value).doubleValue());
            case Constants.T_LONG:
                return cp.addLong(((Long)value).longValue());
            case Constants.T_REFERENCE:
                return cp.addString(((String)value));
            default:
                throw new RuntimeException("Oops: Unhandled : "+type.getType());
        }
    }

    public String getSignature(){
        return type.getSignature();
    }

    public void addObserver(FieldObserver o){
        if(observers==null)
            observers=new ArrayList();
        observers.add(o);
    }

    public void removeObserver(FieldObserver o){
        if(observers!=null)
            observers.remove(o);
    }

    public void update(){
        if(observers!=null)
            for(Iterator e=observers.iterator();e.hasNext();)
                ((FieldObserver)e.next()).notify(this);
    }

    public final String toString(){
        String name, signature, access; // Short cuts to constant pool
        access=Utility.accessToString(access_flags);
        access=access.equals("")?"":(access+" ");
        signature=type.toString();
        name=getName();
        StringBuffer buf=new StringBuffer(access+signature+" "+name);
        String value=getInitValue();
        if(value!=null)
            buf.append(" = "+value);
        return buf.toString();
    }

    public String getInitValue(){
        if(value!=null){
            return value.toString();
        }else
            return null;
    }

    public void setInitValue(double d){
        checkType(Type.DOUBLE);
        if(d!=0.0)
            value=new Double(d);
    }

    public FieldGen copy(ConstantPoolGen cp){
        FieldGen fg=(FieldGen)clone();
        fg.setConstantPool(cp);
        return fg;
    }
}
