/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream;

public class XMLStreamException extends Exception{
    protected Throwable nested;
    protected Location location;

    public XMLStreamException(){
        super();
    }

    public XMLStreamException(String msg){
        super(msg);
    }

    public XMLStreamException(Throwable th){
        super(th);
        nested=th;
    }

    public XMLStreamException(String msg,Throwable th){
        super(msg,th);
        nested=th;
    }

    public XMLStreamException(String msg,Location location,Throwable th){
        super("ParseError at [row,col]:["+location.getLineNumber()+","+
                location.getColumnNumber()+"]\n"+
                "Message: "+msg);
        nested=th;
        this.location=location;
    }

    public XMLStreamException(String msg,
                              Location location){
        super("ParseError at [row,col]:["+location.getLineNumber()+","+
                location.getColumnNumber()+"]\n"+
                "Message: "+msg);
        this.location=location;
    }

    public Throwable getNestedException(){
        return nested;
    }

    public Location getLocation(){
        return location;
    }
}
