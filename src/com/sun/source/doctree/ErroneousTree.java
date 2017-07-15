/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.source.doctree;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@jdk.Exported
public interface ErroneousTree extends TextTree{
    Diagnostic<JavaFileObject> getDiagnostic();
}
