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
 * $Id: VariableStack.java,v 1.2.4.1 2005/09/10 18:16:22 jeffsuttor Exp $
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
 * $Id: VariableStack.java,v 1.2.4.1 2005/09/10 18:16:22 jeffsuttor Exp $
 */
package com.sun.org.apache.xpath.internal;

import com.sun.org.apache.xalan.internal.res.XSLMessages;
import com.sun.org.apache.xpath.internal.objects.XObject;
import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;

import javax.xml.transform.TransformerException;

public class VariableStack implements Cloneable{
    public static final int CLEARLIMITATION=1024;
    private static XObject[] m_nulls=new XObject[CLEARLIMITATION];
    XObject[] _stackFrames=new XObject[XPathContext.RECURSIONLIMIT*2];
    int _frameTop;
    int[] _links=new int[XPathContext.RECURSIONLIMIT];
    int _linksTop;
    private int _currentFrameBottom;
    public VariableStack(){
        reset();
    }

    public void reset(){
        _frameTop=0;
        _linksTop=0;
        // Adding one here to the stack of frame positions will allow us always
        // to look one under without having to check if we're at zero.
        // (As long as the caller doesn't screw up link/unlink.)
        _links[_linksTop++]=0;
        _stackFrames=new XObject[_stackFrames.length];
    }

    public synchronized Object clone() throws CloneNotSupportedException{
        VariableStack vs=(VariableStack)super.clone();
        // I *think* I can get away with a shallow clone here?
        vs._stackFrames=(XObject[])_stackFrames.clone();
        vs._links=(int[])_links.clone();
        return vs;
    }

    public XObject elementAt(final int i){
        return _stackFrames[i];
    }

    public int size(){
        return _frameTop;
    }

    public int getStackFrame(){
        return _currentFrameBottom;
    }

    public void setStackFrame(int sf){
        _currentFrameBottom=sf;
    }

    public int link(final int size){
        _currentFrameBottom=_frameTop;
        _frameTop+=size;
        if(_frameTop>=_stackFrames.length){
            XObject newsf[]=new XObject[_stackFrames.length+XPathContext.RECURSIONLIMIT+size];
            System.arraycopy(_stackFrames,0,newsf,0,_stackFrames.length);
            _stackFrames=newsf;
        }
        if(_linksTop+1>=_links.length){
            int newlinks[]=new int[_links.length+(CLEARLIMITATION*2)];
            System.arraycopy(_links,0,newlinks,0,_links.length);
            _links=newlinks;
        }
        _links[_linksTop++]=_currentFrameBottom;
        return _currentFrameBottom;
    }

    public void unlink(){
        _frameTop=_links[--_linksTop];
        _currentFrameBottom=_links[_linksTop-1];
    }

    public void unlink(int currentFrame){
        _frameTop=_links[--_linksTop];
        _currentFrameBottom=currentFrame;
    }

    public void setLocalVariable(int index,XObject val){
        _stackFrames[index+_currentFrameBottom]=val;
    }

    public void setLocalVariable(int index,XObject val,int stackFrame){
        _stackFrames[index+stackFrame]=val;
    }

