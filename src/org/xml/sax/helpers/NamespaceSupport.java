/**
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// NamespaceSupport.java - generic Namespace support for SAX.
// http://www.saxproject.org
// Written by David Megginson
// This class is in the Public Domain.  NO WARRANTY!
// $Id: NamespaceSupport.java,v 1.5 2004/11/03 22:53:09 jsuttor Exp $
package org.xml.sax.helpers;

import java.util.*;

public class NamespaceSupport{
    ////////////////////////////////////////////////////////////////////
    // Constants.
    ////////////////////////////////////////////////////////////////////
    public final static String XMLNS=
            "http://www.w3.org/XML/1998/namespace";
    public final static String NSDECL=
            "http://www.w3.org/xmlns/2000/";
    private final static Enumeration EMPTY_ENUMERATION=
            Collections.enumeration(new ArrayList<String>());
    ////////////////////////////////////////////////////////////////////
    // Constructor.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    private Context contexts[];
    ////////////////////////////////////////////////////////////////////
    // Context management.
    ////////////////////////////////////////////////////////////////////
    private Context currentContext;
    private int contextPos;
    private boolean namespaceDeclUris;
    ////////////////////////////////////////////////////////////////////
    // Operations within a context.
    ////////////////////////////////////////////////////////////////////

    public NamespaceSupport(){
        reset();
    }

    public void reset(){
        contexts=new Context[32];
        namespaceDeclUris=false;
        contextPos=0;
        contexts[contextPos]=currentContext=new Context();
        currentContext.declarePrefix("xml",XMLNS);
    }

    public void pushContext(){
        int max=contexts.length;
        contextPos++;
        // Extend the array if necessary
        if(contextPos>=max){
            Context newContexts[]=new Context[max*2];
            System.arraycopy(contexts,0,newContexts,0,max);
            max*=2;
            contexts=newContexts;
        }
        // Allocate the context if necessary.
        currentContext=contexts[contextPos];
        if(currentContext==null){
            contexts[contextPos]=currentContext=new Context();
        }
        // Set the parent, if any.
        if(contextPos>0){
            currentContext.setParent(contexts[contextPos-1]);
        }
    }

    public void popContext(){
        contexts[contextPos].clear();
        contextPos--;
        if(contextPos<0){
            throw new EmptyStackException();
        }
        currentContext=contexts[contextPos];
    }

    public boolean declarePrefix(String prefix,String uri){
        if(prefix.equals("xml")||prefix.equals("xmlns")){
            return false;
        }else{
            currentContext.declarePrefix(prefix,uri);
            return true;
        }
    }

    public String[] processName(String qName,String parts[],
                                boolean isAttribute){
        String myParts[]=currentContext.processName(qName,isAttribute);
        if(myParts==null){
            return null;
        }else{
            parts[0]=myParts[0];
            parts[1]=myParts[1];
            parts[2]=myParts[2];
            return parts;
        }
    }

    public String getPrefix(String uri){
        return currentContext.getPrefix(uri);
    }

    public Enumeration getPrefixes(String uri){
        List<String> prefixes=new ArrayList<>();
        Enumeration allPrefixes=getPrefixes();
        while(allPrefixes.hasMoreElements()){
            String prefix=(String)allPrefixes.nextElement();
            if(uri.equals(getURI(prefix))){
                prefixes.add(prefix);
            }
        }
        return Collections.enumeration(prefixes);
    }

    public String getURI(String prefix){
        return currentContext.getURI(prefix);
    }

    public Enumeration getPrefixes(){
        return currentContext.getPrefixes();
    }

    public Enumeration getDeclaredPrefixes(){
        return currentContext.getDeclaredPrefixes();
    }

    public boolean isNamespaceDeclUris(){
        return namespaceDeclUris;
    }

    public void setNamespaceDeclUris(boolean value){
        if(contextPos!=0)
            throw new IllegalStateException();
        if(value==namespaceDeclUris)
            return;
        namespaceDeclUris=value;
        if(value)
            currentContext.declarePrefix("xmlns",NSDECL);
        else{
            contexts[contextPos]=currentContext=new Context();
            currentContext.declarePrefix("xml",XMLNS);
        }
    }
    ////////////////////////////////////////////////////////////////////
    // Internal classes.
    ////////////////////////////////////////////////////////////////////

    final class Context{
        ////////////////////////////////////////////////////////////////
        // Protected state.
        ////////////////////////////////////////////////////////////////
        Map<String,String> prefixTable;
        Map<String,String> uriTable;
        Map<String,String[]> elementNameTable;
        Map<String,String[]> attributeNameTable;
        String defaultNS=null;
        ////////////////////////////////////////////////////////////////
        // Internal state.
        ////////////////////////////////////////////////////////////////
        private List<String> declarations=null;
        private boolean declSeen=false;
        private Context parent=null;

        Context(){
            copyTables();
        }
        ////////////////////////////////////////////////////////////////
        // Internal methods.
        ////////////////////////////////////////////////////////////////

        private void copyTables(){
            if(prefixTable!=null){
                prefixTable=new HashMap<>(prefixTable);
            }else{
                prefixTable=new HashMap<>();
            }
            if(uriTable!=null){
                uriTable=new HashMap<>(uriTable);
            }else{
                uriTable=new HashMap<>();
            }
            elementNameTable=new HashMap<>();
            attributeNameTable=new HashMap<>();
            declSeen=true;
        }

        void setParent(Context parent){
            this.parent=parent;
            declarations=null;
            prefixTable=parent.prefixTable;
            uriTable=parent.uriTable;
            elementNameTable=parent.elementNameTable;
            attributeNameTable=parent.attributeNameTable;
            defaultNS=parent.defaultNS;
            declSeen=false;
        }

        void clear(){
            parent=null;
            prefixTable=null;
            uriTable=null;
            elementNameTable=null;
            attributeNameTable=null;
            defaultNS=null;
        }

        void declarePrefix(String prefix,String uri){
            // Lazy processing...
//          if (!declsOK)
//              throw new IllegalStateException (
//                  "can't declare any more prefixes in this context");
            if(!declSeen){
                copyTables();
            }
            if(declarations==null){
                declarations=new ArrayList<>();
            }
            prefix=prefix.intern();
            uri=uri.intern();
            if("".equals(prefix)){
                if("".equals(uri)){
                    defaultNS=null;
                }else{
                    defaultNS=uri;
                }
            }else{
                prefixTable.put(prefix,uri);
                uriTable.put(uri,prefix); // may wipe out another prefix
            }
            declarations.add(prefix);
        }

        String[] processName(String qName,boolean isAttribute){
            String name[];
            Map<String,String[]> table;
            // Select the appropriate table.
            if(isAttribute){
                table=attributeNameTable;
            }else{
                table=elementNameTable;
            }
            // Start by looking in the cache, and
            // return immediately if the name
            // is already known in this content
            name=(String[])table.get(qName);
            if(name!=null){
                return name;
            }
            // We haven't seen this name in this
            // context before.  Maybe in the parent
            // context, but we can't assume prefix
            // bindings are the same.
            name=new String[3];
            name[2]=qName.intern();
            int index=qName.indexOf(':');
            // No prefix.
            if(index==-1){
                if(isAttribute){
                    if(qName=="xmlns"&&namespaceDeclUris)
                        name[0]=NSDECL;
                    else
                        name[0]="";
                }else if(defaultNS==null){
                    name[0]="";
                }else{
                    name[0]=defaultNS;
                }
                name[1]=name[2];
            }
            // Prefix
            else{
                String prefix=qName.substring(0,index);
                String local=qName.substring(index+1);
                String uri;
                if("".equals(prefix)){
                    uri=defaultNS;
                }else{
                    uri=(String)prefixTable.get(prefix);
                }
                if(uri==null
                        ||(!isAttribute&&"xmlns".equals(prefix))){
                    return null;
                }
                name[0]=uri;
                name[1]=local.intern();
            }
            // Save in the cache for future use.
            // (Could be shared with parent context...)
            table.put(name[2],name);
            return name;
        }

        String getURI(String prefix){
            if("".equals(prefix)){
                return defaultNS;
            }else if(prefixTable==null){
                return null;
            }else{
                return (String)prefixTable.get(prefix);
            }
        }

        String getPrefix(String uri){
            if(uriTable==null){
                return null;
            }else{
                return (String)uriTable.get(uri);
            }
        }

        Enumeration getDeclaredPrefixes(){
            if(declarations==null){
                return EMPTY_ENUMERATION;
            }else{
                return Collections.enumeration(declarations);
            }
        }

        Enumeration getPrefixes(){
            if(prefixTable==null){
                return EMPTY_ENUMERATION;
            }else{
                return Collections.enumeration(prefixTable.keySet());
            }
        }
    }
}
// end of NamespaceSupport.java
