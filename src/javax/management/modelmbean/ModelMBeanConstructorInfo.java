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

import javax.management.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MODELMBEAN_LOGGER;

@SuppressWarnings("serial")  // serialVersionUID is not constant
public class ModelMBeanConstructorInfo
        extends MBeanConstructorInfo
        implements DescriptorAccess{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-4440125391095574518L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=3862947819818064362L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("consDescriptor",Descriptor.class),
                    new ObjectStreamField("currClass",String.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("consDescriptor",Descriptor.class)
            };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private final static String currClass="ModelMBeanConstructorInfo";
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
    private Descriptor consDescriptor=validDescriptor(null);

    public ModelMBeanConstructorInfo(String description,
                                     Constructor<?> constructorMethod){
        super(description,constructorMethod);
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "ModelMBeanConstructorInfo(String,Constructor)",
                    "Entry");
        }
        consDescriptor=validDescriptor(null);
        // put getter and setter methods in constructors list
        // create default descriptor
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
            clone.setField("descriptorType","operation");
            MODELMBEAN_LOGGER.finer("Defaulting descriptorType to \"operation\"");
        }
        if(clone.getFieldValue("displayName")==null){
            clone.setField("displayName",this.getName());
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor displayName to "+this.getName());
        }
        if(clone.getFieldValue("role")==null){
            clone.setField("role","constructor");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor role field to \"constructor\"");
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
        if(!"operation".equalsIgnoreCase((String)clone.getFieldValue("descriptorType"))){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"descriptorType\" field does not match the object described. "+
                            " Expected: \"operation\" ,"+" was: "+clone.getFieldValue("descriptorType"));
        }
        if(!((String)clone.getFieldValue("role")).equalsIgnoreCase("constructor")){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"role\" field does not match the object described. "+
                            " Expected: \"constructor\" ,"+" was: "+clone.getFieldValue("role"));
        }
        return clone;
    }

    public ModelMBeanConstructorInfo(String description,
                                     Constructor<?> constructorMethod,
                                     Descriptor descriptor){
        super(description,constructorMethod);
        // put getter and setter methods in constructors list
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "ModelMBeanConstructorInfo("+
                            "String,Constructor,Descriptor)","Entry");
        }
        consDescriptor=validDescriptor(descriptor);
    }

    public ModelMBeanConstructorInfo(String name,
                                     String description,
                                     MBeanParameterInfo[] signature){
        super(name,description,signature);
        // create default descriptor
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "ModelMBeanConstructorInfo("+
                            "String,String,MBeanParameterInfo[])","Entry");
        }
        consDescriptor=validDescriptor(null);
    }

    public ModelMBeanConstructorInfo(String name,
                                     String description,
                                     MBeanParameterInfo[] signature,
                                     Descriptor descriptor){
        super(name,description,signature);
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "ModelMBeanConstructorInfo("+
                            "String,String,MBeanParameterInfo[],Descriptor)",
                    "Entry");
        }
        consDescriptor=validDescriptor(descriptor);
    }

    ModelMBeanConstructorInfo(ModelMBeanConstructorInfo old){
        super(old.getName(),old.getDescription(),old.getSignature());
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "ModelMBeanConstructorInfo("+
                            "ModelMBeanConstructorInfo)","Entry");
        }
        consDescriptor=validDescriptor(consDescriptor);
    }

    @Override
    public Object clone(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "clone()","Entry");
        }
        return (new ModelMBeanConstructorInfo(this));
    }

    @Override
    public String toString(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "toString()","Entry");
        }
        String retStr=
                "ModelMBeanConstructorInfo: "+this.getName()+
                        " ; Description: "+this.getDescription()+
                        " ; Descriptor: "+this.getDescriptor()+
                        " ; Signature: ";
        MBeanParameterInfo[] pTypes=this.getSignature();
        for(int i=0;i<pTypes.length;i++){
            retStr=retStr.concat((pTypes[i]).getType()+", ");
        }
        return retStr;
    }

    @Override
    public Descriptor getDescriptor(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "getDescriptor()","Entry");
        }
        if(consDescriptor==null){
            consDescriptor=validDescriptor(null);
        }
        return ((Descriptor)consDescriptor.clone());
    }

    public void setDescriptor(Descriptor inDescriptor){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanConstructorInfo.class.getName(),
                    "setDescriptor()","Entry");
        }
        consDescriptor=validDescriptor(inDescriptor);
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
            fields.put("consDescriptor",consDescriptor);
            fields.put("currClass",currClass);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
