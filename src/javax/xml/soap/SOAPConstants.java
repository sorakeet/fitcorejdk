/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import javax.xml.namespace.QName;

public interface SOAPConstants{
    public static final String DYNAMIC_SOAP_PROTOCOL="Dynamic Protocol";
    public static final String SOAP_1_1_PROTOCOL="SOAP 1.1 Protocol";
    public static final String SOAP_1_2_PROTOCOL="SOAP 1.2 Protocol";
    public static final String DEFAULT_SOAP_PROTOCOL=SOAP_1_1_PROTOCOL;
    public static final String
            URI_NS_SOAP_1_1_ENVELOPE="http://schemas.xmlsoap.org/soap/envelope/";
    public static final String
            URI_NS_SOAP_1_2_ENVELOPE="http://www.w3.org/2003/05/soap-envelope";
    public static final String
            URI_NS_SOAP_ENVELOPE=URI_NS_SOAP_1_1_ENVELOPE;
    public static final String
            URI_NS_SOAP_ENCODING="http://schemas.xmlsoap.org/soap/encoding/";
    public static final String
            URI_NS_SOAP_1_2_ENCODING="http://www.w3.org/2003/05/soap-encoding";
    public static final String
            SOAP_1_1_CONTENT_TYPE="text/xml";
    public static final String
            SOAP_1_2_CONTENT_TYPE="application/soap+xml";
    public static final String
            URI_SOAP_ACTOR_NEXT="http://schemas.xmlsoap.org/soap/actor/next";
    public static final String
            URI_SOAP_1_2_ROLE_NEXT=URI_NS_SOAP_1_2_ENVELOPE+"/role/next";
    public static final String
            URI_SOAP_1_2_ROLE_NONE=URI_NS_SOAP_1_2_ENVELOPE+"/role/none";
    public static final String
            URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER=
            URI_NS_SOAP_1_2_ENVELOPE+"/role/ultimateReceiver";
    public static final String SOAP_ENV_PREFIX="env";
    public static final QName SOAP_VERSIONMISMATCH_FAULT=
            new QName(URI_NS_SOAP_1_2_ENVELOPE,"VersionMismatch",SOAP_ENV_PREFIX);
    public static final QName SOAP_MUSTUNDERSTAND_FAULT=
            new QName(URI_NS_SOAP_1_2_ENVELOPE,"MustUnderstand",SOAP_ENV_PREFIX);
    public static final QName SOAP_DATAENCODINGUNKNOWN_FAULT=
            new QName(URI_NS_SOAP_1_2_ENVELOPE,"DataEncodingUnknown",SOAP_ENV_PREFIX);
    public static final QName SOAP_SENDER_FAULT=
            new QName(URI_NS_SOAP_1_2_ENVELOPE,"Sender",SOAP_ENV_PREFIX);
    public static final QName SOAP_RECEIVER_FAULT=
            new QName(URI_NS_SOAP_1_2_ENVELOPE,"Receiver",SOAP_ENV_PREFIX);
}
