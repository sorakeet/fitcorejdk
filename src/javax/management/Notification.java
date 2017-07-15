/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.util.EventObject;

@SuppressWarnings("serial")  // serialVersionUID is not constant
public class Notification extends EventObject{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=1716977971058914352L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-7516092053498031989L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("message",String.class),
                    new ObjectStreamField("sequenceNumber",Long.TYPE),
                    new ObjectStreamField("source",Object.class),
                    new ObjectStreamField("sourceObjectName",ObjectName.class),
                    new ObjectStreamField("timeStamp",Long.TYPE),
                    new ObjectStreamField("type",String.class),
                    new ObjectStreamField("userData",Object.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("message",String.class),
                    new ObjectStreamField("sequenceNumber",Long.TYPE),
                    new ObjectStreamField("source",Object.class),
                    new ObjectStreamField("timeStamp",Long.TYPE),
                    new ObjectStreamField("type",String.class),
                    new ObjectStreamField("userData",Object.class)
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

    protected Object source=null;
    //
    // END Serialization compatibility stuff
    private String type;
    private long sequenceNumber;
    private long timeStamp;
    private Object userData=null;
    private String message="";

    public Notification(String type,Object source,long sequenceNumber){
        super(source);
        this.source=source;
        this.type=type;
        this.sequenceNumber=sequenceNumber;
        this.timeStamp=(new java.util.Date()).getTime();
    }

    public Notification(String type,Object source,long sequenceNumber,String message){
        super(source);
        this.source=source;
        this.type=type;
        this.sequenceNumber=sequenceNumber;
        this.timeStamp=(new java.util.Date()).getTime();
        this.message=message;
    }

    public Notification(String type,Object source,long sequenceNumber,long timeStamp){
        super(source);
        this.source=source;
        this.type=type;
        this.sequenceNumber=sequenceNumber;
        this.timeStamp=timeStamp;
    }

    public Notification(String type,Object source,long sequenceNumber,long timeStamp,String message){
        super(source);
        this.source=source;
        this.type=type;
        this.sequenceNumber=sequenceNumber;
        this.timeStamp=timeStamp;
        this.message=message;
    }

    public void setSource(Object source){
        super.source=source;
        this.source=source;
    }

    public long getSequenceNumber(){
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber){
        this.sequenceNumber=sequenceNumber;
    }

    public String getType(){
        return type;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp){
        this.timeStamp=timeStamp;
    }

    public String getMessage(){
        return message;
    }

    public Object getUserData(){
        return userData;
    }

    public void setUserData(Object userData){
        this.userData=userData;
    }

    @Override
    public String toString(){
        return super.toString()+"[type="+type+"][message="+message+"]";
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // New serial form ignores extra field "sourceObjectName"
        in.defaultReadObject();
        super.source=source;
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        if(compat){
            // Serializes this instance in the old serial form
            //
            ObjectOutputStream.PutField fields=out.putFields();
            fields.put("type",type);
            fields.put("sequenceNumber",sequenceNumber);
            fields.put("timeStamp",timeStamp);
            fields.put("userData",userData);
            fields.put("message",message);
            fields.put("source",source);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
