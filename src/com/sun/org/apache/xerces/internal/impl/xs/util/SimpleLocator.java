/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs.util;

import com.sun.org.apache.xerces.internal.xni.XMLLocator;

public class SimpleLocator implements XMLLocator{
    String lsid, esid;
    int line, column;
    int charOffset;

    public SimpleLocator(){
    }

    public SimpleLocator(String lsid,String esid,int line,int column){
        this(lsid,esid,line,column,-1);
    }

    public SimpleLocator(String lsid,String esid,int line,int column,int offset){
        this.line=line;
        this.column=column;
        this.lsid=lsid;
        this.esid=esid;
        charOffset=offset;
    }

    public void setValues(String lsid,String esid,int line,int column){
        setValues(lsid,esid,line,column,-1);
    }

    public void setValues(String lsid,String esid,int line,int column,int offset){
        this.line=line;
        this.column=column;
        this.lsid=lsid;
        this.esid=esid;
        charOffset=offset;
    }

    public String getPublicId(){
        return null;
    }

    public String getLiteralSystemId(){
        return lsid;
    }

    public String getBaseSystemId(){
        return null;
    }

    public String getExpandedSystemId(){
        return esid;
    }

    public int getLineNumber(){
        return line;
    }

    public int getColumnNumber(){
        return column;
    }

    public int getCharacterOffset(){
        return charOffset;
    }

    public void setCharacterOffset(int offset){
        charOffset=offset;
    }

    public String getEncoding(){
        return null;
    }

    public String getXMLVersion(){
        return null;
    }

    public void setColumnNumber(int col){
        this.column=col;
    }

    public void setLineNumber(int line){
        this.line=line;
    }

    public void setExpandedSystemId(String systemId){
        esid=systemId;
    }

    public void setBaseSystemId(String systemId){
    }

    public void setLiteralSystemId(String systemId){
        lsid=systemId;
    }

    public void setPublicId(String publicId){
    }
}
