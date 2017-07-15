/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.spi.Provider;
import java.io.StringWriter;

@XmlTransient // to treat this class like Object as far as databinding is concerned (proposed JAXB 2.1 feature)
public abstract class EndpointReference{
    //
    //Default constructor to be only called by derived types.
    //
    protected EndpointReference(){
    }

    public static EndpointReference readFrom(Source eprInfoset){
        return Provider.provider().readEndpointReference(eprInfoset);
    }

    public <T> T getPort(Class<T> serviceEndpointInterface,
                         WebServiceFeature... features){
        return Provider.provider().getPort(this,serviceEndpointInterface,
                features);
    }

    public String toString(){
        StringWriter w=new StringWriter();
        writeTo(new StreamResult(w));
        return w.toString();
    }

    public abstract void writeTo(Result result);
}
