/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.serial;

import java.io.Serializable;
import java.net.URL;

public class SerialDatalink implements Serializable, Cloneable{
    static final long serialVersionUID=2826907821828733626L;
    private URL url;
    private int baseType;
    private String baseTypeName;

    public SerialDatalink(URL url) throws SerialException{
        if(url==null){
            throw new SerialException("Cannot serialize empty URL instance");
        }
        this.url=url;
    }

    public URL getDatalink() throws SerialException{
        URL aURL=null;
        try{
            aURL=new URL((this.url).toString());
        }catch(java.net.MalformedURLException e){
            throw new SerialException("MalformedURLException: "+e.getMessage());
        }
        return aURL;
    }

    public int hashCode(){
        return 31+url.hashCode();
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof SerialDatalink){
            SerialDatalink sdl=(SerialDatalink)obj;
            return url.equals(sdl.url);
        }
        return false;
    }

    public Object clone(){
        try{
            SerialDatalink sdl=(SerialDatalink)super.clone();
            return sdl;
        }catch(CloneNotSupportedException ex){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
