/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: Arg.java,v 1.1.2.1 2005/08/01 01:30:11 jeffsuttor Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: Arg.java,v 1.1.2.1 2005/08/01 01:30:11 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xml.internal.utils.QName;
import com.sun.org.apache.xpath.internal.objects.XObject;

import java.util.Objects;

public class Arg{
    private QName m_qname;
    private XObject m_val;
    private String m_expression;
    private boolean m_isFromWithParam;
    private boolean m_isVisible;

    public Arg(){
        m_qname=new QName("");
        // so that string compares can be done.
        m_val=null;
        m_expression=null;
        m_isVisible=true;
        m_isFromWithParam=false;
    }

    public Arg(QName qname,String expression,boolean isFromWithParam){
        m_qname=qname;
        m_val=null;
        m_expression=expression;
        m_isFromWithParam=isFromWithParam;
        m_isVisible=!isFromWithParam;
    }

    public Arg(QName qname,XObject val){
        m_qname=qname;
        m_val=val;
        m_isVisible=true;
        m_isFromWithParam=false;
        m_expression=null;
    }

    public Arg(QName qname,XObject val,boolean isFromWithParam){
        m_qname=qname;
        m_val=val;
        m_isFromWithParam=isFromWithParam;
        m_isVisible=!isFromWithParam;
        m_expression=null;
    }

    public final QName getQName(){
        return m_qname;
    }

    public final void setQName(QName name){
        m_qname=name;
    }

    public final XObject getVal(){
        return m_val;
    }

    public final void setVal(XObject val){
        m_val=val;
    }

    public void detach(){
        if(null!=m_val){
            m_val.allowDetachToRelease(true);
            m_val.detach();
        }
    }

    public String getExpression(){
        return m_expression;
    }

    public void setExpression(String expr){
        m_expression=expr;
    }

    public boolean isFromWithParam(){
        return m_isFromWithParam;
    }

    public boolean isVisible(){
        return m_isVisible;
    }

    public void setIsVisible(boolean b){
        m_isVisible=b;
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(this.m_qname);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof QName){
            return m_qname.equals(obj);
        }else
            return super.equals(obj);
    }
}
