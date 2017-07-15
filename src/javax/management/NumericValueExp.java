/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;

@SuppressWarnings("serial")
        // serialVersionUID not constant
class NumericValueExp extends QueryEval implements ValueExp{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-6227876276058904000L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-4679739485102359104L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("longVal",Long.TYPE),
                    new ObjectStreamField("doubleVal",Double.TYPE),
                    new ObjectStreamField("valIsLong",Boolean.TYPE)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("val",Number.class)
            };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private static boolean compat=false;

    static{
        try{
            GetPropertyAction act=new GetPropertyAction("jmx.serial.form");
            String form=AccessController.doPrivileged(act);
            compat=(form!=null&&form.equals("1.0"));
        }catch(Exception e){
            // OK: exception means no compat with 1.0, too bad
        }
        if(compat){
            serialPersistentFields=oldSerialPersistentFields;
            serialVersionUID=oldSerialVersionUID;
        }else{
            serialPersistentFields=newSerialPersistentFields;
            serialVersionUID=newSerialVersionUID;
        }
    }

    private Number val=0.0;
    //
    // END Serialization compatibility stuff

    public NumericValueExp(){
    }

    NumericValueExp(Number val){
        this.val=val;
    }

    public String toString(){
        if(val==null)
            return "null";
        if(val instanceof Long||val instanceof Integer){
            return Long.toString(val.longValue());
        }
        double d=val.doubleValue();
        if(Double.isInfinite(d))
            return (d>0)?"(1.0 / 0.0)":"(-1.0 / 0.0)";
        if(Double.isNaN(d))
            return "(0.0 / 0.0)";
        return Double.toString(d);
    }

    public ValueExp apply(ObjectName name)
            throws BadStringOperationException, BadBinaryOpValueExpException,
            BadAttributeValueExpException, InvalidApplicationException{
        return this;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            double doubleVal;
            long longVal;
            boolean isLong;
            ObjectInputStream.GetField fields=in.readFields();
            doubleVal=fields.get("doubleVal",(double)0);
            if(fields.defaulted("doubleVal")){
                throw new NullPointerException("doubleVal");
            }
            longVal=fields.get("longVal",(long)0);
            if(fields.defaulted("longVal")){
                throw new NullPointerException("longVal");
            }
            isLong=fields.get("valIsLong",false);
            if(fields.defaulted("valIsLong")){
                throw new NullPointerException("valIsLong");
            }
            if(isLong){
                this.val=longVal;
            }else{
                this.val=doubleVal;
            }
        }else{
            // Read an object serialized in the new serial form
            //
            in.defaultReadObject();
        }
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        if(compat){
            // Serializes this instance in the old serial form
            //
            ObjectOutputStream.PutField fields=out.putFields();
            fields.put("doubleVal",doubleValue());
            fields.put("longVal",longValue());
            fields.put("valIsLong",isLong());
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }

    public double doubleValue(){
        if(val instanceof Long||val instanceof Integer){
            return (double)(val.longValue());
        }
        return val.doubleValue();
    }

    public long longValue(){
        if(val instanceof Long||val instanceof Integer){
            return val.longValue();
        }
        return (long)(val.doubleValue());
    }

    public boolean isLong(){
        return (val instanceof Long||val instanceof Integer);
    }

    @Deprecated
    public void setMBeanServer(MBeanServer s){
        super.setMBeanServer(s);
    }
}
