/**
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;

import static javax.lang.model.SourceVersion.RELEASE_8;

@SupportedSourceVersion(RELEASE_8)
public class ElementScanner8<R,P> extends ElementScanner7<R,P>{
    protected ElementScanner8(){
        super(null);
    }

    protected ElementScanner8(R defaultValue){
        super(defaultValue);
    }
}
