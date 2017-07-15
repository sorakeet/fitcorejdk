/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public interface ParameterMetaData extends Wrapper{
    int parameterNoNulls=0;
    int parameterNullable=1;
    int parameterNullableUnknown=2;
    int parameterModeUnknown=0;
    int parameterModeIn=1;
    int parameterModeInOut=2;
    int parameterModeOut=4;

    int getParameterCount() throws SQLException;

    int isNullable(int param) throws SQLException;

    boolean isSigned(int param) throws SQLException;

    int getPrecision(int param) throws SQLException;

    int getScale(int param) throws SQLException;

    int getParameterType(int param) throws SQLException;

    String getParameterTypeName(int param) throws SQLException;

    String getParameterClassName(int param) throws SQLException;

    int getParameterMode(int param) throws SQLException;
}
