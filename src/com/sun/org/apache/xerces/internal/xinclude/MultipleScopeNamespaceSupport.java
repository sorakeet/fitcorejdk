/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2003-2005 The Apache Software Foundation.
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
 * Copyright 2003-2005 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.xinclude;

import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;

import java.util.Enumeration;

public class MultipleScopeNamespaceSupport extends NamespaceSupport{
    protected int[] fScope=new int[8];
    protected int fCurrentScope;

    public MultipleScopeNamespaceSupport(){
        super();
        fCurrentScope=0;
        fScope[0]=0;
    }

    public MultipleScopeNamespaceSupport(NamespaceContext context){
        super(context);
        fCurrentScope=0;
        fScope[0]=0;
    }

    public String getPrefix(String uri,int context){
        return getPrefix(uri,fContext[context+1],fContext[fScope[getScopeForContext(context)]]);
    }

    public int getScopeForContext(int context){
        int scope=fCurrentScope;
        while(context<fScope[scope]){
            scope--;
        }
        return scope;
    }

    public String getURI(String prefix,int context){
        return getURI(prefix,fContext[context+1],fContext[fScope[getScopeForContext(context)]]);
    }

    public void reset(){
        fCurrentContext=fScope[fCurrentScope];
        fNamespaceSize=fContext[fCurrentContext];
    }

    public String getURI(String prefix){
        return getURI(prefix,fNamespaceSize,fContext[fScope[fCurrentScope]]);
    }

    public String getPrefix(String uri){
        return getPrefix(uri,fNamespaceSize,fContext[fScope[fCurrentScope]]);
    }

    public Enumeration getAllPrefixes(){
        int count=0;
        if(fPrefixes.length<(fNamespace.length/2)){
            // resize prefix array
            String[] prefixes=new String[fNamespaceSize];
            fPrefixes=prefixes;
        }
        String prefix=null;
        boolean unique=true;
        for(int i=fContext[fScope[fCurrentScope]];
            i<=(fNamespaceSize-2);
            i+=2){
            prefix=fNamespace[i];
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

    public String getPrefix(String uri,int start,int end){
        // this saves us from having a copy of each of these in fNamespace for each scope
        if(uri==NamespaceContext.XML_URI){
            return XMLSymbols.PREFIX_XML;
        }
        if(uri==NamespaceContext.XMLNS_URI){
            return XMLSymbols.PREFIX_XMLNS;
        }
        // find uri in current context
        for(int i=start;i>end;i-=2){
            if(fNamespace[i-1]==uri){
                if(getURI(fNamespace[i-2])==uri)
                    return fNamespace[i-2];
            }
        }
        // uri not found
        return null;
    }

    public String getURI(String prefix,int start,int end){
        // this saves us from having a copy of each of these in fNamespace for each scope
        if(prefix==XMLSymbols.PREFIX_XML){
            return NamespaceContext.XML_URI;
        }
        if(prefix==XMLSymbols.PREFIX_XMLNS){
            return NamespaceContext.XMLNS_URI;
        }
        // find prefix in current context
        for(int i=start;i>end;i-=2){
            if(fNamespace[i-2]==prefix){
                return fNamespace[i-1];
            }
        }
        // prefix not found
        return null;
    }

    public void pushScope(){
        if(fCurrentScope+1==fScope.length){
            int[] contextarray=new int[fScope.length*2];
            System.arraycopy(fScope,0,contextarray,0,fScope.length);
            fScope=contextarray;
        }
        pushContext();
        fScope[++fCurrentScope]=fCurrentContext;
    }

    public void popScope(){
        fCurrentContext=fScope[fCurrentScope--];
        popContext();
    }
}
