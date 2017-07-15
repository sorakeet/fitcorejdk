/**
 * Copyright (c) 2011, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.org.apache.xalan.internal;

import com.sun.org.apache.xalan.internal.utils.SecuritySupport;

public final class XalanConstants{
    //
    // Constants
    //
    //Xerces security manager
    public static final String SECURITY_MANAGER=
            "http://apache.org/xml/properties/security-manager";
    //
    // Implementation limits: API properties
    //
    public static final String ORACLE_JAXP_PROPERTY_PREFIX=
            "http://www.oracle.com/xml/jaxp/properties/";
    public static final String JDK_ENTITY_EXPANSION_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"entityExpansionLimit";
    public static final String JDK_ELEMENT_ATTRIBUTE_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"elementAttributeLimit";
    public static final String JDK_MAX_OCCUR_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"maxOccurLimit";
    public static final String JDK_TOTAL_ENTITY_SIZE_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"totalEntitySizeLimit";
    public static final String JDK_GENERAL_ENTITY_SIZE_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"maxGeneralEntitySizeLimit";
    public static final String JDK_ENTITY_REPLACEMENT_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"entityReplacementLimit";
    public static final String JDK_PARAMETER_ENTITY_SIZE_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"maxParameterEntitySizeLimit";
    public static final String JDK_XML_NAME_LIMIT=
            ORACLE_JAXP_PROPERTY_PREFIX+"maxXMLNameLimit";
    public static final String JDK_MAX_ELEMENT_DEPTH=
            ORACLE_JAXP_PROPERTY_PREFIX+"maxElementDepth";
    public static final String JDK_ENTITY_COUNT_INFO=
            ORACLE_JAXP_PROPERTY_PREFIX+"getEntityCountInfo";
    //
    // Implementation limits: corresponding System Properties of the above
    // API properties
    //
    public static final String SP_ENTITY_EXPANSION_LIMIT="jdk.xml.entityExpansionLimit";
    public static final String SP_ELEMENT_ATTRIBUTE_LIMIT="jdk.xml.elementAttributeLimit";
    public static final String SP_MAX_OCCUR_LIMIT="jdk.xml.maxOccurLimit";
    public static final String SP_TOTAL_ENTITY_SIZE_LIMIT="jdk.xml.totalEntitySizeLimit";
    public static final String SP_GENERAL_ENTITY_SIZE_LIMIT="jdk.xml.maxGeneralEntitySizeLimit";
    public static final String SP_ENTITY_REPLACEMENT_LIMIT="jdk.xml.entityReplacementLimit";
    public static final String SP_PARAMETER_ENTITY_SIZE_LIMIT="jdk.xml.maxParameterEntitySizeLimit";
    public static final String SP_XML_NAME_LIMIT="jdk.xml.maxXMLNameLimit";
    public static final String SP_MAX_ELEMENT_DEPTH="jdk.xml.maxElementDepth";
    public static final String JDK_EXTENSION_CLASSLOADER="jdk.xml.transform.extensionClassLoader";
    //legacy System Properties
    public final static String ENTITY_EXPANSION_LIMIT="entityExpansionLimit";
    public static final String ELEMENT_ATTRIBUTE_LIMIT="elementAttributeLimit";
    public final static String MAX_OCCUR_LIMIT="maxOccurLimit";
    public static final String JDK_YES="yes";
    // Oracle Feature:
    public static final String ORACLE_FEATURE_SERVICE_MECHANISM="http://www.oracle.com/feature/use-service-mechanism";
    //System Properties corresponding to ACCESS_EXTERNAL_* properties
    public static final String SP_ACCESS_EXTERNAL_STYLESHEET="javax.xml.accessExternalStylesheet";
    public static final String SP_ACCESS_EXTERNAL_DTD="javax.xml.accessExternalDTD";
    //all access keyword
    public static final String ACCESS_EXTERNAL_ALL="all";
    public static final String EXTERNAL_ACCESS_DEFAULT_FSP="";
    public static final String EXTERNAL_ACCESS_DEFAULT=ACCESS_EXTERNAL_ALL;
    public static final String XML_SECURITY_PROPERTY_MANAGER=
            ORACLE_JAXP_PROPERTY_PREFIX+"xmlSecurityPropertyManager";
    public static final String ORACLE_ENABLE_EXTENSION_FUNCTION=
            ORACLE_JAXP_PROPERTY_PREFIX+"enableExtensionFunctions";
    public static final String SP_ORACLE_ENABLE_EXTENSION_FUNCTION="javax.xml.enableExtensionFunctions";
    public static final String FEATURE_TRUE="true";
    public static final String FEATURE_FALSE="false";
    public static final boolean IS_JDK8_OR_ABOVE=isJavaVersionAtLeast(8);

    public static boolean isJavaVersionAtLeast(int compareTo){
        String javaVersion=SecuritySupport.getSystemProperty("java.version");
        String versions[]=javaVersion.split("\\.",3);
        if(Integer.parseInt(versions[0])>=compareTo||
                Integer.parseInt(versions[1])>=compareTo){
            return true;
        }
        return false;
    }
} // class Constants
