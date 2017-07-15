package org.omg.PortableInterceptor;

public interface ServerRequestInterceptorOperations extends InterceptorOperations{
    void receive_request_service_contexts(ServerRequestInfo ri) throws ForwardRequest;

    void receive_request(ServerRequestInfo ri) throws ForwardRequest;

    void send_reply(ServerRequestInfo ri);

    void send_exception(ServerRequestInfo ri) throws ForwardRequest;

    void send_other(ServerRequestInfo ri) throws ForwardRequest;
} // interface ServerRequestInterceptorOperations