    public XObject getLocalVariable(XPathContext xctxt,int index)
            throws TransformerException{
        index+=_currentFrameBottom;
        XObject val=_stackFrames[index];
        if(null==val)
            throw new TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_VARIABLE_ACCESSED_BEFORE_BIND,null),
                    xctxt.getSAXLocator());
        // "Variable accessed before it is bound!", xctxt.getSAXLocator());
        // Lazy execution of variables.
        if(val.getType()==XObject.CLASS_UNRESOLVEDVARIABLE)
            return (_stackFrames[index]=val.execute(xctxt));
        return val;
    }

    public XObject getLocalVariable(int index,int frame)
            throws TransformerException{
        index+=frame;
        XObject val=_stackFrames[index];
        return val;
    }

    public XObject getLocalVariable(XPathContext xctxt,int index,boolean destructiveOK)
            throws TransformerException{
        index+=_currentFrameBottom;
        XObject val=_stackFrames[index];
        if(null==val)
            throw new TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_VARIABLE_ACCESSED_BEFORE_BIND,null),
                    xctxt.getSAXLocator());
        // "Variable accessed before it is bound!", xctxt.getSAXLocator());
        // Lazy execution of variables.
        if(val.getType()==XObject.CLASS_UNRESOLVEDVARIABLE)
            return (_stackFrames[index]=val.execute(xctxt));
        return destructiveOK?val:val.getFresh();
    }

    public boolean isLocalSet(int index) throws TransformerException{
        return (_stackFrames[index+_currentFrameBottom]!=null);
    }

    public void clearLocalSlots(int start,int len){
        start+=_currentFrameBottom;
        System.arraycopy(m_nulls,0,_stackFrames,start,len);
    }

    public void setGlobalVariable(final int index,final XObject val){
        _stackFrames[index]=val;
    }

    public XObject getGlobalVariable(XPathContext xctxt,final int index)
            throws TransformerException{
        XObject val=_stackFrames[index];
        // Lazy execution of variables.
        if(val.getType()==XObject.CLASS_UNRESOLVEDVARIABLE)
            return (_stackFrames[index]=val.execute(xctxt));
        return val;
    }

    public XObject getGlobalVariable(XPathContext xctxt,final int index,boolean destructiveOK)
            throws TransformerException{
        XObject val=_stackFrames[index];
        // Lazy execution of variables.
        if(val.getType()==XObject.CLASS_UNRESOLVEDVARIABLE)
            return (_stackFrames[index]=val.execute(xctxt));
        return destructiveOK?val:val.getFresh();
    }

    public XObject getVariableOrParam(
            XPathContext xctxt,com.sun.org.apache.xml.internal.utils.QName qname)
            throws TransformerException{
        // J2SE does not support Xalan interpretive
        /**
         com.sun.org.apache.xml.internal.utils.PrefixResolver prefixResolver =
         xctxt.getNamespaceContext();

         // Get the current ElemTemplateElement, which must be pushed in as the
         // prefix resolver, and then walk backwards in document order, searching
         // for an xsl:param element or xsl:variable element that matches our
         // qname.  If we reach the top level, use the StylesheetRoot's composed
         // list of top level variables and parameters.

         if (prefixResolver instanceof com.sun.org.apache.xalan.internal.templates.ElemTemplateElement)
         {

         com.sun.org.apache.xalan.internal.templates.ElemVariable vvar;

         com.sun.org.apache.xalan.internal.templates.ElemTemplateElement prev =
         (com.sun.org.apache.xalan.internal.templates.ElemTemplateElement) prefixResolver;

         if (!(prev instanceof com.sun.org.apache.xalan.internal.templates.Stylesheet))
         {
         while ( !(prev.getParentNode() instanceof com.sun.org.apache.xalan.internal.templates.Stylesheet) )
         {
         com.sun.org.apache.xalan.internal.templates.ElemTemplateElement savedprev = prev;

         while (null != (prev = prev.getPreviousSiblingElem()))
         {
         if (prev instanceof com.sun.org.apache.xalan.internal.templates.ElemVariable)
         {
         vvar = (com.sun.org.apache.xalan.internal.templates.ElemVariable) prev;

         if (vvar.getName().equals(qname))
         return getLocalVariable(xctxt, vvar.getIndex());
         }
         }
         prev = savedprev.getParentElem();
         }
         }

         vvar = prev.getStylesheetRoot().getVariableOrParamComposed(qname);
         if (null != vvar)
         return getGlobalVariable(xctxt, vvar.getIndex());
         }
         */
        throw new TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_VAR_NOT_RESOLVABLE,new Object[]{qname.toString()})); //"Variable not resolvable: " + qname);
    }
}  // end VariableStack
