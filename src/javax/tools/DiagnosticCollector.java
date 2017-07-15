/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiagnosticCollector<S> implements DiagnosticListener<S>{
    private List<Diagnostic<? extends S>> diagnostics=
            Collections.synchronizedList(new ArrayList<Diagnostic<? extends S>>());

    public void report(Diagnostic<? extends S> diagnostic){
        diagnostic.getClass(); // null check
        diagnostics.add(diagnostic);
    }

    public List<Diagnostic<? extends S>> getDiagnostics(){
        return Collections.unmodifiableList(diagnostics);
    }
}
