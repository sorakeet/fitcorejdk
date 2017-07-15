/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.util.Locale;

public interface Diagnostic<S>{
    public final static long NOPOS=-1;

    Kind getKind();

    S getSource();

    long getPosition();

    long getStartPosition();

    long getEndPosition();

    long getLineNumber();

    long getColumnNumber();

    String getCode();

    String getMessage(Locale locale);

    enum Kind{
        ERROR,
        WARNING,
        MANDATORY_WARNING,
        NOTE,
        OTHER,
    }
}
