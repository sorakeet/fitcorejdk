/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.sasl;

import javax.security.auth.callback.TextInputCallback;

public class RealmCallback extends TextInputCallback{
    private static final long serialVersionUID=-4342673378785456908L;

    public RealmCallback(String prompt){
        super(prompt);
    }

    public RealmCallback(String prompt,String defaultRealmInfo){
        super(prompt,defaultRealmInfo);
    }
}
