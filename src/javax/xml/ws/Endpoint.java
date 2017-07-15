/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.ws;

import org.w3c.dom.Element;

import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.http.HttpContext;
import java.util.List;
import java.util.Map;

public abstract class Endpoint{
    public static final String WSDL_SERVICE="javax.xml.ws.wsdl.service";
    public static final String WSDL_PORT="javax.xml.ws.wsdl.port";

    public static Endpoint create(Object implementor){
        return create(null,implementor);
    }

    public static Endpoint create(String bindingId,Object implementor){
        return Provider.provider().createEndpoint(bindingId,implementor);
    }

    public static Endpoint create(Object implementor,WebServiceFeature... features){
        return create(null,implementor,features);
    }

    public static Endpoint create(String bindingId,Object implementor,WebServiceFeature... features){
        return Provider.provider().createEndpoint(bindingId,implementor,features);
    }

    public static Endpoint publish(String address,Object implementor){
        return Provider.provider().createAndPublishEndpoint(address,implementor);
    }

    public static Endpoint publish(String address,Object implementor,WebServiceFeature... features){
        return Provider.provider().createAndPublishEndpoint(address,implementor,features);
    }

    public abstract Binding getBinding();

    public abstract Object getImplementor();

    public abstract void publish(String address);

    public abstract void publish(Object serverContext);

    public void publish(HttpContext serverContext){
        throw new UnsupportedOperationException("JAX-WS 2.2 implementation must override this default behaviour.");
    }

    public abstract void stop();

    public abstract boolean isPublished();

    public abstract List<javax.xml.transform.Source> getMetadata();

    public abstract void setMetadata(List<javax.xml.transform.Source> metadata);

    public abstract java.util.concurrent.Executor getExecutor();

    public abstract void setExecutor(java.util.concurrent.Executor executor);

    public abstract Map<String,Object> getProperties();

    public abstract void setProperties(Map<String,Object> properties);

    public abstract EndpointReference getEndpointReference(Element... referenceParameters);

    public abstract <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
                                                                         Element... referenceParameters);

    public void setEndpointContext(EndpointContext ctxt){
        throw new UnsupportedOperationException("JAX-WS 2.2 implementation must override this default behaviour.");
    }
}
