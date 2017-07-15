/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class DataTruncation extends SQLWarning{
    private static final long serialVersionUID=6464298989504059473L;
    private int index;
    private boolean parameter;
    private boolean read;
    private int dataSize;
    private int transferSize;

    public DataTruncation(int index,boolean parameter,
                          boolean read,int dataSize,
                          int transferSize){
        super("Data truncation",read==true?"01004":"22001");
        this.index=index;
        this.parameter=parameter;
        this.read=read;
        this.dataSize=dataSize;
        this.transferSize=transferSize;
    }

    public DataTruncation(int index,boolean parameter,
                          boolean read,int dataSize,
                          int transferSize,Throwable cause){
        super("Data truncation",read==true?"01004":"22001",cause);
        this.index=index;
        this.parameter=parameter;
        this.read=read;
        this.dataSize=dataSize;
        this.transferSize=transferSize;
    }

    public int getIndex(){
        return index;
    }

    public boolean getParameter(){
        return parameter;
    }

    public boolean getRead(){
        return read;
    }

    public int getDataSize(){
        return dataSize;
    }

    public int getTransferSize(){
        return transferSize;
    }
}
