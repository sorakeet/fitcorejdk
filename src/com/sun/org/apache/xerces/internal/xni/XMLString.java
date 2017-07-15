/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xni;

public class XMLString{
    //
    // Data
    //
    public char[] ch;
    public int offset;
    public int length;
    //
    // Constructors
    //

    public XMLString(){
    } // <init>()

    public XMLString(char[] ch,int offset,int length){
        setValues(ch,offset,length);
    } // <init>(char[],int,int)

    public void setValues(char[] ch,int offset,int length){
        this.ch=ch;
        this.offset=offset;
        this.length=length;
    } // setValues(char[],int,int)
    //
    // Public methods
    //

    public XMLString(XMLString string){
        setValues(string);
    } // <init>(XMLString)

    public void setValues(XMLString s){
        setValues(s.ch,s.offset,s.length);
    } // setValues(XMLString)

    public void clear(){
        this.ch=null;
        this.offset=0;
        this.length=-1;
    } // clear()

    public boolean equals(char[] ch,int offset,int length){
        if(ch==null){
            return false;
        }
        if(this.length!=length){
            return false;
        }
        for(int i=0;i<length;i++){
            if(this.ch[this.offset+i]!=ch[offset+i]){
                return false;
            }
        }
        return true;
    } // equals(char[],int,int):boolean

    public boolean equals(String s){
        if(s==null){
            return false;
        }
        if(length!=s.length()){
            return false;
        }
        // is this faster than call s.toCharArray first and compare the
        // two arrays directly, which will possibly involve creating a
        // new char array object.
        for(int i=0;i<length;i++){
            if(ch[offset+i]!=s.charAt(i)){
                return false;
            }
        }
        return true;
    } // equals(String):boolean
    //
    // Object methods
    //

    public String toString(){
        return length>0?new String(ch,offset,length):"";
    } // toString():String
} // class XMLString
