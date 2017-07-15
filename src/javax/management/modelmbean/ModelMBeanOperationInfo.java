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
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MODELMBEAN_LOGGER;

@SuppressWarnings("serial")  // serialVersionUID is not constant
public class ModelMBeanOperationInfo extends MBeanOperationInfo
        implements DescriptorAccess{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=9087646304346171239L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=6532732096650090465L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("operationDescriptor",Descriptor.class),
                    new ObjectStreamField("currClass",String.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("operationDescriptor",Descriptor.class)
            };
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    private static final String currClass="ModelMBeanOperationInfo";
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
    private Descriptor operationDescriptor=validDescriptor(null);

    public ModelMBeanOperationInfo(String description,
                                   Method operationMethod){
        super(description,operationMethod);
        // create default descriptor
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "ModelMBeanOperationInfo(String,Method)",
                    "Entry");
        }
        operationDescriptor=validDescriptor(null);
    }

    private Descriptor validDescriptor(final Descriptor in)
            throws RuntimeOperationsException{
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
            clone.setField("role","operation");
            MODELMBEAN_LOGGER.finer("Defaulting Descriptor role field to \"operation\"");
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
        final String role=(String)clone.getFieldValue("role");
        if(!(role.equalsIgnoreCase("operation")||
                role.equalsIgnoreCase("setter")||
                role.equalsIgnoreCase("getter"))){
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                    "The Descriptor \"role\" field does not match the object described. "+
                            " Expected: \"operation\", \"setter\", or \"getter\" ,"+" was: "+clone.getFieldValue("role"));
        }
        final Object targetValue=clone.getFieldValue("targetType");
        if(targetValue!=null){
            if(!(targetValue instanceof String)){
                throw new RuntimeOperationsException(new IllegalArgumentException("Invalid Descriptor argument"),
                        "The Descriptor field \"targetValue\" is invalid class. "+
                                " Expected: java.lang.String, "+" was: "+targetValue.getClass().getName());
            }
        }
        return clone;
    }

    public ModelMBeanOperationInfo(String description,
                                   Method operationMethod,
                                   Descriptor descriptor){
        super(description,operationMethod);
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "ModelMBeanOperationInfo(String,Method,Descriptor)",
                    "Entry");
        }
        operationDescriptor=validDescriptor(descriptor);
    }

    public ModelMBeanOperationInfo(String name,
                                   String description,
                                   MBeanParameterInfo[] signature,
                                   String type,
                                   int impact){
        super(name,description,signature,type,impact);
        // create default descriptor
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "ModelMBeanOperationInfo("+
                            "String,String,MBeanParameterInfo[],String,int)",
                    "Entry");
        }
        operationDescriptor=validDescriptor(null);
    }

    public ModelMBeanOperationInfo(String name,
                                   String description,
                                   MBeanParameterInfo[] signature,
                                   String type,
                                   int impact,
                                   Descriptor descriptor){
        super(name,description,signature,type,impact);
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "ModelMBeanOperationInfo(String,String,"+
                            "MBeanParameterInfo[],String,int,Descriptor)",
                    "Entry");
        }
        operationDescriptor=validDescriptor(descriptor);
    }

    public ModelMBeanOperationInfo(ModelMBeanOperationInfo inInfo){
        super(inInfo.getName(),
                inInfo.getDescription(),
                inInfo.getSignature(),
                inInfo.getReturnType(),
                inInfo.getImpact());
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "ModelMBeanOperationInfo(ModelMBeanOperationInfo)",
                    "Entry");
        }
        Descriptor newDesc=inInfo.getDescriptor();
        operationDescriptor=validDescriptor(newDesc);
    }

    public Object clone(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "clone()","Entry");
        }
        return (new ModelMBeanOperationInfo(this));
    }

    public String toString(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "toString()","Entry");
        }
        String retStr=
                "ModelMBeanOperationInfo: "+this.getName()+
                        " ; Description: "+this.getDescription()+
                        " ; Descriptor: "+this.getDescriptor()+
                        " ; ReturnType: "+this.getReturnType()+
                        " ; Signature: ";
        MBeanParameterInfo[] pTypes=this.getSignature();
        for(int i=0;i<pTypes.length;i++){
            retStr=retStr.concat((pTypes[i]).getType()+", ");
        }
        return retStr;
    }

    public Descriptor getDescriptor(){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "getDescriptor()","Entry");
        }
        if(operationDescriptor==null){
            operationDescriptor=validDescriptor(null);
        }
        return ((Descriptor)operationDescriptor.clone());
    }

    public void setDescriptor(Descriptor inDescriptor){
        if(MODELMBEAN_LOGGER.isLoggable(Level.FINER)){
            MODELMBEAN_LOGGER.logp(Level.FINER,
                    ModelMBeanOperationInfo.class.getName(),
                    "setDescriptor(Descriptor)","Entry");
        }
        operationDescriptor=validDescriptor(inDescriptor);
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
            fields.put("operationDescriptor",operationDescriptor);
            fields.put("currClass",currClass);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
