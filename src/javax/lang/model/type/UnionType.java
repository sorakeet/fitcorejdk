/**
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

import java.util.List;

public interface UnionType extends TypeMirror{
    List<? extends TypeMirror> getAlternatives();
}
