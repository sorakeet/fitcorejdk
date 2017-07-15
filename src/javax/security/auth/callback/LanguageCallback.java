/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.callback;

import java.util.Locale;

public class LanguageCallback implements Callback, java.io.Serializable{
    private static final long serialVersionUID=2019050433478903213L;
    private Locale locale;

    public LanguageCallback(){
    }

    public Locale getLocale(){
        return locale;
    }

    public void setLocale(Locale locale){
        this.locale=locale;
    }
}
