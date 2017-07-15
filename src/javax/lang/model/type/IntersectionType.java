/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.lang.model.type;

import java.util.List;

public interface IntersectionType extends TypeMirror{
    List<? extends TypeMirror> getBounds();
}
