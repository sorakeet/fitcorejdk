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
 * $Id: ExtendedType.java,v 1.2.4.1 2005/09/15 08:15:06 suresh_emailid Exp $
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
 * $Id: ExtendedType.java,v 1.2.4.1 2005/09/15 08:15:06 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm.ref;

public final class ExtendedType{
    private int nodetype;
    private String namespace;
    private String localName;
    private int hash;

    public ExtendedType(int nodetype,String namespace,String localName){
        this.nodetype=nodetype;
        this.namespace=namespace;
        this.localName=localName;
        this.hash=nodetype+namespace.hashCode()+localName.hashCode();
    }

    public ExtendedType(int nodetype,String namespace,String localName,int hash){
        this.nodetype=nodetype;
        this.namespace=namespace;
        this.localName=localName;
        this.hash=hash;
    }

    protected void redefine(int nodetype,String namespace,String localName){
        this.nodetype=nodetype;
        this.namespace=namespace;
        this.localName=localName;
        this.hash=nodetype+namespace.hashCode()+localName.hashCode();
    }

    protected void redefine(int nodetype,String namespace,String localName,int hash){
        this.nodetype=nodetype;
        this.namespace=namespace;
        this.localName=localName;
        this.hash=hash;
    }

    public int hashCode(){
        return hash;
    }

    public boolean equals(ExtendedType other){
        try{
            return other.nodetype==this.nodetype&&
                    other.localName.equals(this.localName)&&
                    other.namespace.equals(this.namespace);
        }catch(NullPointerException e){
            return false;
        }
    }

    public int getNodeType(){
        return nodetype;
    }

    public String getLocalName(){
        return localName;
    }

    public String getNamespace(){
        return namespace;
    }
}
