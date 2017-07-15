/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author IBM Corp.
 * <p>
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
/**
 * @author IBM Corp.
 *
 * Copyright IBM Corp. 1999-2000.  All rights reserved.
 */
package javax.management.modelmbean;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;

@SuppressWarnings("serial")  // serialVersionUID not constant
public class InvalidTargetObjectTypeException extends Exception{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=3711724570458346634L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=1190536278266811217L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("msgStr",String.class),
                    new ObjectStreamField("relatedExcept",Exception.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("exception",Exception.class)
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
            // OK: No compat with 1.0
        }
        if(compat){
            serialPersistentFields=oldSerialPersistentFields;
            serialVersionUID=oldSerialVersionUID;
        }else{
            serialPersistentFields=newSerialPersistentFields;
            serialVersionUID=newSerialVersionUID;
        }
    }
    //
    // END Serialization compatibility stuff
    Exception exception;

    public InvalidTargetObjectTypeException(){
        super("InvalidTargetObjectTypeException: ");
        exception=null;
    }

    public InvalidTargetObjectTypeException(String s){
        super("InvalidTargetObjectTypeException: "+s);
        exception=null;
    }

    public InvalidTargetObjectTypeException(Exception e,String s){
        super("InvalidTargetObjectTypeException: "+
                s+
                ((e!=null)?("\n\t triggered by:"+e.toString()):""));
        exception=e;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            exception=(Exception)fields.get("relatedExcept",null);
            if(fields.defaulted("relatedExcept")){
                throw new NullPointerException("relatedExcept");
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
            fields.put("relatedExcept",exception);
            fields.put("msgStr",((exception!=null)?exception.getMessage():""));
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
