/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SimpleScriptContext implements ScriptContext{
    private static List<Integer> scopes;

    static{
        scopes=new ArrayList<Integer>(2);
        scopes.add(ENGINE_SCOPE);
        scopes.add(GLOBAL_SCOPE);
        scopes=Collections.unmodifiableList(scopes);
    }

    protected Writer writer;
    protected Writer errorWriter;
    protected Reader reader;
    protected Bindings engineScope;
    protected Bindings globalScope;

    public SimpleScriptContext(){
        engineScope=new SimpleBindings();
        globalScope=null;
        reader=new InputStreamReader(System.in);
        writer=new PrintWriter(System.out,true);
        errorWriter=new PrintWriter(System.err,true);
    }

    public void setBindings(Bindings bindings,int scope){
        switch(scope){
            case ENGINE_SCOPE:
                if(bindings==null){
                    throw new NullPointerException("Engine scope cannot be null.");
                }
                engineScope=bindings;
                break;
            case GLOBAL_SCOPE:
                globalScope=bindings;
                break;
            default:
                throw new IllegalArgumentException("Invalid scope value.");
        }
    }

    public Bindings getBindings(int scope){
        if(scope==ENGINE_SCOPE){
            return engineScope;
        }else if(scope==GLOBAL_SCOPE){
            return globalScope;
        }else{
            throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    public void setAttribute(String name,Object value,int scope){
        checkName(name);
        switch(scope){
            case ENGINE_SCOPE:
                engineScope.put(name,value);
                return;
            case GLOBAL_SCOPE:
                if(globalScope!=null){
                    globalScope.put(name,value);
                }
                return;
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    public Object getAttribute(String name,int scope){
        checkName(name);
        switch(scope){
            case ENGINE_SCOPE:
                return engineScope.get(name);
            case GLOBAL_SCOPE:
                if(globalScope!=null){
                    return globalScope.get(name);
                }
                return null;
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    public Object removeAttribute(String name,int scope){
        checkName(name);
        switch(scope){
            case ENGINE_SCOPE:
                if(getBindings(ENGINE_SCOPE)!=null){
                    return getBindings(ENGINE_SCOPE).remove(name);
                }
                return null;
            case GLOBAL_SCOPE:
                if(getBindings(GLOBAL_SCOPE)!=null){
                    return getBindings(GLOBAL_SCOPE).remove(name);
                }
                return null;
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    public Object getAttribute(String name){
        checkName(name);
        if(engineScope.containsKey(name)){
            return getAttribute(name,ENGINE_SCOPE);
        }else if(globalScope!=null&&globalScope.containsKey(name)){
            return getAttribute(name,GLOBAL_SCOPE);
        }
        return null;
    }

    public int getAttributesScope(String name){
        checkName(name);
        if(engineScope.containsKey(name)){
            return ENGINE_SCOPE;
        }else if(globalScope!=null&&globalScope.containsKey(name)){
            return GLOBAL_SCOPE;
        }else{
            return -1;
        }
    }

    public Writer getWriter(){
        return writer;
    }

    public void setWriter(Writer writer){
        this.writer=writer;
    }

    public Writer getErrorWriter(){
        return errorWriter;
    }

    public void setErrorWriter(Writer writer){
        this.errorWriter=writer;
    }

    public Reader getReader(){
        return reader;
    }

    public void setReader(Reader reader){
        this.reader=reader;
    }

    public List<Integer> getScopes(){
        return scopes;
    }

    private void checkName(String name){
        Objects.requireNonNull(name);
        if(name.isEmpty()){
            throw new IllegalArgumentException("name cannot be empty");
        }
    }
}
