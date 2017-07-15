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

public class XMLContentSpec{
    //
    // Constants
    //
    public static final short CONTENTSPECNODE_LEAF=0;
    public static final short CONTENTSPECNODE_ZERO_OR_ONE=1;
    public static final short CONTENTSPECNODE_ZERO_OR_MORE=2;
    public static final short CONTENTSPECNODE_ONE_OR_MORE=3;
    public static final short CONTENTSPECNODE_CHOICE=4;
    public static final short CONTENTSPECNODE_SEQ=5;
    public static final short CONTENTSPECNODE_ANY=6;
    public static final short CONTENTSPECNODE_ANY_OTHER=7;
    public static final short CONTENTSPECNODE_ANY_LOCAL=8;
    public static final short CONTENTSPECNODE_ANY_LAX=22;
    public static final short CONTENTSPECNODE_ANY_OTHER_LAX=23;
    public static final short CONTENTSPECNODE_ANY_LOCAL_LAX=24;
    public static final short CONTENTSPECNODE_ANY_SKIP=38;
    public static final short CONTENTSPECNODE_ANY_OTHER_SKIP=39;
    public static final short CONTENTSPECNODE_ANY_LOCAL_SKIP=40;
    //
    // Data
    //
    public short type;
    public Object value;
    public Object otherValue;
    //
    // Constructors
    //

    public XMLContentSpec(){
        clear();
    }

    public void clear(){
        type=-1;
        value=null;
        otherValue=null;
    }

    public XMLContentSpec(short type,Object value,Object otherValue){
        setValues(type,value,otherValue);
    }

    public void setValues(short type,Object value,Object otherValue){
        this.type=type;
        this.value=value;
        this.otherValue=otherValue;
    }
    //
    // Public methods
    //

    public XMLContentSpec(XMLContentSpec contentSpec){
        setValues(contentSpec);
    }

    public void setValues(XMLContentSpec contentSpec){
        type=contentSpec.type;
        value=contentSpec.value;
        otherValue=contentSpec.otherValue;
    }

    public XMLContentSpec(Provider provider,
                          int contentSpecIndex){
        setValues(provider,contentSpecIndex);
    }

    public void setValues(Provider provider,
                          int contentSpecIndex){
        if(!provider.getContentSpec(contentSpecIndex,this)){
            clear();
        }
    }
    //
    // Object methods
    //

    public int hashCode(){
        return type<<16|
                value.hashCode()<<8|
                otherValue.hashCode();
    }

    public boolean equals(Object object){
        if(object!=null&&object instanceof XMLContentSpec){
            XMLContentSpec contentSpec=(XMLContentSpec)object;
            return type==contentSpec.type&&
                    value==contentSpec.value&&
                    otherValue==contentSpec.otherValue;
        }
        return false;
    }
    //
    // Interfaces
    //

    public interface Provider{
        //
        // XMLContentSpec.Provider methods
        //

        public boolean getContentSpec(int contentSpecIndex,XMLContentSpec contentSpec);
    } // interface Provider
} // class XMLContentSpec
