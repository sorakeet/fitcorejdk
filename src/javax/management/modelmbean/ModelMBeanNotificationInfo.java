/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanNotificationInfo;
import javax.management.RuntimeOperationsException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MODELMBEAN_LOGGER;

@SuppressWarnings("serial")  // serialVersionUID is not constant
public class ModelMBeanNotificationInfo
        extends MBeanNotificationInfo
        implements DescriptorAccess{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form
    // depends on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-5211564525059047097L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=-7445681389570207141L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("notificationDescriptor",Descriptor.class),
                    new ObjectStreamField("currClass",String.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("notificationDescriptor",Descriptor.class)
            };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private static final String currClass="ModelMBeanNotificationInfo";
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
    private Descriptor notificationDescriptor;

    public ModelMBeanNotificationInfo(String[] notifTypes,
                                      String name,
                                      String description){
        this(notifTypes,name,description,null);
    }

    public ModelMBeanNotificationInfo(String[] notifTypes,
                                      String name,
                                      String description,
                                      Descriptor descriptor){
        super(notifTypes,name,description);
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "ModelMBeanNotificationInfo","Entry");
        }
        notificationDescriptor=validDescriptor(descriptor);
    }

    private Descriptor validDescriptor(final Descriptor in) throws RuntimeOperationsException{
        Descriptor clone;
        boolean defaulted=(in==null);
        if(defaulted){
            clone=new DescriptorSupport();
            MODELMBEAN_LOGGER.finer("Null Descriptor, creating new.");
        }else{
            clone=(Descriptor)in.clone();
        }
        //Setting defaults.
        if(defaulted&&clone.getFieldValue("name")==null){
            clone.setField("name",this.getName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor name to "+this.getName());
        }
        if(defaulted&&clone.getFieldValue("descriptorType")==null){
            clone.setField("descriptorType","notification");
            MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"notification\"");
        }
        if(clone.getFieldValue("displayName")==null){
            clone.setField("displayName",this.getName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to "+this.getName());
        }
        if(clone.getFieldValue("severity")==null){
            clone.setField("severity","6");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor severity field to 6");
        }
        //Checking validity
        if(!clone.isValid()){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The isValid() method of the Descriptor object itself returned false,"+
                            "one or more required fields are invalid. Descriptor:"+clone.toString());
        }
        if(!getName().equalsIgnoreCase((String)clone.getFieldValue("name"))){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"name\" field does not match the object described. "+
                            " Expected: "+this.getName()+" , was: "+clone.getFieldValue("name"));
        }
        if(!"notification".equalsIgnoreCase((String)clone.getFieldValue("descriptorType"))){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"descriptorType\" field does not match the object described. "+
                            " Expected: \"notification\" ,"+" was: "+clone.getFieldValue("descriptorType"));
        }
        return clone;
    }

    public ModelMBeanNotificationInfo(ModelMBeanNotificationInfo inInfo){
        this(inInfo.getNotifTypes(),
                inInfo.getName(),
                inInfo.getDescription(),inInfo.getDescriptor());
    }

    public Object clone(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "clone()","Entry");
        }
        return (new ModelMBeanNotificationInfo(this));
    }

    public String toString(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "toString()","Entry");
        }
        final StringBuilder retStr=new StringBuilder();
        retStr.append("ModelMBeanNotificationInfo: ")
                .append(this.getName());
        retStr.append(" ; Description: ")
                .append(this.getDescription());
        retStr.append(" ; Descriptor: ")
                .append(this.getDescriptor());
        retStr.append(" ; Types: ");
        String[] nTypes=this.getNotifTypes();
        for(int i=0;i<nTypes.length;i++){
            if(i>0) retStr.append(", ");
            retStr.append(nTypes[i]);
        }
        return retStr.toString();
    }

    public Descriptor getDescriptor(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "getDescriptor()","Entry");
        }
        if(notificationDescriptor==null){
            // Dead code. Should never happen.
            if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
                MODELMBEAN_LOGGER.logp(Level.FINER,
                        ModelMBeanNotificationInfo.class.getName(),
                        "getDescriptor()","Descriptor value is null, "+
                                "setting descriptor to default values");
            }
            notificationDescriptor=validDescriptor(null);
        }
        return ((Descriptor)notificationDescriptor.clone());
    }

    public void setDescriptor(Descriptor inDescriptor){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanNotificationInfo.class.getName(),
                    "setDescriptor(Descriptor)","Entry");
        }
        notificationDescriptor=validDescriptor(inDescriptor);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // New serial form ignores extra field "currClass"
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        if(compat){
            // Serializes this instance in the old serial form
            //
            ObjectOutputStream.PutField fields=out.putFields();
            fields.put("notificationDescriptor",notificationDescriptor);
            fields.put("currClass",currClass);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
