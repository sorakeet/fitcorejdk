/**
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

public interface JavaFileObject extends FileObject{
    Kind getKind();

    ;

    boolean isNameCompatible(String simpleName,Kind kind);

    NestingKind getNestingKind();

    Modifier getAccessLevel();

    enum Kind{
        SOURCE(".java"),
        CLASS(".class"),
        HTML(".html"),
        OTHER("");
        public final String extension;

        private Kind(String extension){
            extension.getClass(); // null check
            this.extension=extension;
        }
    }
}
