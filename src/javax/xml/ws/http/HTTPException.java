/**
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws.http;

public class HTTPException extends javax.xml.ws.ProtocolException{
    private int statusCode;

    public HTTPException(int statusCode){
        super();
        this.statusCode=statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
