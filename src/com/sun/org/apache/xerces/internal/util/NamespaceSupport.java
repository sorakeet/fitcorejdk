/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * The Apache Software License, Version 1.1
 * <p>
 * <p>
 * Copyright (c) 2000-2002 The Apache Software Foundation.
 * All rights reserved.
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
 * Copyright (c) 2000-2002 The Apache Software Foundation.
 * All rights reserved.
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

import com.sun.org.apache.xerces.internal.xni.NamespaceContext;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class NamespaceSupport implements NamespaceContext{
    //
    // Data
    //
    protected String[] fNamespace=new String[16*2];
    protected int fNamespaceSize;
    // NOTE: The constructor depends on the initial context size
    //       being at least 1. -Ac
    protected int[] fContext=new int[8];
    protected int fCurrentContext;
    protected String[] fPrefixes=new String[16];
    //
    // Constructors
    //

    public NamespaceSupport(){
    } // <init>()

    public NamespaceSupport(NamespaceContext context){
        pushContext();
        // copy declaration in the context
        Enumeration prefixes=context.getAllPrefixes();
        while(prefixes.hasMoreElements()){
            String prefix=(String)prefixes.nextElement();
            String uri=context.getURI(prefix);
            declarePrefix(prefix,uri);
        }
    } // <init>(NamespaceContext)
    //
    // Public methods
    //

    public void pushContext(){
        // extend the array, if necessary
        if(fCurrentContext+1==fContext.length){
            int[] contextarray=new int[fContext.length*2];
            System.arraycopy(fContext,0,contextarray,0,fContext.length);
            fContext=contextarray;
        }
        // push context
        fContext[++fCurrentContext]=fNamespaceSize;
        //System.out.println("calling push context, current context = " + fCurrentContext);
    } // pushContext()

    public void popContext(){
        fNamespaceSize=fContext[fCurrentContext--];
        //System.out.println("Calling popContext, fCurrentContext = " + fCurrentContext);
    } // popContext()

    public boolean declarePrefix(String prefix,String uri){
        // ignore "xml" and "xmlns" prefixes
        if(prefix==XMLSymbols.PREFIX_XML||prefix==XMLSymbols.PREFIX_XMLNS){
            return false;
        }
        // see if prefix already exists in current context
        for(int i=fNamespaceSize;i>fContext[fCurrentContext];i-=2){
            if(fNamespace[i-2]==prefix){
                // REVISIT: [Q] Should the new binding override the
                //          previously declared binding or should it
                //          it be ignored? -Ac
                // NOTE:    The SAX2 "NamespaceSupport" helper allows
                //          re-bindings with the new binding overwriting
                //          the previous binding. -Ac
                fNamespace[i-1]=uri;
                return true;
            }
        }
        // resize array, if needed
        if(fNamespaceSize==fNamespace.length){
            String[] namespacearray=new String[fNamespaceSize*2];
            System.arraycopy(fNamespace,0,namespacearray,0,fNamespaceSize);
            fNamespace=namespacearray;
        }
        // bind prefix to uri in current context
        fNamespace[fNamespaceSize++]=prefix;
        fNamespace[fNamespaceSize++]=uri;
        return true;
    } // declarePrefix(String,String):boolean

    public String getURI(String prefix){
        // find prefix in current context
        for(int i=fNamespaceSize;i>0;i-=2){
            if(fNamespace[i-2]==prefix){
                return fNamespace[i-1];
            }
        }
        // prefix not found
        return null;
    } // getURI(String):String

    public String getPrefix(String uri){
        // find uri in current context
        for(int i=fNamespaceSize;i>0;i-=2){
            if(fNamespace[i-1]==uri){
                if(getURI(fNamespace[i-2])==uri)
                    return fNamespace[i-2];
            }
        }
        // uri not found
        return null;
    } // getPrefix(String):String

    public int getDeclaredPrefixCount(){
        return (fNamespaceSize-fContext[fCurrentContext])/2;
    } // getDeclaredPrefixCount():int

    public String getDeclaredPrefixAt(int index){
        return fNamespace[fContext[fCurrentContext]+index*2];
    } // getDeclaredPrefixAt(int):String

    public Enumeration getAllPrefixes(){
        int count=0;
        if(fPrefixes.length<(fNamespace.length/2)){
            // resize prefix array
            String[] prefixes=new String[fNamespaceSize];
            fPrefixes=prefixes;
        }
        String prefix=null;
        boolean unique=true;
        for(int i=2;i<(fNamespaceSize-2);i+=2){
            prefix=fNamespace[i+2];
            for(int k=0;k<count;k++){
                if(fPrefixes[k]==prefix){
                    unique=false;
                    break;
                }
            }
            if(unique){
                fPrefixes[count++]=prefix;
            }
            unique=true;
        }
        return new Prefixes(fPrefixes,count);
    }

    public void reset(){
        // reset namespace and context info
        fNamespaceSize=0;
        fCurrentContext=0;
        // bind "xml" prefix to the XML uri
        fNamespace[fNamespaceSize++]=XMLSymbols.PREFIX_XML;
        fNamespace[fNamespaceSize++]=NamespaceContext.XML_URI;
        // bind "xmlns" prefix to the XMLNS uri
        fNamespace[fNamespaceSize++]=XMLSymbols.PREFIX_XMLNS;
        fNamespace[fNamespaceSize++]=NamespaceContext.XMLNS_URI;
        fContext[fCurrentContext]=fNamespaceSize;
        //++fCurrentContext;
    } // reset(SymbolTable)

    public Iterator getPrefixes(){
        int count=0;
        if(fPrefixes.length<(fNamespace.length/2)){
            // resize prefix array
            String[] prefixes=new String[fNamespaceSize];
            fPrefixes=prefixes;
        }
        String prefix=null;
        boolean unique=true;
        for(int i=2;i<(fNamespaceSize-2);i+=2){
            prefix=fNamespace[i+2];
            for(int k=0;k<count;k++){
                if(fPrefixes[k]==prefix){
                    unique=false;
                    break;
                }
            }
            if(unique){
                fPrefixes[count++]=prefix;
            }
            unique=true;
        }
        return new IteratorPrefixes(fPrefixes,count);
    }//getPrefixes

    public Vector getPrefixes(String uri){
        int count=0;
        String prefix=null;
        boolean unique=true;
        Vector prefixList=new Vector();
        for(int i=fNamespaceSize;i>0;i-=2){
            if(fNamespace[i-1]==uri){
                if(!prefixList.contains(fNamespace[i-2]))
                    prefixList.add(fNamespace[i-2]);
            }
        }
        return prefixList;
    }

    public boolean containsPrefix(String prefix){
        // find prefix in context
        for(int i=fNamespaceSize;i>0;i-=2){
            if(fNamespace[i-2]==prefix){
                return true;
            }
        }
        // prefix not found
        return false;
    }

    public boolean containsPrefixInCurrentContext(String prefix){
        // find prefix in current context
        for(int i=fContext[fCurrentContext];i<fNamespaceSize;i+=2){
            if(fNamespace[i]==prefix){
                return true;
            }
        }
        // prefix not found
        return false;
    }

    protected final class IteratorPrefixes implements Iterator{
        private String[] prefixes;
        private int counter=0;
        private int size=0;

        public IteratorPrefixes(String[] prefixes,int size){
            this.prefixes=prefixes;
            this.size=size;
        }

        public boolean hasNext(){
            return (counter<size);
        }

        public Object next(){
            if(counter<size){
                return fPrefixes[counter++];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }

        public String toString(){
            StringBuffer buf=new StringBuffer();
            for(int i=0;i<size;i++){
                buf.append(prefixes[i]);
                buf.append(" ");
            }
            return buf.toString();
        }
    }

    protected final class Prefixes implements Enumeration{
        private String[] prefixes;
        private int counter=0;
        private int size=0;

        public Prefixes(String[] prefixes,int size){
            this.prefixes=prefixes;
            this.size=size;
        }

        public boolean hasMoreElements(){
            return (counter<size);
        }

        public Object nextElement(){
            if(counter<size){
                return fPrefixes[counter++];
            }
            throw new NoSuchElementException("Illegal access to Namespace prefixes enumeration.");
        }

        public String toString(){
            StringBuffer buf=new StringBuffer();
            for(int i=0;i<size;i++){
                buf.append(prefixes[i]);
                buf.append(" ");
            }
            return buf.toString();
        }
    }
} // class NamespaceSupport
