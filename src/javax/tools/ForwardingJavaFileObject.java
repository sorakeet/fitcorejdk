/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

public class ForwardingJavaFileObject<F extends JavaFileObject>
        extends ForwardingFileObject<F>
        implements JavaFileObject{
    protected ForwardingJavaFileObject(F fileObject){
        super(fileObject);
    }

    public Kind getKind(){
        return fileObject.getKind();
    }

    public boolean isNameCompatible(String simpleName,Kind kind){
        return fileObject.isNameCompatible(simpleName,kind);
    }

    public NestingKind getNestingKind(){
        return fileObject.getNestingKind();
    }

    public Modifier getAccessLevel(){
        return fileObject.getAccessLevel();
    }
}
