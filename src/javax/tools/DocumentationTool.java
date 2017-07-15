/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.Callable;

public interface DocumentationTool extends Tool, OptionChecker{
    DocumentationTask getTask(Writer out,
                              JavaFileManager fileManager,
                              DiagnosticListener<? super JavaFileObject> diagnosticListener,
                              Class<?> docletClass,
                              Iterable<String> options,
                              Iterable<? extends JavaFileObject> compilationUnits);

    StandardJavaFileManager getStandardFileManager(
            DiagnosticListener<? super JavaFileObject> diagnosticListener,
            Locale locale,
            Charset charset);

    enum Location implements JavaFileManager.Location{
        DOCUMENTATION_OUTPUT,
        DOCLET_PATH,
        TAGLET_PATH;

        public String getName(){
            return name();
        }

        public boolean isOutputLocation(){
            switch(this){
                case DOCUMENTATION_OUTPUT:
                    return true;
                default:
                    return false;
            }
        }
    }

    interface DocumentationTask extends Callable<Boolean>{
        void setLocale(Locale locale);

        Boolean call();
    }
}
