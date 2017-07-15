/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.namespace;

import javax.xml.XMLConstants;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class QName implements Serializable{
    private static final long serialVersionUID;
    private static final long defaultSerialVersionUID=-9120448754896609940L;
    private static final long compatibleSerialVersionUID=4418622981026545151L;
    private static boolean useDefaultSerialVersionUID=true;

    static{
        try{
            // use a privileged block as reading a system property
            String valueUseCompatibleSerialVersionUID=(String)AccessController.doPrivileged(
                    new PrivilegedAction(){
                        public Object run(){
                            return System.getProperty("com.sun.xml.namespace.QName.useCompatibleSerialVersionUID");
                        }
                    }
            );
            useDefaultSerialVersionUID=(valueUseCompatibleSerialVersionUID!=null&&valueUseCompatibleSerialVersionUID.equals("1.0"))?false:true;
        }catch(Exception exception){
            // use default if any Exceptions
            useDefaultSerialVersionUID=true;
        }
        // set serialVersionUID to desired value
        if(useDefaultSerialVersionUID){
            serialVersionUID=defaultSerialVersionUID;
        }else{
            serialVersionUID=compatibleSerialVersionUID;
        }
    }

    private final String namespaceURI;
    private final String localPart;
    private final String prefix;

    public QName(final String namespaceURI,final String localPart){
        this(namespaceURI,localPart,XMLConstants.DEFAULT_NS_PREFIX);
    }

    public QName(String namespaceURI,String localPart,String prefix){
        // map null Namespace URI to default
        // to preserve compatibility with QName 1.0
        if(namespaceURI==null){
            this.namespaceURI=XMLConstants.NULL_NS_URI;
        }else{
            this.namespaceURI=namespaceURI;
        }
        // local part is required.
        // "" is allowed to preserve compatibility with QName 1.0
        if(localPart==null){
            throw new IllegalArgumentException(
                    "local part cannot be \"null\" when creating a QName");
        }
        this.localPart=localPart;
        // prefix is required
        if(prefix==null){
            throw new IllegalArgumentException(
                    "prefix cannot be \"null\" when creating a QName");
        }
        this.prefix=prefix;
    }

    public QName(String localPart){
        this(
                XMLConstants.NULL_NS_URI,
                localPart,
                XMLConstants.DEFAULT_NS_PREFIX);
    }

    public static QName valueOf(String qNameAsString){
        // null is not valid
        if(qNameAsString==null){
            throw new IllegalArgumentException(
                    "cannot create QName from \"null\" or \"\" String");
        }
        // "" local part is valid to preserve compatible behavior with QName 1.0
        if(qNameAsString.length()==0){
            return new QName(
                    XMLConstants.NULL_NS_URI,
                    qNameAsString,
                    XMLConstants.DEFAULT_NS_PREFIX);
        }
        // local part only?
        if(qNameAsString.charAt(0)!='{'){
            return new QName(
                    XMLConstants.NULL_NS_URI,
                    qNameAsString,
                    XMLConstants.DEFAULT_NS_PREFIX);
        }
        // Namespace URI improperly specified?
        if(qNameAsString.startsWith("{"+XMLConstants.NULL_NS_URI+"}")){
            throw new IllegalArgumentException(
                    "Namespace URI .equals(XMLConstants.NULL_NS_URI), "
                            +".equals(\""+XMLConstants.NULL_NS_URI+"\"), "
                            +"only the local part, "
                            +"\""
                            +qNameAsString.substring(2+XMLConstants.NULL_NS_URI.length())
                            +"\", "
                            +"should be provided.");
        }
        // Namespace URI and local part specified
        int endOfNamespaceURI=qNameAsString.indexOf('}');
        if(endOfNamespaceURI==-1){
            throw new IllegalArgumentException(
                    "cannot create QName from \""
                            +qNameAsString
                            +"\", missing closing \"}\"");
        }
        return new QName(
                qNameAsString.substring(1,endOfNamespaceURI),
                qNameAsString.substring(endOfNamespaceURI+1),
                XMLConstants.DEFAULT_NS_PREFIX);
    }

    public String getNamespaceURI(){
        return namespaceURI;
    }

    public String getLocalPart(){
        return localPart;
    }

    public String getPrefix(){
        return prefix;
    }

    public final int hashCode(){
        return namespaceURI.hashCode()^localPart.hashCode();
    }

    public final boolean equals(Object objectToTest){
        if(objectToTest==this){
            return true;
        }
        if(objectToTest==null||!(objectToTest instanceof QName)){
            return false;
        }
        QName qName=(QName)objectToTest;
        return localPart.equals(qName.localPart)
                &&namespaceURI.equals(qName.namespaceURI);
    }

    public String toString(){
        if(namespaceURI.equals(XMLConstants.NULL_NS_URI)){
            return localPart;
        }else{
            return "{"+namespaceURI+"}"+localPart;
        }
    }
}
