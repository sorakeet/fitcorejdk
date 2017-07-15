/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.net;

// There are no abstract methods, but to be useful the user must
// subclass.
public abstract class Authenticator{
    // The system-wide authenticator object.  See setDefault().
    private static Authenticator theAuthenticator;
    private String requestingHost;
    private InetAddress requestingSite;
    private int requestingPort;
    private String requestingProtocol;
    private String requestingPrompt;
    private String requestingScheme;
    private URL requestingURL;
    private RequestorType requestingAuthType;

    public synchronized static void setDefault(Authenticator a){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            NetPermission setDefaultPermission
                    =new NetPermission("setDefaultAuthenticator");
            sm.checkPermission(setDefaultPermission);
        }
        theAuthenticator=a;
    }

    public static PasswordAuthentication requestPasswordAuthentication(
            InetAddress addr,
            int port,
            String protocol,
            String prompt,
            String scheme){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            NetPermission requestPermission
                    =new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        Authenticator a=theAuthenticator;
        if(a==null){
            return null;
        }else{
            synchronized(a){
                a.reset();
                a.requestingSite=addr;
                a.requestingPort=port;
                a.requestingProtocol=protocol;
                a.requestingPrompt=prompt;
                a.requestingScheme=scheme;
                return a.getPasswordAuthentication();
            }
        }
    }

    public static PasswordAuthentication requestPasswordAuthentication(
            String host,
            InetAddress addr,
            int port,
            String protocol,
            String prompt,
            String scheme){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            NetPermission requestPermission
                    =new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        Authenticator a=theAuthenticator;
        if(a==null){
            return null;
        }else{
            synchronized(a){
                a.reset();
                a.requestingHost=host;
                a.requestingSite=addr;
                a.requestingPort=port;
                a.requestingProtocol=protocol;
                a.requestingPrompt=prompt;
                a.requestingScheme=scheme;
                return a.getPasswordAuthentication();
            }
        }
    }

    public static PasswordAuthentication requestPasswordAuthentication(
            String host,
            InetAddress addr,
            int port,
            String protocol,
            String prompt,
            String scheme,
            URL url,
            RequestorType reqType){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            NetPermission requestPermission
                    =new NetPermission("requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        Authenticator a=theAuthenticator;
        if(a==null){
            return null;
        }else{
            synchronized(a){
                a.reset();
                a.requestingHost=host;
                a.requestingSite=addr;
                a.requestingPort=port;
                a.requestingProtocol=protocol;
                a.requestingPrompt=prompt;
                a.requestingScheme=scheme;
                a.requestingURL=url;
                a.requestingAuthType=reqType;
                return a.getPasswordAuthentication();
            }
        }
    }

    private void reset(){
        requestingHost=null;
        requestingSite=null;
        requestingPort=-1;
        requestingProtocol=null;
        requestingPrompt=null;
        requestingScheme=null;
        requestingURL=null;
        requestingAuthType=RequestorType.SERVER;
    }

    protected final String getRequestingHost(){
        return requestingHost;
    }

    protected final InetAddress getRequestingSite(){
        return requestingSite;
    }

    protected final int getRequestingPort(){
        return requestingPort;
    }

    protected final String getRequestingProtocol(){
        return requestingProtocol;
    }

    protected final String getRequestingPrompt(){
        return requestingPrompt;
    }

    protected final String getRequestingScheme(){
        return requestingScheme;
    }

    protected PasswordAuthentication getPasswordAuthentication(){
        return null;
    }

    protected URL getRequestingURL(){
        return requestingURL;
    }

    protected RequestorType getRequestorType(){
        return requestingAuthType;
    }

    public enum RequestorType{
        PROXY,
        SERVER
    }
}
