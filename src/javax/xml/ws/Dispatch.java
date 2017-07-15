/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import java.util.concurrent.Future;

public interface Dispatch<T> extends BindingProvider{
    public T invoke(T msg);

    public Response<T> invokeAsync(T msg);

    public Future<?> invokeAsync(T msg,AsyncHandler<T> handler);

    public void invokeOneWay(T msg);
}
