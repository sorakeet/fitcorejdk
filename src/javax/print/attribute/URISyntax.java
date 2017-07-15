/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute;

import java.io.Serializable;
import java.net.URI;

public abstract class URISyntax implements Serializable, Cloneable{
    private static final long serialVersionUID=-7842661210486401678L;
    private URI uri;

    protected URISyntax(URI uri){
        this.uri=verify(uri);
    }

    private static URI verify(URI uri){
        if(uri==null){
            throw new NullPointerException(" uri is null");
        }
        return uri;
    }

    public URI getURI(){
        return uri;
    }

    public int hashCode(){
        return uri.hashCode();
    }

    public boolean equals(Object object){
        return (object!=null&&
                object instanceof URISyntax&&
                this.uri.equals(((URISyntax)object).uri));
    }

    public String toString(){
        return uri.toString();
    }
}
