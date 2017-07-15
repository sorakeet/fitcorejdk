package org.omg.PortableInterceptor;

public interface ClientRequestInterceptorOperations extends InterceptorOperations{
    void send_request(ClientRequestInfo ri) throws ForwardRequest;

    void send_poll(ClientRequestInfo ri);

    void receive_reply(ClientRequestInfo ri);

    void receive_exception(ClientRequestInfo ri) throws ForwardRequest;

    void receive_other(ClientRequestInfo ri) throws ForwardRequest;
} // interface ClientRequestInterceptorOperations
