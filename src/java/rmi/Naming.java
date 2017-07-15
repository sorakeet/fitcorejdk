/**
 * Copyright (c) 1996, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Naming{
    private Naming(){
    }

    public static Remote lookup(String name)
            throws NotBoundException,
            MalformedURLException,
            RemoteException{
        ParsedNamingURL parsed=parseURL(name);
        Registry registry=getRegistry(parsed);
        if(parsed.name==null)
            return registry;
        return registry.lookup(parsed.name);
    }

    private static Registry getRegistry(ParsedNamingURL parsed)
            throws RemoteException{
        return LocateRegistry.getRegistry(parsed.host,parsed.port);
    }

    private static ParsedNamingURL parseURL(String str)
            throws MalformedURLException{
        try{
            return intParseURL(str);
        }catch(URISyntaxException ex){
            /** With RFC 3986 URI handling, 'rmi://:<port>' and
             * '//:<port>' forms will result in a URI syntax exception
             * Convert the authority to a localhost:<port> form
             */
            MalformedURLException mue=new MalformedURLException(
                    "invalid URL String: "+str);
            mue.initCause(ex);
            int indexSchemeEnd=str.indexOf(':');
            int indexAuthorityBegin=str.indexOf("//:");
            if(indexAuthorityBegin<0){
                throw mue;
            }
            if((indexAuthorityBegin==0)||
                    ((indexSchemeEnd>0)&&
                            (indexAuthorityBegin==indexSchemeEnd+1))){
                int indexHostBegin=indexAuthorityBegin+2;
                String newStr=str.substring(0,indexHostBegin)+
                        "localhost"+
                        str.substring(indexHostBegin);
                try{
                    return intParseURL(newStr);
                }catch(URISyntaxException inte){
                    throw mue;
                }catch(MalformedURLException inte){
                    throw inte;
                }
            }
            throw mue;
        }
    }

    private static ParsedNamingURL intParseURL(String str)
            throws MalformedURLException, URISyntaxException{
        URI uri=new URI(str);
        if(uri.isOpaque()){
            throw new MalformedURLException(
                    "not a hierarchical URL: "+str);
        }
        if(uri.getFragment()!=null){
            throw new MalformedURLException(
                    "invalid character, '#', in URL name: "+str);
        }else if(uri.getQuery()!=null){
            throw new MalformedURLException(
                    "invalid character, '?', in URL name: "+str);
        }else if(uri.getUserInfo()!=null){
            throw new MalformedURLException(
                    "invalid character, '@', in URL host: "+str);
        }
        String scheme=uri.getScheme();
        if(scheme!=null&&!scheme.equals("rmi")){
            throw new MalformedURLException("invalid URL scheme: "+str);
        }
        String name=uri.getPath();
        if(name!=null){
            if(name.startsWith("/")){
                name=name.substring(1);
            }
            if(name.length()==0){
                name=null;
            }
        }
        String host=uri.getHost();
        if(host==null){
            host="";
            try{
                /**
                 * With 2396 URI handling, forms such as 'rmi://host:bar'
                 * or 'rmi://:<port>' are parsed into a registry based
                 * authority. We only want to allow server based naming
                 * authorities.
                 */
                uri.parseServerAuthority();
            }catch(URISyntaxException use){
                // Check if the authority is of form ':<port>'
                String authority=uri.getAuthority();
                if(authority!=null&&authority.startsWith(":")){
                    // Convert the authority to 'localhost:<port>' form
                    authority="localhost"+authority;
                    try{
                        uri=new URI(null,authority,null,null,null);
                        // Make sure it now parses to a valid server based
                        // naming authority
                        uri.parseServerAuthority();
                    }catch(URISyntaxException use2){
                        throw new
                                MalformedURLException("invalid authority: "+str);
                    }
                }else{
                    throw new
                            MalformedURLException("invalid authority: "+str);
                }
            }
        }
        int port=uri.getPort();
        if(port==-1){
            port=Registry.REGISTRY_PORT;
        }
        return new ParsedNamingURL(host,port,name);
    }

    public static void bind(String name,Remote obj)
            throws AlreadyBoundException,
            MalformedURLException,
            RemoteException{
        ParsedNamingURL parsed=parseURL(name);
        Registry registry=getRegistry(parsed);
        if(obj==null)
            throw new NullPointerException("cannot bind to null");
        registry.bind(parsed.name,obj);
    }

    public static void unbind(String name)
            throws RemoteException,
            NotBoundException,
            MalformedURLException{
        ParsedNamingURL parsed=parseURL(name);
        Registry registry=getRegistry(parsed);
        registry.unbind(parsed.name);
    }

    public static void rebind(String name,Remote obj)
            throws RemoteException, MalformedURLException{
        ParsedNamingURL parsed=parseURL(name);
        Registry registry=getRegistry(parsed);
        if(obj==null)
            throw new NullPointerException("cannot bind to null");
        registry.rebind(parsed.name,obj);
    }

    public static String[] list(String name)
            throws RemoteException, MalformedURLException{
        ParsedNamingURL parsed=parseURL(name);
        Registry registry=getRegistry(parsed);
        String prefix="";
        if(parsed.port>0||!parsed.host.equals(""))
            prefix+="//"+parsed.host;
        if(parsed.port>0)
            prefix+=":"+parsed.port;
        prefix+="/";
        String[] names=registry.list();
        for(int i=0;i<names.length;i++){
            names[i]=prefix+names[i];
        }
        return names;
    }

    private static class ParsedNamingURL{
        String host;
        int port;
        String name;

        ParsedNamingURL(String host,int port,String name){
            this.host=host;
            this.port=port;
            this.name=name;
        }
    }
}
