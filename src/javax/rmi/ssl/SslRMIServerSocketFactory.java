/**
 * Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.rmi.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Arrays;
import java.util.List;

public class SslRMIServerSocketFactory implements RMIServerSocketFactory{
    // We use a static field because:
    //
    //    SSLSocketFactory.getDefault() always returns the same object
    //    (at least on Sun's implementation), and we want to make sure
    //    that the Javadoc & the implementation stay in sync.
    //
    // If someone needs to have different SslRMIServerSocketFactory
    // factories with different underlying SSLSocketFactory objects
    // using different keystores and truststores, he/she can always
    // use the constructor that takes an SSLContext as input.
    //
    private static SSLSocketFactory defaultSSLSocketFactory=null;
    private final String[] enabledCipherSuites;
    private final String[] enabledProtocols;
    private final boolean needClientAuth;
    private List<String> enabledCipherSuitesList;
    private List<String> enabledProtocolsList;
    private SSLContext context;

    public SslRMIServerSocketFactory(){
        this(null,null,false);
    }

    public SslRMIServerSocketFactory(
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean needClientAuth)
            throws IllegalArgumentException{
        this(null,enabledCipherSuites,enabledProtocols,needClientAuth);
    }

    public SslRMIServerSocketFactory(
            SSLContext context,
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean needClientAuth)
            throws IllegalArgumentException{
        // Initialize the configuration parameters.
        //
        this.enabledCipherSuites=enabledCipherSuites==null?
                null:enabledCipherSuites.clone();
        this.enabledProtocols=enabledProtocols==null?
                null:enabledProtocols.clone();
        this.needClientAuth=needClientAuth;
        // Force the initialization of the default at construction time,
        // rather than delaying it to the first time createServerSocket()
        // is called.
        //
        this.context=context;
        final SSLSocketFactory sslSocketFactory=
                context==null?
                        getDefaultSSLSocketFactory():context.getSocketFactory();
        SSLSocket sslSocket=null;
        if(this.enabledCipherSuites!=null||this.enabledProtocols!=null){
            try{
                sslSocket=(SSLSocket)sslSocketFactory.createSocket();
            }catch(Exception e){
                final String msg="Unable to check if the cipher suites "+
                        "and protocols to enable are supported";
                throw (IllegalArgumentException)
                        new IllegalArgumentException(msg).initCause(e);
            }
        }
        // Check if all the cipher suites and protocol versions to enable
        // are supported by the underlying SSL/TLS implementation and if
        // true create lists from arrays.
        //
        if(this.enabledCipherSuites!=null){
            sslSocket.setEnabledCipherSuites(this.enabledCipherSuites);
            enabledCipherSuitesList=Arrays.asList(this.enabledCipherSuites);
        }
        if(this.enabledProtocols!=null){
            sslSocket.setEnabledProtocols(this.enabledProtocols);
            enabledProtocolsList=Arrays.asList(this.enabledProtocols);
        }
    }

    private static synchronized SSLSocketFactory getDefaultSSLSocketFactory(){
        if(defaultSSLSocketFactory==null)
            defaultSSLSocketFactory=
                    (SSLSocketFactory)SSLSocketFactory.getDefault();
        return defaultSSLSocketFactory;
    }

    public final String[] getEnabledCipherSuites(){
        return enabledCipherSuites==null?
                null:enabledCipherSuites.clone();
    }

    public final String[] getEnabledProtocols(){
        return enabledProtocols==null?
                null:enabledProtocols.clone();
    }

    public final boolean getNeedClientAuth(){
        return needClientAuth;
    }

    public ServerSocket createServerSocket(int port) throws IOException{
        final SSLSocketFactory sslSocketFactory=
                context==null?
                        getDefaultSSLSocketFactory():context.getSocketFactory();
        return new ServerSocket(port){
            public Socket accept() throws IOException{
                Socket socket=super.accept();
                SSLSocket sslSocket=(SSLSocket)sslSocketFactory.createSocket(
                        socket,socket.getInetAddress().getHostName(),
                        socket.getPort(),true);
                sslSocket.setUseClientMode(false);
                if(enabledCipherSuites!=null){
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                }
                if(enabledProtocols!=null){
                    sslSocket.setEnabledProtocols(enabledProtocols);
                }
                sslSocket.setNeedClientAuth(needClientAuth);
                return sslSocket;
            }
        };
    }

    public int hashCode(){
        return getClass().hashCode()+
                (context==null?0:context.hashCode())+
                (needClientAuth?Boolean.TRUE.hashCode():Boolean.FALSE.hashCode())+
                (enabledCipherSuites==null?0:enabledCipherSuitesList.hashCode())+
                (enabledProtocols==null?0:enabledProtocolsList.hashCode());
    }

    public boolean equals(Object obj){
        if(obj==null) return false;
        if(obj==this) return true;
        if(!(obj instanceof SslRMIServerSocketFactory))
            return false;
        SslRMIServerSocketFactory that=(SslRMIServerSocketFactory)obj;
        return (getClass().equals(that.getClass())&&checkParameters(that));
    }

    private boolean checkParameters(SslRMIServerSocketFactory that){
        // SSL context
        //
        if(context==null?that.context!=null:!context.equals(that.context))
            return false;
        // needClientAuth flag
        //
        if(needClientAuth!=that.needClientAuth)
            return false;
        // enabledCipherSuites
        //
        if((enabledCipherSuites==null&&that.enabledCipherSuites!=null)||
                (enabledCipherSuites!=null&&that.enabledCipherSuites==null))
            return false;
        if(enabledCipherSuites!=null&&that.enabledCipherSuites!=null){
            List<String> thatEnabledCipherSuitesList=
                    Arrays.asList(that.enabledCipherSuites);
            if(!enabledCipherSuitesList.equals(thatEnabledCipherSuitesList))
                return false;
        }
        // enabledProtocols
        //
        if((enabledProtocols==null&&that.enabledProtocols!=null)||
                (enabledProtocols!=null&&that.enabledProtocols==null))
            return false;
        if(enabledProtocols!=null&&that.enabledProtocols!=null){
            List<String> thatEnabledProtocolsList=
                    Arrays.asList(that.enabledProtocols);
            if(!enabledProtocolsList.equals(thatEnabledProtocolsList))
                return false;
        }
        return true;
    }
}
