/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public interface DatatypeConverterInterface{
    public String parseString(String lexicalXSDString);

    public java.math.BigInteger parseInteger(String lexicalXSDInteger);

    public int parseInt(String lexicalXSDInt);

    public long parseLong(String lexicalXSDLong);

    public short parseShort(String lexicalXSDShort);

    public java.math.BigDecimal parseDecimal(String lexicalXSDDecimal);

    public float parseFloat(String lexicalXSDFloat);

    public double parseDouble(String lexicalXSDDouble);

    public boolean parseBoolean(String lexicalXSDBoolean);

    public byte parseByte(String lexicalXSDByte);

    public javax.xml.namespace.QName parseQName(String lexicalXSDQName,
                                                javax.xml.namespace.NamespaceContext nsc);

    public java.util.Calendar parseDateTime(String lexicalXSDDateTime);

    public byte[] parseBase64Binary(String lexicalXSDBase64Binary);

    public byte[] parseHexBinary(String lexicalXSDHexBinary);

    public long parseUnsignedInt(String lexicalXSDUnsignedInt);

    public int parseUnsignedShort(String lexicalXSDUnsignedShort);

    public java.util.Calendar parseTime(String lexicalXSDTime);

    public java.util.Calendar parseDate(String lexicalXSDDate);

    public String parseAnySimpleType(String lexicalXSDAnySimpleType);

    public String printString(String val);

    public String printInteger(java.math.BigInteger val);

    public String printInt(int val);

    public String printLong(long val);

    public String printShort(short val);

    public String printDecimal(java.math.BigDecimal val);

    public String printFloat(float val);

    public String printDouble(double val);

    public String printBoolean(boolean val);

    public String printByte(byte val);

    public String printQName(javax.xml.namespace.QName val,
                             javax.xml.namespace.NamespaceContext nsc);

    public String printDateTime(java.util.Calendar val);

    public String printBase64Binary(byte[] val);

    public String printHexBinary(byte[] val);

    public String printUnsignedInt(long val);

    public String printUnsignedShort(int val);

    public String printTime(java.util.Calendar val);

    public String printDate(java.util.Calendar val);

    public String printAnySimpleType(String val);
}
