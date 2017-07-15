/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.serial;

import sun.reflect.misc.ReflectUtil;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;

public class SQLInputImpl implements SQLInput{
    private boolean lastValueWasNull;
    private int idx;
    private Object attrib[];
    private Map<String,Class<?>> map;

    public SQLInputImpl(Object[] attributes,Map<String,Class<?>> map)
            throws SQLException{
        if((attributes==null)||(map==null)){
            throw new SQLException("Cannot instantiate a SQLInputImpl "+
                    "object with null parameters");
        }
        // assign our local reference to the attribute stream
        attrib=Arrays.copyOf(attributes,attributes.length);
        // init the index point before the head of the stream
        idx=-1;
        // set the map
        this.map=map;
    }

    public String readString() throws SQLException{
        return (String)getNextAttribute();
    }
    //================================================================
    // Methods for reading attributes from the stream of SQL data.
    // These methods correspond to the column-accessor methods of
    // java.sql.ResultSet.
    //================================================================

    private Object getNextAttribute() throws SQLException{
        if(++idx>=attrib.length){
            throw new SQLException("SQLInputImpl exception: Invalid read "+
                    "position");
        }else{
            lastValueWasNull=attrib[idx]==null;
            return attrib[idx];
        }
    }

    public boolean readBoolean() throws SQLException{
        Boolean attrib=(Boolean)getNextAttribute();
        return (attrib==null)?false:attrib.booleanValue();
    }

    public byte readByte() throws SQLException{
        Byte attrib=(Byte)getNextAttribute();
        return (attrib==null)?0:attrib.byteValue();
    }

    public short readShort() throws SQLException{
        Short attrib=(Short)getNextAttribute();
        return (attrib==null)?0:attrib.shortValue();
    }

    public int readInt() throws SQLException{
        Integer attrib=(Integer)getNextAttribute();
        return (attrib==null)?0:attrib.intValue();
    }

    public long readLong() throws SQLException{
        Long attrib=(Long)getNextAttribute();
        return (attrib==null)?0:attrib.longValue();
    }

    public float readFloat() throws SQLException{
        Float attrib=(Float)getNextAttribute();
        return (attrib==null)?0:attrib.floatValue();
    }

    public double readDouble() throws SQLException{
        Double attrib=(Double)getNextAttribute();
        return (attrib==null)?0:attrib.doubleValue();
    }

    public java.math.BigDecimal readBigDecimal() throws SQLException{
        return (java.math.BigDecimal)getNextAttribute();
    }

    public byte[] readBytes() throws SQLException{
        return (byte[])getNextAttribute();
    }

    public Date readDate() throws SQLException{
        return (Date)getNextAttribute();
    }

    public Time readTime() throws SQLException{
        return (Time)getNextAttribute();
    }

    public Timestamp readTimestamp() throws SQLException{
        return (Timestamp)getNextAttribute();
    }

    public java.io.Reader readCharacterStream() throws SQLException{
        return (java.io.Reader)getNextAttribute();
    }

    public java.io.InputStream readAsciiStream() throws SQLException{
        return (java.io.InputStream)getNextAttribute();
    }

    public java.io.InputStream readBinaryStream() throws SQLException{
        return (java.io.InputStream)getNextAttribute();
    }
    //================================================================
    // Methods for reading items of SQL user-defined types from the stream.
    //================================================================

    public Object readObject() throws SQLException{
        Object attrib=getNextAttribute();
        if(attrib instanceof Struct){
            Struct s=(Struct)attrib;
            // look up the class in the map
            Class<?> c=map.get(s.getSQLTypeName());
            if(c!=null){
                // create new instance of the class
                SQLData obj=null;
                try{
                    obj=(SQLData)ReflectUtil.newInstance(c);
                }catch(Exception ex){
                    throw new SQLException("Unable to Instantiate: ",ex);
                }
                // get the attributes from the struct
                Object attribs[]=s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput=new SQLInputImpl(attribs,map);
                // read the values...
                obj.readSQL(sqlInput,s.getSQLTypeName());
                return obj;
            }
        }
        return attrib;
    }

    public Ref readRef() throws SQLException{
        return (Ref)getNextAttribute();
    }

    public Blob readBlob() throws SQLException{
        return (Blob)getNextAttribute();
    }

    public Clob readClob() throws SQLException{
        return (Clob)getNextAttribute();
    }

    public Array readArray() throws SQLException{
        return (Array)getNextAttribute();
    }

    public boolean wasNull() throws SQLException{
        return lastValueWasNull;
    }

    public java.net.URL readURL() throws SQLException{
        return (java.net.URL)getNextAttribute();
    }
    //---------------------------- JDBC 4.0 -------------------------

    public NClob readNClob() throws SQLException{
        return (NClob)getNextAttribute();
    }

    public String readNString() throws SQLException{
        return (String)getNextAttribute();
    }

    public SQLXML readSQLXML() throws SQLException{
        return (SQLXML)getNextAttribute();
    }

    public RowId readRowId() throws SQLException{
        return (RowId)getNextAttribute();
    }
}
