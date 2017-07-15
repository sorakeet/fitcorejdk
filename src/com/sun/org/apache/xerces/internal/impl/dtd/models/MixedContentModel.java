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

public class MixedContentModel
        implements ContentModelValidator{
    //
    // Data
    //
    private int fCount;
    private QName fChildren[];
    private int fChildrenType[];
    //private EquivClassComparator comparator = null;
    private boolean fOrdered;
    //
    // Constructors
    //

    public MixedContentModel(QName[] children,int[] type,int offset,int length,boolean ordered){
        // Make our own copy now, which is exactly the right size
        fCount=length;
        fChildren=new QName[fCount];
        fChildrenType=new int[fCount];
        for(int i=0;i<fCount;i++){
            fChildren[i]=new QName(children[offset+i]);
            fChildrenType[i]=type[offset+i];
        }
        fOrdered=ordered;
    }
    //
    // ContentModelValidator methods
    //

    public int validate(QName[] children,int offset,int length){
        // must match order
        if(fOrdered){
            int inIndex=0;
            for(int outIndex=0;outIndex<length;outIndex++){
                // ignore mixed text
                final QName curChild=children[offset+outIndex];
                if(curChild.localpart==null){
                    continue;
                }
                // element must match
                int type=fChildrenType[inIndex];
                if(type==XMLContentSpec.CONTENTSPECNODE_LEAF){
                    if(fChildren[inIndex].rawname!=children[offset+outIndex].rawname){
                        return outIndex;
                    }
                }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY){
                    String uri=fChildren[inIndex].uri;
                    if(uri!=null&&uri!=children[outIndex].uri){
                        return outIndex;
                    }
                }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY_LOCAL){
                    if(children[outIndex].uri!=null){
                        return outIndex;
                    }
                }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY_OTHER){
                    if(fChildren[inIndex].uri==children[outIndex].uri){
                        return outIndex;
                    }
                }
                // advance index
                inIndex++;
            }
        }
        // can appear in any order
        else{
            for(int outIndex=0;outIndex<length;outIndex++){
                // Get the current child out of the source index
                final QName curChild=children[offset+outIndex];
                // If its PCDATA, then we just accept that
                if(curChild.localpart==null)
                    continue;
                // And try to find it in our list
                int inIndex=0;
                for(;inIndex<fCount;inIndex++){
                    int type=fChildrenType[inIndex];
                    if(type==XMLContentSpec.CONTENTSPECNODE_LEAF){
                        if(curChild.rawname==fChildren[inIndex].rawname){
                            break;
                        }
                    }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY){
                        String uri=fChildren[inIndex].uri;
                        if(uri==null||uri==children[outIndex].uri){
                            break;
                        }
                    }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY_LOCAL){
                        if(children[outIndex].uri==null){
                            break;
                        }
                    }else if(type==XMLContentSpec.CONTENTSPECNODE_ANY_OTHER){
                        if(fChildren[inIndex].uri!=children[outIndex].uri){
                            break;
                        }
                    }
                    // REVISIT: What about checking for multiple ANY matches?
                    //          The content model ambiguity *could* be checked
                    //          by the caller before constructing the mixed
                    //          content model.
                }
                // We did not find this one, so the validation failed
                if(inIndex==fCount)
                    return outIndex;
            }
        }
        // Everything seems to be in order, so return success
        return -1;
    } // validate
} // class MixedContentModel
