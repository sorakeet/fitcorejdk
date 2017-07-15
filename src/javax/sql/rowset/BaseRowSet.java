/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset;

import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialRef;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public abstract class BaseRowSet implements Serializable, Cloneable{
    public static final int UNICODE_STREAM_PARAM=0;
    public static final int BINARY_STREAM_PARAM=1;
    public static final int ASCII_STREAM_PARAM=2;
    static final long serialVersionUID=4886719666485113312L;
    protected InputStream binaryStream;
    protected InputStream unicodeStream;
    protected InputStream asciiStream;
    protected Reader charStream;
    private String command;
    private String URL;
    private String dataSource;
    private transient String username;
    private transient String password;
    private int rowSetType=ResultSet.TYPE_SCROLL_INSENSITIVE;
    private boolean showDeleted=false; // default is false
    private int queryTimeout=0; // default is no timeout
    private int maxRows=0; // default is no limit
    private int maxFieldSize=0; // default is no limit
    private int concurrency=ResultSet.CONCUR_UPDATABLE;
    private boolean readOnly;
    private boolean escapeProcessing=true;
    private int isolation;
    private int fetchDir=ResultSet.FETCH_FORWARD; // default fetch direction
    private int fetchSize=0; // default fetchSize
    private Map<String,Class<?>> map;
    private Vector<RowSetListener> listeners;
    private Hashtable<Integer,Object> params; // could be transient?

    public BaseRowSet(){
        // allocate the listeners collection
        listeners=new Vector<RowSetListener>();
    }
    //--------------------------------------------------------------------
    // Events
    //--------------------------------------------------------------------

    public void addRowSetListener(RowSetListener listener){
        listeners.add(listener);
    }

    public void removeRowSetListener(RowSetListener listener){
        listeners.remove(listener);
    }

    protected void notifyCursorMoved() throws SQLException{
        checkforRowSetInterface();
        if(listeners.isEmpty()==false){
            RowSetEvent event=new RowSetEvent((RowSet)this);
            for(RowSetListener rsl : listeners){
                rsl.cursorMoved(event);
            }
        }
    }

    private void checkforRowSetInterface() throws SQLException{
        if((this instanceof RowSet)==false){
            throw new SQLException("The class extending abstract class BaseRowSet "+
                    "must implement javax.sql.RowSet or one of it's sub-interfaces.");
        }
    }

    protected void notifyRowChanged() throws SQLException{
        checkforRowSetInterface();
        if(listeners.isEmpty()==false){
            RowSetEvent event=new RowSetEvent((RowSet)this);
            for(RowSetListener rsl : listeners){
                rsl.rowChanged(event);
            }
        }
    }

    protected void notifyRowSetChanged() throws SQLException{
        checkforRowSetInterface();
        if(listeners.isEmpty()==false){
            RowSetEvent event=new RowSetEvent((RowSet)this);
            for(RowSetListener rsl : listeners){
                rsl.rowSetChanged(event);
            }
        }
    }

    public String getCommand(){
        return command;
    }

    public void setCommand(String cmd) throws SQLException{
        // cmd equal to null or
        // cmd with length 0 (implies url =="")
        // are not independent events.
        if(cmd==null){
            command=null;
        }else if(cmd.length()==0){
            throw new SQLException("Invalid command string detected. "+
                    "Cannot be of length less than 0");
        }else{
            // "unbind" any parameters from any previous command.
            if(params==null){
                throw new SQLException("Set initParams() before setCommand");
            }
            params.clear();
            command=cmd;
        }
    }

    public String getUrl() throws SQLException{
        return URL;
    }

    public void setUrl(String url) throws SQLException{
        if(url==null){
            url=null;
        }else if(url.length()<1){
            throw new SQLException("Invalid url string detected. "+
                    "Cannot be of length less than 1");
        }else{
            URL=url;
        }
        dataSource=null;
    }

    public String getDataSourceName(){
        return dataSource;
    }

    public void setDataSourceName(String name) throws SQLException{
        if(name==null){
            dataSource=null;
        }else if(name.equals("")){
            throw new SQLException("DataSource name cannot be empty string");
        }else{
            dataSource=name;
        }
        URL=null;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String name){
        if(name==null){
            username=null;
        }else{
            username=name;
        }
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String pass){
        if(pass==null){
            password=null;
        }else{
            password=pass;
        }
    }

    public boolean isReadOnly(){
        return readOnly;
    }

    public void setReadOnly(boolean value){
        readOnly=value;
    }

    public int getTransactionIsolation(){
        return isolation;
    }

    public void setTransactionIsolation(int level) throws SQLException{
        if((level!=Connection.TRANSACTION_NONE)&&
                (level!=Connection.TRANSACTION_READ_COMMITTED)&&
                (level!=Connection.TRANSACTION_READ_UNCOMMITTED)&&
                (level!=Connection.TRANSACTION_REPEATABLE_READ)&&
                (level!=Connection.TRANSACTION_SERIALIZABLE)){
            throw new SQLException("Invalid transaction isolation set. Must "+
                    "be either "+
                    "Connection.TRANSACTION_NONE or "+
                    "Connection.TRANSACTION_READ_UNCOMMITTED or "+
                    "Connection.TRANSACTION_READ_COMMITTED or "+
                    "Connection.RRANSACTION_REPEATABLE_READ or "+
                    "Connection.TRANSACTION_SERIALIZABLE");
        }
        this.isolation=level;
    }

    ;

    public Map<String,Class<?>> getTypeMap(){
        return map;
    }

    public void setTypeMap(Map<String,Class<?>> map){
        this.map=map;
    }

    ;

    public int getMaxFieldSize() throws SQLException{
        return maxFieldSize;
    }

    public void setMaxFieldSize(int max) throws SQLException{
        if(max<0){
            throw new SQLException("Invalid max field size set. Cannot be of "+
                    "value: "+max);
        }
        maxFieldSize=max;
    }

    public int getQueryTimeout() throws SQLException{
        return queryTimeout;
    }

    public void setQueryTimeout(int seconds) throws SQLException{
        if(seconds<0){
            throw new SQLException("Invalid query timeout value set. Cannot be "+
                    "of value: "+seconds);
        }
        this.queryTimeout=seconds;
    }

    public boolean getShowDeleted() throws SQLException{
        return showDeleted;
    }

    public void setShowDeleted(boolean value) throws SQLException{
        showDeleted=value;
    }    public int getMaxRows() throws SQLException{
        return maxRows;
    }

    public boolean getEscapeProcessing() throws SQLException{
        return escapeProcessing;
    }    public void setMaxRows(int max) throws SQLException{
        if(max<0){
            throw new SQLException("Invalid max row size set. Cannot be of "+
                    "value: "+max);
        }else if(max<this.getFetchSize()){
            throw new SQLException("Invalid max row size set. Cannot be less "+
                    "than the fetchSize.");
        }
        this.maxRows=max;
    }

    public void setEscapeProcessing(boolean enable) throws SQLException{
        escapeProcessing=enable;
    }

    public int getFetchDirection() throws SQLException{
        //Added the following code to throw a
        //SQL Exception if the fetchDir is not
        //set properly.Bug id:4914155
        // This checking is not necessary!
        /**
         if((fetchDir != ResultSet.FETCH_FORWARD) &&
         (fetchDir != ResultSet.FETCH_REVERSE) &&
         (fetchDir != ResultSet.FETCH_UNKNOWN)) {
         throw new SQLException("Fetch Direction Invalid");
         }
         */
        return (fetchDir);
    }

    public void setFetchDirection(int direction) throws SQLException{
        // Changed the condition checking to the below as there were two
        // conditions that had to be checked
        // 1. RowSet is TYPE_FORWARD_ONLY and direction is not FETCH_FORWARD
        // 2. Direction is not one of the valid values
        if(((getType()==ResultSet.TYPE_FORWARD_ONLY)&&(direction!=ResultSet.FETCH_FORWARD))||
                ((direction!=ResultSet.FETCH_FORWARD)&&
                        (direction!=ResultSet.FETCH_REVERSE)&&
                        (direction!=ResultSet.FETCH_UNKNOWN))){
            throw new SQLException("Invalid Fetch Direction");
        }
        fetchDir=direction;
    }

    public int getType() throws SQLException{
        return rowSetType;
    }

    public void setType(int type) throws SQLException{
        if((type!=ResultSet.TYPE_FORWARD_ONLY)&&
                (type!=ResultSet.TYPE_SCROLL_INSENSITIVE)&&
                (type!=ResultSet.TYPE_SCROLL_SENSITIVE)){
            throw new SQLException("Invalid type of RowSet set. Must be either "+
                    "ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_INSENSITIVE "+
                    "or ResultSet.TYPE_SCROLL_SENSITIVE.");
        }
        this.rowSetType=type;
    }

    public int getConcurrency() throws SQLException{
        return concurrency;
    }

    public void setConcurrency(int concurrency) throws SQLException{
        if((concurrency!=ResultSet.CONCUR_READ_ONLY)&&
                (concurrency!=ResultSet.CONCUR_UPDATABLE)){
            throw new SQLException("Invalid concurrency set. Must be either "+
                    "ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE.");
        }
        this.concurrency=concurrency;
    }

    public void setNull(int parameterIndex,int sqlType) throws SQLException{
        Object nullVal[];
        checkParamIndex(parameterIndex);
        nullVal=new Object[2];
        nullVal[0]=null;
        nullVal[1]=Integer.valueOf(sqlType);
        if(params==null){
            throw new SQLException("Set initParams() before setNull");
        }
        params.put(Integer.valueOf(parameterIndex-1),nullVal);
    }

    private void checkParamIndex(int idx) throws SQLException{
        if((idx<1)){
            throw new SQLException("Invalid Parameter Index");
        }
    }    public void setFetchSize(int rows) throws SQLException{
        //Added this checking as maxRows can be 0 when this function is called
        //maxRows = 0 means rowset can hold any number of rows, os this checking
        // is needed to take care of this condition.
        if(getMaxRows()==0&&rows>=0){
            fetchSize=rows;
            return;
        }
        if((rows<0)||(rows>getMaxRows())){
            throw new SQLException("Invalid fetch size set. Cannot be of "+
                    "value: "+rows);
        }
        fetchSize=rows;
    }

    public void setNull(int parameterIndex,int sqlType,String typeName)
            throws SQLException{
        Object nullVal[];
        checkParamIndex(parameterIndex);
        nullVal=new Object[3];
        nullVal[0]=null;
        nullVal[1]=Integer.valueOf(sqlType);
        nullVal[2]=typeName;
        if(params==null){
            throw new SQLException("Set initParams() before setNull");
        }
        params.put(Integer.valueOf(parameterIndex-1),nullVal);
    }    public int getFetchSize() throws SQLException{
        return fetchSize;
    }

    public void setBoolean(int parameterIndex,boolean x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setNull");
        }
        params.put(Integer.valueOf(parameterIndex-1),Boolean.valueOf(x));
    }
    //-----------------------------------------------------------------------
    // Parameters
    //-----------------------------------------------------------------------

    public void setByte(int parameterIndex,byte x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setByte");
        }
        params.put(Integer.valueOf(parameterIndex-1),Byte.valueOf(x));
    }
    //---------------------------------------------------------------------
    // setter methods for setting the parameters in a <code>RowSet</code> object's command
    //---------------------------------------------------------------------

    public void setShort(int parameterIndex,short x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setShort");
        }
        params.put(Integer.valueOf(parameterIndex-1),Short.valueOf(x));
    }

    public void setInt(int parameterIndex,int x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setInt");
        }
        params.put(Integer.valueOf(parameterIndex-1),Integer.valueOf(x));
    }

    public void setLong(int parameterIndex,long x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setLong");
        }
        params.put(Integer.valueOf(parameterIndex-1),Long.valueOf(x));
    }

    public void setFloat(int parameterIndex,float x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setFloat");
        }
        params.put(Integer.valueOf(parameterIndex-1),Float.valueOf(x));
    }

    public void setDouble(int parameterIndex,double x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setDouble");
        }
        params.put(Integer.valueOf(parameterIndex-1),Double.valueOf(x));
    }

    public void setBigDecimal(int parameterIndex,BigDecimal x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setBigDecimal");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setString(int parameterIndex,String x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setString");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setBytes(int parameterIndex,byte x[]) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setBytes");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setDate(int parameterIndex,java.sql.Date x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setDate");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setTime(int parameterIndex,Time x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setTime");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setTimestamp(int parameterIndex,Timestamp x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setTimestamp");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setAsciiStream(int parameterIndex,InputStream x,int length) throws SQLException{
        Object asciiStream[];
        checkParamIndex(parameterIndex);
        asciiStream=new Object[3];
        asciiStream[0]=x;
        asciiStream[1]=Integer.valueOf(length);
        asciiStream[2]=Integer.valueOf(ASCII_STREAM_PARAM);
        if(params==null){
            throw new SQLException("Set initParams() before setAsciiStream");
        }
        params.put(Integer.valueOf(parameterIndex-1),asciiStream);
    }

    public void setAsciiStream(int parameterIndex,InputStream x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBinaryStream(int parameterIndex,InputStream x,int length) throws SQLException{
        Object binaryStream[];
        checkParamIndex(parameterIndex);
        binaryStream=new Object[3];
        binaryStream[0]=x;
        binaryStream[1]=Integer.valueOf(length);
        binaryStream[2]=Integer.valueOf(BINARY_STREAM_PARAM);
        if(params==null){
            throw new SQLException("Set initParams() before setBinaryStream");
        }
        params.put(Integer.valueOf(parameterIndex-1),binaryStream);
    }

    public void setBinaryStream(int parameterIndex,InputStream x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex,InputStream x,int length) throws SQLException{
        Object unicodeStream[];
        checkParamIndex(parameterIndex);
        unicodeStream=new Object[3];
        unicodeStream[0]=x;
        unicodeStream[1]=Integer.valueOf(length);
        unicodeStream[2]=Integer.valueOf(UNICODE_STREAM_PARAM);
        if(params==null){
            throw new SQLException("Set initParams() before setUnicodeStream");
        }
        params.put(Integer.valueOf(parameterIndex-1),unicodeStream);
    }

    public void setCharacterStream(int parameterIndex,Reader reader,int length) throws SQLException{
        Object charStream[];
        checkParamIndex(parameterIndex);
        charStream=new Object[2];
        charStream[0]=reader;
        charStream[1]=Integer.valueOf(length);
        if(params==null){
            throw new SQLException("Set initParams() before setCharacterStream");
        }
        params.put(Integer.valueOf(parameterIndex-1),charStream);
    }

    public void setCharacterStream(int parameterIndex,
                                   Reader reader) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setObject(int parameterIndex,Object x,int targetSqlType,int scale) throws SQLException{
        Object obj[];
        checkParamIndex(parameterIndex);
        obj=new Object[3];
        obj[0]=x;
        obj[1]=Integer.valueOf(targetSqlType);
        obj[2]=Integer.valueOf(scale);
        if(params==null){
            throw new SQLException("Set initParams() before setObject");
        }
        params.put(Integer.valueOf(parameterIndex-1),obj);
    }

    public void setObject(int parameterIndex,Object x,int targetSqlType) throws SQLException{
        Object obj[];
        checkParamIndex(parameterIndex);
        obj=new Object[2];
        obj[0]=x;
        obj[1]=Integer.valueOf(targetSqlType);
        if(params==null){
            throw new SQLException("Set initParams() before setObject");
        }
        params.put(Integer.valueOf(parameterIndex-1),obj);
    }

    public void setObject(int parameterIndex,Object x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setObject");
        }
        params.put(Integer.valueOf(parameterIndex-1),x);
    }

    public void setRef(int parameterIndex,Ref ref) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setRef");
        }
        params.put(Integer.valueOf(parameterIndex-1),new SerialRef(ref));
    }

    public void setBlob(int parameterIndex,Blob x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setBlob");
        }
        params.put(Integer.valueOf(parameterIndex-1),new SerialBlob(x));
    }

    public void setClob(int parameterIndex,Clob x) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setClob");
        }
        params.put(Integer.valueOf(parameterIndex-1),new SerialClob(x));
    }

    public void setArray(int parameterIndex,Array array) throws SQLException{
        checkParamIndex(parameterIndex);
        if(params==null){
            throw new SQLException("Set initParams() before setArray");
        }
        params.put(Integer.valueOf(parameterIndex-1),new SerialArray(array));
    }

    public void setDate(int parameterIndex,java.sql.Date x,Calendar cal) throws SQLException{
        Object date[];
        checkParamIndex(parameterIndex);
        date=new Object[2];
        date[0]=x;
        date[1]=cal;
        if(params==null){
            throw new SQLException("Set initParams() before setDate");
        }
        params.put(Integer.valueOf(parameterIndex-1),date);
    }

    public void setTime(int parameterIndex,Time x,Calendar cal) throws SQLException{
        Object time[];
        checkParamIndex(parameterIndex);
        time=new Object[2];
        time[0]=x;
        time[1]=cal;
        if(params==null){
            throw new SQLException("Set initParams() before setTime");
        }
        params.put(Integer.valueOf(parameterIndex-1),time);
    }

    public void setTimestamp(int parameterIndex,Timestamp x,Calendar cal) throws SQLException{
        Object timestamp[];
        checkParamIndex(parameterIndex);
        timestamp=new Object[2];
        timestamp[0]=x;
        timestamp[1]=cal;
        if(params==null){
            throw new SQLException("Set initParams() before setTimestamp");
        }
        params.put(Integer.valueOf(parameterIndex-1),timestamp);
    }

    public void clearParameters() throws SQLException{
        params.clear();
    }

    public Object[] getParams() throws SQLException{
        if(params==null){
            initParams();
            Object[] paramsArray=new Object[params.size()];
            return paramsArray;
        }else{
            // The parameters may be set in random order
            // but all must be set, check to verify all
            // have been set till the last parameter
            // else throw exception.
            Object[] paramsArray=new Object[params.size()];
            for(int i=0;i<params.size();i++){
                paramsArray[i]=params.get(Integer.valueOf(i));
                if(paramsArray[i]==null){
                    throw new SQLException("missing parameter: "+(i+1));
                } //end if
            } //end for
            return paramsArray;
        } //end if
    } //end getParams

    protected void initParams(){
        params=new Hashtable<Integer,Object>();
    }

    public void setNull(String parameterName,int sqlType) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNull(String parameterName,int sqlType,String typeName)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBoolean(String parameterName,boolean x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setByte(String parameterName,byte x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setShort(String parameterName,short x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setInt(String parameterName,int x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setLong(String parameterName,long x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setFloat(String parameterName,float x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setDouble(String parameterName,double x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBigDecimal(String parameterName,BigDecimal x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setString(String parameterName,String x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBytes(String parameterName,byte x[]) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setTimestamp(String parameterName,Timestamp x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setAsciiStream(String parameterName,InputStream x,int length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBinaryStream(String parameterName,InputStream x,
                                int length) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setCharacterStream(String parameterName,
                                   Reader reader,
                                   int length) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setAsciiStream(String parameterName,InputStream x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBinaryStream(String parameterName,InputStream x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setCharacterStream(String parameterName,
                                   Reader reader) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNCharacterStream(int parameterIndex,Reader value) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setObject(String parameterName,Object x,int targetSqlType,int scale)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setObject(String parameterName,Object x,int targetSqlType)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setObject(String parameterName,Object x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBlob(int parameterIndex,InputStream inputStream,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBlob(int parameterIndex,InputStream inputStream)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBlob(String parameterName,InputStream inputStream,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBlob(String parameterName,Blob x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setBlob(String parameterName,InputStream inputStream)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setClob(int parameterIndex,Reader reader,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setClob(int parameterIndex,Reader reader)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setClob(String parameterName,Reader reader,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setClob(String parameterName,Clob x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setClob(String parameterName,Reader reader)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setDate(String parameterName,java.sql.Date x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setDate(String parameterName,java.sql.Date x,Calendar cal)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setTime(String parameterName,Time x)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setTime(String parameterName,Time x,Calendar cal)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setTimestamp(String parameterName,Timestamp x,Calendar cal)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setSQLXML(int parameterIndex,SQLXML xmlObject) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setSQLXML(String parameterName,SQLXML xmlObject) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setRowId(int parameterIndex,RowId x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setRowId(String parameterName,RowId x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNString(int parameterIndex,String value) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNString(String parameterName,String value)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNCharacterStream(int parameterIndex,Reader value,long length) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNCharacterStream(String parameterName,Reader value,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNCharacterStream(String parameterName,Reader value) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(String parameterName,NClob value) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(String parameterName,Reader reader,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(String parameterName,Reader reader)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(int parameterIndex,Reader reader,long length)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(int parameterIndex,NClob value) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setNClob(int parameterIndex,Reader reader)
            throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public void setURL(int parameterIndex,java.net.URL x) throws SQLException{
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }








} //end class
