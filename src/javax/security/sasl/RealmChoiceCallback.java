/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.sasl;

import javax.security.auth.callback.ChoiceCallback;

public class RealmChoiceCallback extends ChoiceCallback{
    private static final long serialVersionUID=-8588141348846281332L;

    public RealmChoiceCallback(String prompt,String[] choices,
                               int defaultChoice,boolean multiple){
        super(prompt,choices,defaultChoice,multiple);
    }
}
