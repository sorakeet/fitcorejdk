/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2004 The Apache Software Foundation.
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
 * Copyright 2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.impl.xs;

import com.sun.org.apache.xerces.internal.impl.xs.util.XSGrammarPool;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xs.*;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSInput;

public final class XSLoaderImpl implements XSLoader, DOMConfiguration{
    private final XSGrammarPool fGrammarPool=new XSGrammarMerger();
    private final XMLSchemaLoader fSchemaLoader=new XMLSchemaLoader();

    public XSLoaderImpl(){
        fSchemaLoader.setProperty(XMLSchemaLoader.XMLGRAMMAR_POOL,fGrammarPool);
    }

    public DOMConfiguration getConfig(){
        return this;
    }

    public XSModel loadURIList(StringList uriList){
        int length=uriList.getLength();
        try{
            fGrammarPool.clear();
            for(int i=0;i<length;++i){
                fSchemaLoader.loadGrammar(new XMLInputSource(null,uriList.item(i),null));
            }
            return fGrammarPool.toXSModel();
        }catch(Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    public XSModel loadInputList(LSInputList is){
        final int length=is.getLength();
        try{
            fGrammarPool.clear();
            for(int i=0;i<length;++i){
                fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is.item(i)));
            }
            return fGrammarPool.toXSModel();
        }catch(Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    public XSModel loadURI(String uri){
        try{
            fGrammarPool.clear();
            return ((XSGrammar)fSchemaLoader.loadGrammar(new XMLInputSource(null,uri,null))).toXSModel();
        }catch(Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    public XSModel load(LSInput is){
        try{
            fGrammarPool.clear();
            return ((XSGrammar)fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is))).toXSModel();
        }catch(Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    public void setParameter(String name,Object value) throws DOMException{
        fSchemaLoader.setParameter(name,value);
    }

    public Object getParameter(String name) throws DOMException{
        return fSchemaLoader.getParameter(name);
    }

    public boolean canSetParameter(String name,Object value){
        return fSchemaLoader.canSetParameter(name,value);
    }

    public DOMStringList getParameterNames(){
        return fSchemaLoader.getParameterNames();
    }

    private static final class XSGrammarMerger extends XSGrammarPool{
        public XSGrammarMerger(){
        }

        public Grammar[] retrieveInitialGrammarSet(String grammarType){
            return new Grammar[0];
        }

        public Grammar retrieveGrammar(XMLGrammarDescription desc){
            return null;
        }

        public void putGrammar(Grammar grammar){
            SchemaGrammar cachedGrammar=
                    toSchemaGrammar(super.getGrammar(grammar.getGrammarDescription()));
            if(cachedGrammar!=null){
                SchemaGrammar newGrammar=toSchemaGrammar(grammar);
                if(newGrammar!=null){
                    mergeSchemaGrammars(cachedGrammar,newGrammar);
                }
            }else{
                super.putGrammar(grammar);
            }
        }

        private SchemaGrammar toSchemaGrammar(Grammar grammar){
            return (grammar instanceof SchemaGrammar)?(SchemaGrammar)grammar:null;
        }

        private void mergeSchemaGrammars(SchemaGrammar cachedGrammar,SchemaGrammar newGrammar){
            /** Add new top-level element declarations. **/
            XSNamedMap map=newGrammar.getComponents(XSConstants.ELEMENT_DECLARATION);
            int length=map.getLength();
            for(int i=0;i<length;++i){
                XSElementDecl decl=(XSElementDecl)map.item(i);
                if(cachedGrammar.getGlobalElementDecl(decl.getName())==null){
                    cachedGrammar.addGlobalElementDecl(decl);
                }
            }
            /** Add new top-level attribute declarations. **/
            map=newGrammar.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
            length=map.getLength();
            for(int i=0;i<length;++i){
                XSAttributeDecl decl=(XSAttributeDecl)map.item(i);
                if(cachedGrammar.getGlobalAttributeDecl(decl.getName())==null){
                    cachedGrammar.addGlobalAttributeDecl(decl);
                }
            }
            /** Add new top-level type definitions. **/
            map=newGrammar.getComponents(XSConstants.TYPE_DEFINITION);
            length=map.getLength();
            for(int i=0;i<length;++i){
                XSTypeDefinition decl=(XSTypeDefinition)map.item(i);
                if(cachedGrammar.getGlobalTypeDecl(decl.getName())==null){
                    cachedGrammar.addGlobalTypeDecl(decl);
                }
            }
            /** Add new top-level attribute group definitions. **/
            map=newGrammar.getComponents(XSConstants.ATTRIBUTE_GROUP);
            length=map.getLength();
            for(int i=0;i<length;++i){
                XSAttributeGroupDecl decl=(XSAttributeGroupDecl)map.item(i);
                if(cachedGrammar.getGlobalAttributeGroupDecl(decl.getName())==null){
                    cachedGrammar.addGlobalAttributeGroupDecl(decl);
                }
            }
            /** Add new top-level model group definitions. **/
            map=newGrammar.getComponents(XSConstants.MODEL_GROUP);
            length=map.getLength();
            for(int i=0;i<length;++i){
                XSGroupDecl decl=(XSGroupDecl)map.item(i);
                if(cachedGrammar.getGlobalGroupDecl(decl.getName())==null){
                    cachedGrammar.addGlobalGroupDecl(decl);
                }
            }
            /** Add new top-level notation declarations. **/
            map=newGrammar.getComponents(XSConstants.NOTATION_DECLARATION);
            length=map.getLength();
            for(int i=0;i<length;++i){
                XSNotationDecl decl=(XSNotationDecl)map.item(i);
                if(cachedGrammar.getGlobalNotationDecl(decl.getName())==null){
                    cachedGrammar.addGlobalNotationDecl(decl);
                }
            }
            /**
             * Add all annotations. Since these components are not named it's
             * possible we'll add duplicate components. There isn't much we can
             * do. It's no worse than XMLSchemaLoader when used as an XSLoader.
             */
            XSObjectList annotations=newGrammar.getAnnotations();
            length=annotations.getLength();
            for(int i=0;i<length;++i){
                cachedGrammar.addAnnotation((XSAnnotationImpl)annotations.item(i));
            }
        }

        public Grammar getGrammar(XMLGrammarDescription desc){
            return null;
        }

        public boolean containsGrammar(XMLGrammarDescription desc){
            return false;
        }
    }
}
