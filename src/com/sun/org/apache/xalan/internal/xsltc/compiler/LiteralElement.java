/**
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * $Id: LiteralElement.java,v 1.2.4.1 2005/09/13 12:38:33 pvedula Exp $
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: LiteralElement.java,v 1.2.4.1 2005/09/13 12:38:33 pvedula Exp $
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xml.internal.serializer.ElemDesc;
import com.sun.org.apache.xml.internal.serializer.ToHTMLStream;

import java.util.*;

final class LiteralElement extends Instruction{
    private final static String XMLNS_STRING="xmlns";
    private String _name;
    private LiteralElement _literalElemParent=null;
    private List<SyntaxTreeNode> _attributeElements=null;
    private Map<String,String> _accessedPrefixes=null;
    // True if all attributes of this LRE are unique, i.e. they all have
    // different names. This flag is set to false if some attribute
    // names are not known at compile time.
    private boolean _allAttributesUnique=false;

    public QName getName(){
        return _qname;
    }

    private String accessedNamespace(String prefix){
        if(_literalElemParent!=null){
            String result=_literalElemParent.accessedNamespace(prefix);
            if(result!=null){
                return result;
            }
        }
        return _accessedPrefixes!=null?_accessedPrefixes.get(prefix):null;
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError{
        // Type-check all attributes
        if(_attributeElements!=null){
            for(SyntaxTreeNode node : _attributeElements){
                node.typeCheck(stable);
            }
        }
        typeCheckContents(stable);
        return Type.Void;
    }

    public void translate(ClassGenerator classGen,MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        // Check whether all attributes are unique.
        _allAttributesUnique=checkAttributesUnique();
        // Compile code to emit element start tag
        il.append(methodGen.loadHandler());
        il.append(new PUSH(cpg,_name));
        il.append(DUP2);                // duplicate these 2 args for endElement
        il.append(methodGen.startElement());
        // The value of an attribute may depend on a (sibling) variable
        int j=0;
        while(j<elementCount()){
            final SyntaxTreeNode item=(SyntaxTreeNode)elementAt(j);
            if(item instanceof Variable){
                item.translate(classGen,methodGen);
            }
            j++;
        }
        // Compile code to emit namespace attributes
        if(_accessedPrefixes!=null){
            boolean declaresDefaultNS=false;
            for(Map.Entry<String,String> entry : _accessedPrefixes.entrySet()){
                final String prefix=entry.getKey();
                final String uri=entry.getValue();
                if(uri!=Constants.EMPTYSTRING||
                        prefix!=Constants.EMPTYSTRING){
                    if(prefix==Constants.EMPTYSTRING){
                        declaresDefaultNS=true;
                    }
                    il.append(methodGen.loadHandler());
                    il.append(new PUSH(cpg,prefix));
                    il.append(new PUSH(cpg,uri));
                    il.append(methodGen.namespace());
                }
            }
            /**
             * If our XslElement parent redeclares the default NS, and this
             * element doesn't, it must be redeclared one more time.
             */
            if(!declaresDefaultNS&&(_parent instanceof XslElement)
                    &&((XslElement)_parent).declaresDefaultNS()){
                il.append(methodGen.loadHandler());
                il.append(new PUSH(cpg,Constants.EMPTYSTRING));
                il.append(new PUSH(cpg,Constants.EMPTYSTRING));
                il.append(methodGen.namespace());
            }
        }
        // Output all attributes
        if(_attributeElements!=null){
            for(SyntaxTreeNode node : _attributeElements){
                if(!(node instanceof XslAttribute)){
                    node.translate(classGen,methodGen);
                }
            }
        }
        // Compile code to emit attributes and child elements
        translateContents(classGen,methodGen);
        // Compile code to emit element end tag
        il.append(methodGen.endElement());
    }

    public void parseContents(Parser parser){
        final SymbolTable stable=parser.getSymbolTable();
        stable.setCurrentNode(this);
        // Check if in a literal element context
        SyntaxTreeNode parent=getParent();
        if(parent!=null&&parent instanceof LiteralElement){
            _literalElemParent=(LiteralElement)parent;
        }
        _name=translateQName(_qname,stable);
        // Process all attributes and register all namespaces they use
        final int count=_attributes.getLength();
        for(int i=0;i<count;i++){
            final QName qname=parser.getQName(_attributes.getQName(i));
            final String uri=qname.getNamespace();
            final String val=_attributes.getValue(i);
            // Handle xsl:use-attribute-sets. Attribute sets are placed first
            // in the vector or attributes to make sure that later local
            // attributes can override an attributes in the set.
            if(qname.equals(parser.getUseAttributeSets())){
                if(!Util.isValidQNames(val)){
                    ErrorMsg err=new ErrorMsg(ErrorMsg.INVALID_QNAME_ERR,val,this);
                    parser.reportError(Constants.ERROR,err);
                }
                setFirstAttribute(new UseAttributeSets(val,parser));
            }
            // Handle xsl:extension-element-prefixes
            else if(qname.equals(parser.getExtensionElementPrefixes())){
                stable.excludeNamespaces(val);
            }
            // Handle xsl:exclude-result-prefixes
            else if(qname.equals(parser.getExcludeResultPrefixes())){
                stable.excludeNamespaces(val);
            }else{
                // Ignore special attributes (e.g. xmlns:prefix and xmlns)
                final String prefix=qname.getPrefix();
                if(prefix!=null&&prefix.equals(XMLNS_PREFIX)||
                        prefix==null&&qname.getLocalPart().equals("xmlns")||
                        uri!=null&&uri.equals(XSLT_URI)){
                    continue;
                }
                // Handle all other literal attributes
                final String name=translateQName(qname,stable);
                LiteralAttribute attr=new LiteralAttribute(name,val,parser,this);
                addAttribute(attr);
                attr.setParent(this);
                attr.parseContents(parser);
            }
        }
        // Register all namespaces that are in scope, except for those that
        // are listed in the xsl:stylesheet element's *-prefixes attributes
        Set<Map.Entry<String,String>> include=getNamespaceScope(this);
        for(Map.Entry<String,String> entry : include){
            final String prefix=entry.getKey();
            if(!prefix.equals("xml")){
                final String uri=lookupNamespace(prefix);
                if(uri!=null&&!stable.isExcludedNamespace(uri)){
                    registerNamespace(prefix,uri,stable,true);
                }
            }
        }
        parseChildren(parser);
        // Process all attributes and register all namespaces they use
        for(int i=0;i<count;i++){
            final QName qname=parser.getQName(_attributes.getQName(i));
            final String val=_attributes.getValue(i);
            // Handle xsl:extension-element-prefixes
            if(qname.equals(parser.getExtensionElementPrefixes())){
                stable.unExcludeNamespaces(val);
            }
            // Handle xsl:exclude-result-prefixes
            else if(qname.equals(parser.getExcludeResultPrefixes())){
                stable.unExcludeNamespaces(val);
            }
        }
    }

    private String translateQName(QName qname,SymbolTable stable){
        // Break up the QName and get prefix:localname strings
        String localname=qname.getLocalPart();
        String prefix=qname.getPrefix();
        // Treat default namespace as "" and not null
        if(prefix==null)
            prefix=Constants.EMPTYSTRING;
        else if(prefix.equals(XMLNS_STRING))
            return (XMLNS_STRING);
        // Check if we must translate the prefix
        final String alternative=stable.lookupPrefixAlias(prefix);
        if(alternative!=null){
            stable.excludeNamespaces(prefix);
            prefix=alternative;
        }
        // Get the namespace this prefix refers to
        String uri=lookupNamespace(prefix);
        if(uri==null) return (localname);
        // Register the namespace as accessed
        registerNamespace(prefix,uri,stable,false);
        // Construct the new name for the element (may be unchanged)
        if(prefix!=Constants.EMPTYSTRING)
            return (prefix+":"+localname);
        else
            return (localname);
    }

    public void registerNamespace(String prefix,String uri,
                                  SymbolTable stable,boolean declared){
        // Check if the parent has a declaration for this namespace
        if(_literalElemParent!=null){
            final String parentUri=_literalElemParent.accessedNamespace(prefix);
            if(parentUri!=null&&parentUri.equals(uri)){
                return;
            }
        }
        // Check if we have any declared namespaces
        if(_accessedPrefixes==null){
            _accessedPrefixes=new Hashtable<>();
        }else{
            if(!declared){
                // Check if this node has a declaration for this namespace
                final String old=_accessedPrefixes.get(prefix);
                if(old!=null){
                    if(old.equals(uri))
                        return;
                    else
                        prefix=stable.generateNamespacePrefix();
                }
            }
        }
        if(!prefix.equals("xml")){
            _accessedPrefixes.put(prefix,uri);
        }
    }

    public void addAttribute(SyntaxTreeNode attribute){
        if(_attributeElements==null){
            _attributeElements=new ArrayList<>(2);
        }
        _attributeElements.add(attribute);
    }

    public void setFirstAttribute(SyntaxTreeNode attribute){
        if(_attributeElements==null){
            _attributeElements=new ArrayList<>(2);
        }
        _attributeElements.add(0,attribute);
    }

    public Set<Map.Entry<String,String>> getNamespaceScope(SyntaxTreeNode node){
        Map<String,String> all=new HashMap<>();
        while(node!=null){
            Map<String,String> mapping=node.getPrefixMapping();
            if(mapping!=null){
                for(String prefix : mapping.keySet()){
                    if(!all.containsKey(prefix)){
                        all.put(prefix,mapping.get(prefix));
                    }
                }
            }
            node=node.getParent();
        }
        return all.entrySet();
    }

    protected boolean contextDependent(){
        return dependentContents();
    }

    public void display(int indent){
        indent(indent);
        Util.println("LiteralElement name = "+_name);
        displayContents(indent+IndentIncrement);
    }

    public ElemDesc getElemDesc(){
        if(isHTMLOutput()){
            return ToHTMLStream.getElemDesc(_name);
        }else
            return null;
    }

    private boolean isHTMLOutput(){
        return getStylesheet().getOutputMethod()==Stylesheet.HTML_OUTPUT;
    }

    public boolean allAttributesUnique(){
        return _allAttributesUnique;
    }

    private boolean checkAttributesUnique(){
        boolean hasHiddenXslAttribute=canProduceAttributeNodes(this,true);
        if(hasHiddenXslAttribute)
            return false;
        if(_attributeElements!=null){
            int numAttrs=_attributeElements.size();
            Map<String,SyntaxTreeNode> attrsTable=null;
            for(int i=0;i<numAttrs;i++){
                SyntaxTreeNode node=_attributeElements.get(i);
                if(node instanceof UseAttributeSets){
                    return false;
                }else if(node instanceof XslAttribute){
                    if(attrsTable==null){
                        attrsTable=new HashMap<>();
                        for(int k=0;k<i;k++){
                            SyntaxTreeNode n=_attributeElements.get(k);
                            if(n instanceof LiteralAttribute){
                                LiteralAttribute literalAttr=(LiteralAttribute)n;
                                attrsTable.put(literalAttr.getName(),literalAttr);
                            }
                        }
                    }
                    XslAttribute xslAttr=(XslAttribute)node;
                    AttributeValue attrName=xslAttr.getName();
                    if(attrName instanceof AttributeValueTemplate){
                        return false;
                    }else if(attrName instanceof SimpleAttributeValue){
                        SimpleAttributeValue simpleAttr=(SimpleAttributeValue)attrName;
                        String name=simpleAttr.toString();
                        if(name!=null&&attrsTable.get(name)!=null)
                            return false;
                        else if(name!=null){
                            attrsTable.put(name,xslAttr);
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean canProduceAttributeNodes(SyntaxTreeNode node,boolean ignoreXslAttribute){
        List<SyntaxTreeNode> contents=node.getContents();
        for(SyntaxTreeNode child : contents){
            if(child instanceof Text){
                Text text=(Text)child;
                if(text.isIgnore())
                    continue;
                else
                    return false;
            }
            // Cannot add an attribute to an element after children have been added to it.
            // We can safely return false when the instruction can produce an output node.
            else if(child instanceof LiteralElement
                    ||child instanceof ValueOf
                    ||child instanceof XslElement
                    ||child instanceof Comment
                    ||child instanceof Number
                    ||child instanceof ProcessingInstruction)
                return false;
            else if(child instanceof XslAttribute){
                if(ignoreXslAttribute)
                    continue;
                else
                    return true;
            }
            // In general, there is no way to check whether <xsl:call-template> or
            // <xsl:apply-templates> can produce attribute nodes. <xsl:copy> and
            // <xsl:copy-of> can also copy attribute nodes to an element. Return
            // true in those cases to be safe.
            else if(child instanceof CallTemplate
                    ||child instanceof ApplyTemplates
                    ||child instanceof Copy
                    ||child instanceof CopyOf)
                return true;
            else if((child instanceof If
                    ||child instanceof ForEach)
                    &&canProduceAttributeNodes(child,false)){
                return true;
            }else if(child instanceof Choose){
                List<SyntaxTreeNode> chooseContents=child.getContents();
                for(SyntaxTreeNode chooseChild : chooseContents){
                    if(chooseChild instanceof When||chooseChild instanceof Otherwise){
                        if(canProduceAttributeNodes(chooseChild,false))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
