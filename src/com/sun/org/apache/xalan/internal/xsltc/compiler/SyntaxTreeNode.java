/**
 * Copyright (c) 2006, 2016, Oracle and/or its affiliates. All rights reserved.
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
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xalan.internal.xsltc.compiler;

import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.*;

public abstract class SyntaxTreeNode implements Constants{
    // Sentinel - used to denote unrecognised syntaxt tree nodes.
    protected static final SyntaxTreeNode Dummy=new AbsolutePathPattern(null);
    // These two are used for indenting nodes in the AST (debug output)
    protected static final int IndentIncrement=4;
    private static final char[] _spaces=
            "                                                       ".toCharArray();
    private final List<SyntaxTreeNode> _contents=new ArrayList<>(2); // Child nodes
    // AST navigation pointers
    protected SyntaxTreeNode _parent;          // Parent node
    // Element description data
    protected QName _qname;                    // The element QName
    protected AttributesImpl _attributes=null;   // Attributes of this element
    // Reference to the AST parser
    private Parser _parser;
    private Stylesheet _stylesheet;      // Stylesheet ancestor node
    private Template _template;        // Template ancestor node
    private int _line;                         // Source file line number
    private Map<String,String> _prefixMapping=null; // Namespace declarations

    public SyntaxTreeNode(){
        _line=0;
        _qname=null;
    }

    public SyntaxTreeNode(int line){
        _line=line;
        _qname=null;
    }

    public SyntaxTreeNode(String uri,String prefix,String local){
        _line=0;
        setQName(uri,prefix,local);
    }

    protected void setQName(String uri,String prefix,String localname){
        _qname=new QName(uri,prefix,localname);
    }

    public final int getLineNumber(){
        if(_line>0) return _line;
        SyntaxTreeNode parent=getParent();
        return (parent!=null)?parent.getLineNumber():0;
    }

    protected final void setLineNumber(int line){
        _line=line;
    }

    protected final SyntaxTreeNode getParent(){
        return _parent;
    }

    protected void setParent(SyntaxTreeNode parent){
        if(_parent==null) _parent=parent;
    }

    protected QName getQName(){
        return (_qname);
    }

    protected void setQName(QName qname){
        _qname=qname;
    }

    protected String getAttribute(String prefix,String localName){
        return getAttribute(prefix+':'+localName);
    }

    protected String getAttribute(String qname){
        if(_attributes==null){
            return EMPTYSTRING;
        }
        final String value=_attributes.getValue(qname);
        return (value==null||value.equals(EMPTYSTRING))?
                EMPTYSTRING:value;
    }

    protected boolean hasAttribute(String qname){
        return (_attributes!=null&&_attributes.getValue(qname)!=null);
    }

    protected void addAttribute(String qname,String value){
        int index=_attributes.getIndex(qname);
        if(index!=-1){
            _attributes.setAttribute(index,"",Util.getLocalName(qname),
                    qname,"CDATA",value);
        }else{
            _attributes.addAttribute("",Util.getLocalName(qname),qname,
                    "CDATA",value);
        }
    }

    protected Attributes getAttributes(){
        return (_attributes);
    }

    protected void setAttributes(AttributesImpl attributes){
        _attributes=attributes;
    }

    protected Map<String,String> getPrefixMapping(){
        return _prefixMapping;
    }

    protected void setPrefixMapping(Map<String,String> mapping){
        _prefixMapping=mapping;
    }

    protected void addPrefixMapping(String prefix,String uri){
        if(_prefixMapping==null)
            _prefixMapping=new HashMap<>();
        _prefixMapping.put(prefix,uri);
    }

    protected String lookupNamespace(String prefix){
        // Initialise the output (default is 'null' for undefined)
        String uri=null;
        // First look up the prefix/uri mapping in our own map...
        if(_prefixMapping!=null)
            uri=_prefixMapping.get(prefix);
        // ... but if we can't find it there we ask our parent for the mapping
        if((uri==null)&&(_parent!=null)){
            uri=_parent.lookupNamespace(prefix);
            if((prefix==Constants.EMPTYSTRING)&&(uri==null))
                uri=Constants.EMPTYSTRING;
        }
        // ... and then we return whatever URI we've got.
        return (uri);
    }

    protected String lookupPrefix(String uri){
        // Initialise the output (default is 'null' for undefined)
        String prefix=null;
        // First look up the prefix/uri mapping in our own map...
        if((_prefixMapping!=null)&&
                (_prefixMapping.containsValue(uri))){
            for(Map.Entry<String,String> entry : _prefixMapping.entrySet()){
                prefix=entry.getKey();
                String mapsTo=entry.getValue();
                if(mapsTo.equals(uri)) return (prefix);
            }
        }
        // ... but if we can't find it there we ask our parent for the mapping
        else if(_parent!=null){
            prefix=_parent.lookupPrefix(uri);
            if((uri==Constants.EMPTYSTRING)&&(prefix==null))
                prefix=Constants.EMPTYSTRING;
        }
        return (prefix);
    }

    public final Parser getParser(){
        return _parser;
    }

    protected void setParser(Parser parser){
        _parser=parser;
    }

    protected final boolean isDummy(){
        return this==Dummy;
    }

    protected int getImportPrecedence(){
        Stylesheet stylesheet=getStylesheet();
        if(stylesheet==null) return Integer.MIN_VALUE;
        return stylesheet.getImportPrecedence();
    }

    public Stylesheet getStylesheet(){
        if(_stylesheet==null){
            SyntaxTreeNode parent=this;
            while(parent!=null){
                if(parent instanceof Stylesheet)
                    return ((Stylesheet)parent);
                parent=parent.getParent();
            }
            _stylesheet=(Stylesheet)parent;
        }
        return (_stylesheet);
    }

    protected Template getTemplate(){
        if(_template==null){
            SyntaxTreeNode parent=this;
            while((parent!=null)&&(!(parent instanceof Template)))
                parent=parent.getParent();
            _template=(Template)parent;
        }
        return (_template);
    }

    protected final XSLTC getXSLTC(){
        return _parser.getXSLTC();
    }

    protected final SymbolTable getSymbolTable(){
        return (_parser==null)?null:_parser.getSymbolTable();
    }

    public void parseContents(Parser parser){
        parseChildren(parser);
    }

    protected final void parseChildren(Parser parser){
        List<QName> locals=null;   // only create when needed
        for(SyntaxTreeNode child : _contents){
            parser.getSymbolTable().setCurrentNode(child);
            child.parseContents(parser);
            // if variable or parameter, add it to scope
            final QName varOrParamName=updateScope(parser,child);
            if(varOrParamName!=null){
                if(locals==null){
                    locals=new ArrayList<>(2);
                }
                locals.add(varOrParamName);
            }
        }
        parser.getSymbolTable().setCurrentNode(this);
        // after the last element, remove any locals from scope
        if(locals!=null){
            for(QName varOrParamName : locals){
                parser.removeVariable(varOrParamName);
            }
        }
    }

    protected QName updateScope(Parser parser,SyntaxTreeNode node){
        if(node instanceof Variable){
            final Variable var=(Variable)node;
            parser.addVariable(var);
            return var.getName();
        }else if(node instanceof Param){
            final Param param=(Param)node;
            parser.addParameter(param);
            return param.getName();
        }else{
            return null;
        }
    }

    public abstract Type typeCheck(SymbolTable stable) throws TypeCheckError;

    protected Type typeCheckContents(SymbolTable stable) throws TypeCheckError{
        for(SyntaxTreeNode item : _contents){
            item.typeCheck(stable);
        }
        return Type.Void;
    }

    public abstract void translate(ClassGenerator classGen,
                                   MethodGenerator methodGen);

    protected void translateContents(ClassGenerator classGen,
                                     MethodGenerator methodGen){
        // Call translate() on all child nodes
        final int n=elementCount();
        for(SyntaxTreeNode item : _contents){
            methodGen.markChunkStart();
            item.translate(classGen,methodGen);
            methodGen.markChunkEnd();
        }
        // After translation, unmap any registers for any variables/parameters
        // that were declared in this scope. Performing this unmapping in the
        // same AST scope as the declaration deals with the problems of
        // references falling out-of-scope inside the for-each element.
        // (the cause of which being 'lazy' register allocation for references)
        for(int i=0;i<n;i++){
            if(_contents.get(i) instanceof VariableBase){
                final VariableBase var=(VariableBase)_contents.get(i);
                var.unmapRegister(classGen,methodGen);
            }
        }
    }

    private boolean isSimpleRTF(SyntaxTreeNode node){
        List<SyntaxTreeNode> contents=node.getContents();
        for(SyntaxTreeNode item : contents){
            if(!isTextElement(item,false))
                return false;
        }
        return true;
    }

    private boolean isAdaptiveRTF(SyntaxTreeNode node){
        List<SyntaxTreeNode> contents=node.getContents();
        for(SyntaxTreeNode item : contents){
            if(!isTextElement(item,true))
                return false;
        }
        return true;
    }

    private boolean isTextElement(SyntaxTreeNode node,boolean doExtendedCheck){
        if(node instanceof ValueOf||node instanceof Number
                ||node instanceof Text){
            return true;
        }else if(node instanceof If){
            return doExtendedCheck?isAdaptiveRTF(node):isSimpleRTF(node);
        }else if(node instanceof Choose){
            List<SyntaxTreeNode> contents=node.getContents();
            for(SyntaxTreeNode item : contents){
                if(item instanceof Text||
                        ((item instanceof When||item instanceof Otherwise)
                                &&((doExtendedCheck&&isAdaptiveRTF(item))
                                ||(!doExtendedCheck&&isSimpleRTF(item)))))
                    continue;
                else
                    return false;
            }
            return true;
        }else if(doExtendedCheck&&
                (node instanceof CallTemplate
                        ||node instanceof ApplyTemplates))
            return true;
        else
            return false;
    }

    protected void compileResultTree(ClassGenerator classGen,
                                     MethodGenerator methodGen){
        final ConstantPoolGen cpg=classGen.getConstantPool();
        final InstructionList il=methodGen.getInstructionList();
        final Stylesheet stylesheet=classGen.getStylesheet();
        boolean isSimple=isSimpleRTF(this);
        boolean isAdaptive=false;
        if(!isSimple){
            isAdaptive=isAdaptiveRTF(this);
        }
        int rtfType=isSimple?DOM.SIMPLE_RTF
                :(isAdaptive?DOM.ADAPTIVE_RTF:DOM.TREE_RTF);
        // Save the current handler base on the stack
        il.append(methodGen.loadHandler());
        final String DOM_CLASS=classGen.getDOMClass();
        // Create new instance of DOM class (with RTF_INITIAL_SIZE nodes)
        //int index = cpg.addMethodref(DOM_IMPL, "<init>", "(I)V");
        //il.append(new NEW(cpg.addClass(DOM_IMPL)));
        il.append(methodGen.loadDOM());
        int index=cpg.addInterfaceMethodref(DOM_INTF,
                "getResultTreeFrag",
                "(IIZ)"+DOM_INTF_SIG);
        il.append(new PUSH(cpg,RTF_INITIAL_SIZE));
        il.append(new PUSH(cpg,rtfType));
        il.append(new PUSH(cpg,stylesheet.callsNodeset()));
        il.append(new INVOKEINTERFACE(index,4));
        il.append(DUP);
        // Overwrite old handler with DOM handler
        index=cpg.addInterfaceMethodref(DOM_INTF,
                "getOutputDomBuilder",
                "()"+TRANSLET_OUTPUT_SIG);
        il.append(new INVOKEINTERFACE(index,1));
        il.append(DUP);
        il.append(methodGen.storeHandler());
        // Call startDocument on the new handler
        il.append(methodGen.startDocument());
        // Instantiate result tree fragment
        translateContents(classGen,methodGen);
        // Call endDocument on the new handler
        il.append(methodGen.loadHandler());
        il.append(methodGen.endDocument());
        // Check if we need to wrap the DOMImpl object in a DOMAdapter object.
        // DOMAdapter is not needed if the RTF is a simple RTF and the nodeset()
        // function is not used.
        if(stylesheet.callsNodeset()
                &&!DOM_CLASS.equals(DOM_IMPL_CLASS)){
            // new com.sun.org.apache.xalan.internal.xsltc.dom.DOMAdapter(DOMImpl,String[]);
            index=cpg.addMethodref(DOM_ADAPTER_CLASS,
                    "<init>",
                    "("+DOM_INTF_SIG+
                            "["+STRING_SIG+
                            "["+STRING_SIG+
                            "[I"+
                            "["+STRING_SIG+")V");
            il.append(new NEW(cpg.addClass(DOM_ADAPTER_CLASS)));
            il.append(new DUP_X1());
            il.append(SWAP);
            /**
             * Give the DOM adapter an empty type mapping if the nodeset
             * extension function is never called.
             */
            if(!stylesheet.callsNodeset()){
                il.append(new ICONST(0));
                il.append(new ANEWARRAY(cpg.addClass(STRING)));
                il.append(DUP);
                il.append(DUP);
                il.append(new ICONST(0));
                il.append(new NEWARRAY(BasicType.INT));
                il.append(SWAP);
                il.append(new INVOKESPECIAL(index));
            }else{
                // Push name arrays on the stack
                il.append(ALOAD_0);
                il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                        NAMES_INDEX,
                        NAMES_INDEX_SIG)));
                il.append(ALOAD_0);
                il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                        URIS_INDEX,
                        URIS_INDEX_SIG)));
                il.append(ALOAD_0);
                il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                        TYPES_INDEX,
                        TYPES_INDEX_SIG)));
                il.append(ALOAD_0);
                il.append(new GETFIELD(cpg.addFieldref(TRANSLET_CLASS,
                        NAMESPACE_INDEX,
                        NAMESPACE_INDEX_SIG)));
                // Initialized DOM adapter
                il.append(new INVOKESPECIAL(index));
                // Add DOM adapter to MultiDOM class by calling addDOMAdapter()
                il.append(DUP);
                il.append(methodGen.loadDOM());
                il.append(new CHECKCAST(cpg.addClass(classGen.getDOMClass())));
                il.append(SWAP);
                index=cpg.addMethodref(MULTI_DOM_CLASS,
                        "addDOMAdapter",
                        "("+DOM_ADAPTER_SIG+")I");
                il.append(new INVOKEVIRTUAL(index));
                il.append(POP);         // ignore mask returned by addDOMAdapter
            }
        }
        // Restore old handler base from stack
        il.append(SWAP);
        il.append(methodGen.storeHandler());
    }

    protected boolean contextDependent(){
        return true;
    }

    protected boolean dependentContents(){
        for(SyntaxTreeNode item : _contents){
            if(item.contextDependent()){
                return true;
            }
        }
        return false;
    }

    protected final void addElement(SyntaxTreeNode element){
        _contents.add(element);
        element.setParent(this);
    }

    protected final void setFirstElement(SyntaxTreeNode element){
        _contents.add(0,element);
        element.setParent(this);
    }

    protected final void removeElement(SyntaxTreeNode element){
        _contents.remove(element);
        element.setParent(null);
    }

    protected final List<SyntaxTreeNode> getContents(){
        return _contents;
    }

    protected final boolean hasContents(){
        return elementCount()>0;
    }

    protected final int elementCount(){
        return _contents.size();
    }

    protected final Iterator<SyntaxTreeNode> elements(){
        return _contents.iterator();
    }

    protected final Object elementAt(int pos){
        return _contents.get(pos);
    }

    protected final SyntaxTreeNode lastChild(){
        if(_contents.isEmpty()) return null;
        return _contents.get(_contents.size()-1);
    }

    public void display(int indent){
        displayContents(indent);
    }

    protected void displayContents(int indent){
        for(SyntaxTreeNode item : _contents){
            item.display(indent);
        }
    }

    protected final void indent(int indent){
        System.out.print(new String(_spaces,0,indent));
    }

    protected void reportError(SyntaxTreeNode element,Parser parser,
                               String errorCode,String message){
        final ErrorMsg error=new ErrorMsg(errorCode,message,element);
        parser.reportError(Constants.ERROR,error);
    }

    protected void reportWarning(SyntaxTreeNode element,Parser parser,
                                 String errorCode,String message){
        final ErrorMsg error=new ErrorMsg(errorCode,message,element);
        parser.reportError(Constants.WARNING,error);
    }
}
