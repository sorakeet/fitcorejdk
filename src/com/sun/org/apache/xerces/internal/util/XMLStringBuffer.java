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
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.XMLString;

public class XMLStringBuffer
        extends XMLString{
    //
    // Constants
    //
    public static final int DEFAULT_SIZE=32;
    //
    // Data
    //
    //
    // Constructors
    //

    public XMLStringBuffer(){
        this(DEFAULT_SIZE);
    } // <init>()

    public XMLStringBuffer(int size){
        ch=new char[size];
    } // <init>(int)

    public XMLStringBuffer(char c){
        this(1);
        append(c);
    } // <init>(char)

    public void append(char c){
        if(this.length+1>this.ch.length){
            int newLength=this.ch.length*2;
            if(newLength<this.ch.length+DEFAULT_SIZE){
                newLength=this.ch.length+DEFAULT_SIZE;
            }
            char[] tmp=new char[newLength];
            System.arraycopy(this.ch,0,tmp,0,this.length);
            this.ch=tmp;
        }
        this.ch[this.length]=c;
        this.length++;
    } // append(char)

    public XMLStringBuffer(String s){
        this(s.length());
        append(s);
    } // <init>(String)

    public void append(String s){
        int length=s.length();
        if(this.length+length>this.ch.length){
            int newLength=this.ch.length*2;
            if(newLength<this.ch.length+length+DEFAULT_SIZE){
                newLength=this.ch.length+length+DEFAULT_SIZE;
            }
            char[] newch=new char[newLength];
            System.arraycopy(this.ch,0,newch,0,this.length);
            this.ch=newch;
        }
        s.getChars(0,length,this.ch,this.length);
        this.length+=length;
    } // append(String)
    //
    // Public methods
    //

    public XMLStringBuffer(char[] ch,int offset,int length){
        this(length);
        append(ch,offset,length);
    } // <init>(char[],int,int)

    public void append(char[] ch,int offset,int length){
        if(this.length+length>this.ch.length){
            int newLength=this.ch.length*2;
            if(newLength<this.ch.length+length+DEFAULT_SIZE){
                newLength=this.ch.length+length+DEFAULT_SIZE;
            }
            char[] newch=new char[newLength];
            System.arraycopy(this.ch,0,newch,0,this.length);
            this.ch=newch;
        }
        //making the code more robust as it would handle null or 0 length data,
        //add the data only when it contains some thing
        if(ch!=null&&length>0){
            System.arraycopy(ch,offset,this.ch,this.length,length);
            this.length+=length;
        }
    } // append(char[],int,int)

    public XMLStringBuffer(XMLString s){
        this(s.length);
        append(s);
    } // <init>(XMLString)

    public void append(XMLString s){
        append(s.ch,s.offset,s.length);
    } // append(XMLString)

    public void clear(){
        offset=0;
        length=0;
    }
} // class XMLStringBuffer
