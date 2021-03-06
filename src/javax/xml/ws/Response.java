/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import java.util.Map;
import java.util.concurrent.Future;

public interface Response<T> extends Future<T>{
    Map<String,Object> getContext();
}
