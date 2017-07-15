/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import javax.xml.namespace.NamespaceContext;

final public class DatatypeConverter{
    private final static JAXBPermission SET_DATATYPE_CONVERTER_PERMISSION=
            new JAXBPermission("setDatatypeConverter");
    // delegate to this instance of DatatypeConverter
    private static volatile DatatypeConverterInterface theConverter=null;

    private DatatypeConverter(){
        // private constructor
    }

    public static void setDatatypeConverter(DatatypeConverterInterface converter){
        if(converter==null){
            throw new IllegalArgumentException(
                    Messages.format(Messages.CONVERTER_MUST_NOT_BE_NULL));
        }else if(theConverter==null){
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null)
                sm.checkPermission(SET_DATATYPE_CONVERTER_PERMISSION);
            theConverter=converter;
        }
    }

    public static String parseString(String lexicalXSDString){
        if(theConverter==null) initConverter();
        return theConverter.parseString(lexicalXSDString);
    }

    private static synchronized void initConverter(){
        theConverter=new DatatypeConverterImpl();
    }

    public static java.math.BigInteger parseInteger(String lexicalXSDInteger){
        if(theConverter==null) initConverter();
        return theConverter.parseInteger(lexicalXSDInteger);
    }

    public static int parseInt(String lexicalXSDInt){
        if(theConverter==null) initConverter();
        return theConverter.parseInt(lexicalXSDInt);
    }

    public static long parseLong(String lexicalXSDLong){
        if(theConverter==null) initConverter();
        return theConverter.parseLong(lexicalXSDLong);
    }

    public static short parseShort(String lexicalXSDShort){
        if(theConverter==null) initConverter();
        return theConverter.parseShort(lexicalXSDShort);
    }

    public static java.math.BigDecimal parseDecimal(String lexicalXSDDecimal){
        if(theConverter==null) initConverter();
        return theConverter.parseDecimal(lexicalXSDDecimal);
    }

    public static float parseFloat(String lexicalXSDFloat){
        if(theConverter==null) initConverter();
        return theConverter.parseFloat(lexicalXSDFloat);
    }

    public static double parseDouble(String lexicalXSDDouble){
        if(theConverter==null) initConverter();
        return theConverter.parseDouble(lexicalXSDDouble);
    }

    public static boolean parseBoolean(String lexicalXSDBoolean){
        if(theConverter==null) initConverter();
        return theConverter.parseBoolean(lexicalXSDBoolean);
    }

    public static byte parseByte(String lexicalXSDByte){
        if(theConverter==null) initConverter();
        return theConverter.parseByte(lexicalXSDByte);
    }

    public static javax.xml.namespace.QName parseQName(String lexicalXSDQName,
                                                       NamespaceContext nsc){
        if(theConverter==null) initConverter();
        return theConverter.parseQName(lexicalXSDQName,nsc);
    }

    public static java.util.Calendar parseDateTime(String lexicalXSDDateTime){
        if(theConverter==null) initConverter();
        return theConverter.parseDateTime(lexicalXSDDateTime);
    }

    public static byte[] parseBase64Binary(String lexicalXSDBase64Binary){
        if(theConverter==null) initConverter();
        return theConverter.parseBase64Binary(lexicalXSDBase64Binary);
    }

    public static byte[] parseHexBinary(String lexicalXSDHexBinary){
        if(theConverter==null) initConverter();
        return theConverter.parseHexBinary(lexicalXSDHexBinary);
    }

    public static long parseUnsignedInt(String lexicalXSDUnsignedInt){
        if(theConverter==null) initConverter();
        return theConverter.parseUnsignedInt(lexicalXSDUnsignedInt);
    }

    public static int parseUnsignedShort(String lexicalXSDUnsignedShort){
        if(theConverter==null) initConverter();
        return theConverter.parseUnsignedShort(lexicalXSDUnsignedShort);
    }

    public static java.util.Calendar parseTime(String lexicalXSDTime){
        if(theConverter==null) initConverter();
        return theConverter.parseTime(lexicalXSDTime);
    }

    public static java.util.Calendar parseDate(String lexicalXSDDate){
        if(theConverter==null) initConverter();
        return theConverter.parseDate(lexicalXSDDate);
    }

    public static String parseAnySimpleType(String lexicalXSDAnySimpleType){
        if(theConverter==null) initConverter();
        return theConverter.parseAnySimpleType(lexicalXSDAnySimpleType);
    }
    // also indicate the print methods produce a lexical
    // representation for given Java datatypes.

    public static String printString(String val){
        if(theConverter==null) initConverter();
        return theConverter.printString(val);
    }

    public static String printInteger(java.math.BigInteger val){
        if(theConverter==null) initConverter();
        return theConverter.printInteger(val);
    }

    public static String printInt(int val){
        if(theConverter==null) initConverter();
        return theConverter.printInt(val);
    }

    public static String printLong(long val){
        if(theConverter==null) initConverter();
        return theConverter.printLong(val);
    }

    public static String printShort(short val){
        if(theConverter==null) initConverter();
        return theConverter.printShort(val);
    }

    public static String printDecimal(java.math.BigDecimal val){
        if(theConverter==null) initConverter();
        return theConverter.printDecimal(val);
    }

    public static String printFloat(float val){
        if(theConverter==null) initConverter();
        return theConverter.printFloat(val);
    }

    public static String printDouble(double val){
        if(theConverter==null) initConverter();
        return theConverter.printDouble(val);
    }

    public static String printBoolean(boolean val){
        if(theConverter==null) initConverter();
        return theConverter.printBoolean(val);
    }

    public static String printByte(byte val){
        if(theConverter==null) initConverter();
        return theConverter.printByte(val);
    }

    public static String printQName(javax.xml.namespace.QName val,
                                    NamespaceContext nsc){
        if(theConverter==null) initConverter();
        return theConverter.printQName(val,nsc);
    }

    public static String printDateTime(java.util.Calendar val){
        if(theConverter==null) initConverter();
        return theConverter.printDateTime(val);
    }

    public static String printBase64Binary(byte[] val){
        if(theConverter==null) initConverter();
        return theConverter.printBase64Binary(val);
    }

    public static String printHexBinary(byte[] val){
        if(theConverter==null) initConverter();
        return theConverter.printHexBinary(val);
    }

    public static String printUnsignedInt(long val){
        if(theConverter==null) initConverter();
        return theConverter.printUnsignedInt(val);
    }

    public static String printUnsignedShort(int val){
        if(theConverter==null) initConverter();
        return theConverter.printUnsignedShort(val);
    }

    public static String printTime(java.util.Calendar val){
        if(theConverter==null) initConverter();
        return theConverter.printTime(val);
    }

    public static String printDate(java.util.Calendar val){
        if(theConverter==null) initConverter();
        return theConverter.printDate(val);
    }

    public static String printAnySimpleType(String val){
        if(theConverter==null) initConverter();
        return theConverter.printAnySimpleType(val);
    }
}
