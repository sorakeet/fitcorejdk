/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

public abstract class CompiledScript{
    public Object eval(Bindings bindings) throws ScriptException{
        ScriptContext ctxt=getEngine().getContext();
        if(bindings!=null){
            SimpleScriptContext tempctxt=new SimpleScriptContext();
            tempctxt.setBindings(bindings,ScriptContext.ENGINE_SCOPE);
            tempctxt.setBindings(ctxt.getBindings(ScriptContext.GLOBAL_SCOPE),
                    ScriptContext.GLOBAL_SCOPE);
            tempctxt.setWriter(ctxt.getWriter());
            tempctxt.setReader(ctxt.getReader());
            tempctxt.setErrorWriter(ctxt.getErrorWriter());
            ctxt=tempctxt;
        }
        return eval(ctxt);
    }

    public abstract Object eval(ScriptContext context) throws ScriptException;

    public abstract ScriptEngine getEngine();

    public Object eval() throws ScriptException{
        return eval(getEngine().getContext());
    }
}
