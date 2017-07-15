/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import com.sun.jmx.mbeanserver.GetPropertyAction;

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.RELATION_LOGGER;
import static com.sun.jmx.mbeanserver.Util.cast;

@SuppressWarnings("serial")  // serialVersionUID must be constant
public class MBeanServerNotificationFilter extends NotificationFilterSupport{
    // Serialization compatibility stuff:
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=6001782699077323605L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=2605900539589789736L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("mySelectObjNameList",Vector.class),
                    new ObjectStreamField("myDeselectObjNameList",Vector.class)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields=
            {
                    new ObjectStreamField("selectedNames",List.class),
                    new ObjectStreamField("deselectedNames",List.class)
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
            // OK : Too bad, no compat with 1.0
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
    //
    // Private members
    //
    private List<ObjectName> selectedNames=new Vector<ObjectName>();
    private List<ObjectName> deselectedNames=null;
    //
    // Constructor
    //

    public MBeanServerNotificationFilter(){
        super();
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "MBeanServerNotificationFilter");
        enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "MBeanServerNotificationFilter");
        return;
    }
    //
    // Accessors
    //

    public synchronized void disableAllObjectNames(){
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "disableAllObjectNames");
        selectedNames=new Vector<ObjectName>();
        deselectedNames=null;
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "disableAllObjectNames");
        return;
    }

    public synchronized void disableObjectName(ObjectName objectName)
            throws IllegalArgumentException{
        if(objectName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "disableObjectName",objectName);
        // Removes from selected ObjectNames, if present
        if(selectedNames!=null){
            if(selectedNames.size()!=0){
                selectedNames.remove(objectName);
            }
        }
        // Adds it in deselected ObjectNames
        if(deselectedNames!=null){
            // If all are deselected, no need to do anything :)
            if(!(deselectedNames.contains(objectName))){
                // ObjectName was not already deselected
                deselectedNames.add(objectName);
            }
        }
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "disableObjectName");
        return;
    }

    public synchronized void enableAllObjectNames(){
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "enableAllObjectNames");
        selectedNames=null;
        deselectedNames=new Vector<ObjectName>();
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "enableAllObjectNames");
        return;
    }

    public synchronized void enableObjectName(ObjectName objectName)
            throws IllegalArgumentException{
        if(objectName==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "enableObjectName",objectName);
        // Removes from deselected ObjectNames, if present
        if(deselectedNames!=null){
            if(deselectedNames.size()!=0){
                deselectedNames.remove(objectName);
            }
        }
        // Adds it in selected ObjectNames
        if(selectedNames!=null){
            // If all are selected, no need to do anything :)
            if(!(selectedNames.contains(objectName))){
                // ObjectName was not already selected
                selectedNames.add(objectName);
            }
        }
        RELATION_LOGGER.exiting(MBeanServerNotificationFilter.class.getName(),
                "enableObjectName");
        return;
    }

    public synchronized Vector<ObjectName> getEnabledObjectNames(){
        if(selectedNames!=null){
            return new Vector<ObjectName>(selectedNames);
        }else{
            return null;
        }
    }

    public synchronized Vector<ObjectName> getDisabledObjectNames(){
        if(deselectedNames!=null){
            return new Vector<ObjectName>(deselectedNames);
        }else{
            return null;
        }
    }
    //
    // NotificationFilter interface
    //

    public synchronized boolean isNotificationEnabled(Notification notif)
            throws IllegalArgumentException{
        if(notif==null){
            String excMsg="Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(MBeanServerNotificationFilter.class.getName(),
                "isNotificationEnabled",notif);
        // Checks the type first
        String ntfType=notif.getType();
        Vector<String> enabledTypes=getEnabledTypes();
        if(!(enabledTypes.contains(ntfType))){
            RELATION_LOGGER.logp(Level.FINER,
                    MBeanServerNotificationFilter.class.getName(),
                    "isNotificationEnabled",
                    "Type not selected, exiting");
            return false;
        }
        // We have a MBeanServerNotification: downcasts it
        MBeanServerNotification mbsNtf=(MBeanServerNotification)notif;
        // Checks the ObjectName
        ObjectName objName=mbsNtf.getMBeanName();
        // Is it selected?
        boolean isSelectedFlg=false;
        if(selectedNames!=null){
            // Not all are implicitly selected:
            // checks for explicit selection
            if(selectedNames.size()==0){
                // All are explicitly not selected
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "No ObjectNames selected, exiting");
                return false;
            }
            isSelectedFlg=selectedNames.contains(objName);
            if(!isSelectedFlg){
                // Not in the explicit selected list
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName not in selected list, exiting");
                return false;
            }
        }
        if(!isSelectedFlg){
            // Not explicitly selected: is it deselected?
            if(deselectedNames==null){
                // All are implicitly deselected and it is not explicitly
                // selected
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName not selected, and all "+
                                "names deselected, exiting");
                return false;
            }else if(deselectedNames.contains(objName)){
                // Explicitly deselected
                RELATION_LOGGER.logp(Level.FINER,
                        MBeanServerNotificationFilter.class.getName(),
                        "isNotificationEnabled",
                        "ObjectName explicitly not selected, exiting");
                return false;
            }
        }
        RELATION_LOGGER.logp(Level.FINER,
                MBeanServerNotificationFilter.class.getName(),
                "isNotificationEnabled",
                "ObjectName selected, exiting");
        return true;
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        if(compat){
            // Read an object serialized in the old serial form
            //
            ObjectInputStream.GetField fields=in.readFields();
            selectedNames=cast(fields.get("mySelectObjNameList",null));
            if(fields.defaulted("mySelectObjNameList")){
                throw new NullPointerException("mySelectObjNameList");
            }
            deselectedNames=cast(fields.get("myDeselectObjNameList",null));
            if(fields.defaulted("myDeselectObjNameList")){
                throw new NullPointerException("myDeselectObjNameList");
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
            fields.put("mySelectObjNameList",selectedNames);
            fields.put("myDeselectObjNameList",deselectedNames);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
        }
    }
}
