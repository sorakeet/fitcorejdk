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
package com.sun.org.apache.xerces.internal.impl.dtd.models;

import com.sun.org.apache.xerces.internal.impl.dtd.XMLContentSpec;
import com.sun.org.apache.xerces.internal.xni.QName;

public class SimpleContentModel
        implements ContentModelValidator{
    //
    // Constants
    //
    public static final short CHOICE=-1;
    public static final short SEQUENCE=-1;
    //
    // Data
    //
    private QName fFirstChild=new QName();
    private QName fSecondChild=new QName();
    private int fOperator;
    //private EquivClassComparator comparator = null;
    //
    // Constructors
    //

    public SimpleContentModel(short operator,QName firstChild,QName secondChild){
        //
        //  Store away the children and operation. This is all we need to
        //  do the content model check.
        //
        //  The operation is one of the ContentSpecNode.NODE_XXX values!
        //
        fFirstChild.setValues(firstChild);
        if(secondChild!=null){
            fSecondChild.setValues(secondChild);
        }else{
            fSecondChild.clear();
        }
        fOperator=operator;
    }
    //
    // ContentModelValidator methods
    //

    public int validate(QName[] children,int offset,int length){
        //
        //  According to the type of operation, we do the correct type of
        //  content check.
        //
        switch(fOperator){
            case XMLContentSpec.CONTENTSPECNODE_LEAF:
                // If there is not a child, then report an error at index 0
                if(length==0)
                    return 0;
                // If the 0th child is not the right kind, report an error at 0
                if(children[offset].rawname!=fFirstChild.rawname){
                    return 0;
                }
                // If more than one child, report an error at index 1
                if(length>1)
                    return 1;
                break;
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE:
                //
                //  If there is one child, make sure its the right type. If not,
                //  then its an error at index 0.
                //
                if(length==1){
                    if(children[offset].rawname!=fFirstChild.rawname){
                        return 0;
                    }
                }
                //
                //  If the child count is greater than one, then obviously
                //  bad, so report an error at index 1.
                //
                if(length>1)
                    return 1;
                break;
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE:
                //
                //  If the child count is zero, that's fine. If its more than
                //  zero, then make sure that all children are of the element
                //  type that we stored. If not, report the index of the first
                //  failed one.
                //
                if(length>0){
                    for(int index=0;index<length;index++){
                        if(children[offset+index].rawname!=fFirstChild.rawname){
                            return index;
                        }
                    }
                }
                break;
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE:
                //
                //  If the child count is zero, that's an error so report
                //  an error at index 0.
                //
                if(length==0)
                    return 0;
                //
                //  Otherwise we have to check them all to make sure that they
                //  are of the correct child type. If not, then report the index
                //  of the first one that is not.
                //
                for(int index=0;index<length;index++){
                    if(children[offset+index].rawname!=fFirstChild.rawname){
                        return index;
                    }
                }
                break;
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
                //
                //  There must be one and only one child, so if the element count
                //  is zero, return an error at index 0.
                //
                if(length==0)
                    return 0;
                // If the zeroth element isn't one of our choices, error at 0
                if((children[offset].rawname!=fFirstChild.rawname)&&
                        (children[offset].rawname!=fSecondChild.rawname)){
                    return 0;
                }
                // If there is more than one element, then an error at 1
                if(length>1)
                    return 1;
                break;
            case XMLContentSpec.CONTENTSPECNODE_SEQ:
                //
                //  There must be two children and they must be the two values
                //  we stored, in the stored order.
                //
                if(length==2){
                    if(children[offset].rawname!=fFirstChild.rawname){
                        return 0;
                    }
                    if(children[offset+1].rawname!=fSecondChild.rawname){
                        return 1;
                    }
                }else{
                    if(length>2){
                        return 2;
                    }
                    return length;
                }
                break;
            default:
                throw new RuntimeException("ImplementationMessages.VAL_CST");
        }
        // We survived, so return success status
        return -1;
    } // validate
} // class SimpleContentModel
