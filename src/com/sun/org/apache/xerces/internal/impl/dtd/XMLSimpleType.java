/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * The Apache Software License, Version 1.1
 * <p>
 * <p>
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * <p>
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 * <p>
 * 4. The names "Xerces" and "Apache Software Foundation" must
 * not be used to endorse or promote products derived from this
 * software without prior written permission. For written
 * permission, please contact apache@apache.org.
 * <p>
 * 5. Products derived from this software may not be called "Apache",
 * nor may "Apache" appear in their name, without prior written
 * permission of the Apache Software Foundation.
 * <p>
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
 * <p>
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
/**
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;

public class XMLSimpleType{
    //
    // Constants
    //
    public static final short TYPE_CDATA=0;
    public static final short TYPE_ENTITY=1;
    public static final short TYPE_ENUMERATION=2;
    public static final short TYPE_ID=3;
    public static final short TYPE_IDREF=4;
    public static final short TYPE_NMTOKEN=5;
    public static final short TYPE_NOTATION=6;
    public static final short TYPE_NAMED=7;
    public static final short DEFAULT_TYPE_DEFAULT=3;
    public static final short DEFAULT_TYPE_FIXED=1;
    public static final short DEFAULT_TYPE_IMPLIED=0;
    public static final short DEFAULT_TYPE_REQUIRED=2;
    //
    // Data
    //
    public short type;
    public String name;
    public String[] enumeration;
    public boolean list;
    public short defaultType;
    public String defaultValue;
    public String nonNormalizedDefaultValue;
    public DatatypeValidator datatypeValidator;
    //
    // Methods
    //

    public void setValues(short type,String name,String[] enumeration,
                          boolean list,short defaultType,
                          String defaultValue,String nonNormalizedDefaultValue,
                          DatatypeValidator datatypeValidator){
        this.type=type;
        this.name=name;
        // REVISIT: Should this be a copy? -Ac
        if(enumeration!=null&&enumeration.length>0){
            this.enumeration=new String[enumeration.length];
            System.arraycopy(enumeration,0,this.enumeration,0,this.enumeration.length);
        }else{
            this.enumeration=null;
        }
        this.list=list;
        this.defaultType=defaultType;
        this.defaultValue=defaultValue;
        this.nonNormalizedDefaultValue=nonNormalizedDefaultValue;
        this.datatypeValidator=datatypeValidator;
    } // setValues(short,String,String[],boolean,short,String,String,DatatypeValidator)

    public void setValues(XMLSimpleType simpleType){
        type=simpleType.type;
        name=simpleType.name;
        // REVISIT: Should this be a copy? -Ac
        if(simpleType.enumeration!=null&&simpleType.enumeration.length>0){
            enumeration=new String[simpleType.enumeration.length];
            System.arraycopy(simpleType.enumeration,0,enumeration,0,enumeration.length);
        }else{
            enumeration=null;
        }
        list=simpleType.list;
        defaultType=simpleType.defaultType;
        defaultValue=simpleType.defaultValue;
        nonNormalizedDefaultValue=simpleType.nonNormalizedDefaultValue;
        datatypeValidator=simpleType.datatypeValidator;
    } // setValues(XMLSimpleType)

    public void clear(){
        this.type=-1;
        this.name=null;
        this.enumeration=null;
        this.list=false;
        this.defaultType=-1;
        this.defaultValue=null;
        this.nonNormalizedDefaultValue=null;
        this.datatypeValidator=null;
    } // clear
} // class XMLSimpleType
