/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

import java.io.Reader;

public interface Compilable{
    public CompiledScript compile(String script) throws
            ScriptException;

    public CompiledScript compile(Reader script) throws
            ScriptException;
}
