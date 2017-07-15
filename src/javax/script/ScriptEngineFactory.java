/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.script;

import java.util.List;

public interface ScriptEngineFactory{
    public String getEngineName();

    public String getEngineVersion();

    public List<String> getExtensions();

    public List<String> getMimeTypes();

    public List<String> getNames();

    public String getLanguageName();

    public String getLanguageVersion();

    public Object getParameter(String key);

    public String getMethodCallSyntax(String obj,String m,String... args);

    public String getOutputStatement(String toDisplay);

    public String getProgram(String... statements);

    public ScriptEngine getScriptEngine();
}
