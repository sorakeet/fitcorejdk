/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.serial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

public class SerialRef implements Ref, Serializable, Cloneable{
    static final long serialVersionUID=-4727123500609662274L;
    private String baseTypeName;
    private Object object;
    private Ref reference;

    public SerialRef(Ref ref) throws SerialException, SQLException{
        if(ref==null){
            throw new SQLException("Cannot instantiate a SerialRef object "+
                    "with a null Ref object");
        }
        reference=ref;
        object=ref;
        if(ref.getBaseTypeName()==null){
            throw new SQLException("Cannot instantiate a SerialRef object "+
                    "that returns a null base type name");
        }else{
            baseTypeName=ref.getBaseTypeName();
        }
    }

    public String getBaseTypeName() throws SerialException{
        return baseTypeName;
    }

    public Object getObject(Map<String,Class<?>> map)
            throws SerialException{
        map=new Hashtable<String,Class<?>>(map);
        if(object!=null){
            return map.get(object);
        }else{
            throw new SerialException("The object is not set");
        }
    }

    public Object getObject() throws SerialException{
        if(reference!=null){
            try{
                return reference.getObject();
            }catch(SQLException e){
                throw new SerialException("SQLException: "+e.getMessage());
            }
        }
        if(object!=null){
            return object;
        }
        throw new SerialException("The object is not set");
    }

    public void setObject(Object obj) throws SerialException{
        try{
            reference.setObject(obj);
        }catch(SQLException e){
            throw new SerialException("SQLException: "+e.getMessage());
        }
        object=obj;
    }

    public int hashCode(){
        return (31+object.hashCode())*31+baseTypeName.hashCode();
    }

    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj instanceof SerialRef){
            SerialRef ref=(SerialRef)obj;
            return baseTypeName.equals(ref.baseTypeName)&&
                    object.equals(ref.object);
        }
        return false;
    }

    public Object clone(){
        try{
            SerialRef ref=(SerialRef)super.clone();
            ref.reference=null;
            return ref;
        }catch(CloneNotSupportedException ex){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField fields=s.readFields();
        object=fields.get("object",null);
        baseTypeName=(String)fields.get("baseTypeName",null);
        reference=(Ref)fields.get("reference",null);
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException, ClassNotFoundException{
        ObjectOutputStream.PutField fields=s.putFields();
        fields.put("baseTypeName",baseTypeName);
        fields.put("object",object);
        // Note: this check to see if it is an instance of Serializable
        // is for backwards compatibiity
        fields.put("reference",reference instanceof Serializable?reference:null);
        s.writeFields();
    }
}
