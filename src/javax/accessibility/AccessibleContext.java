/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;

public abstract class AccessibleContext{
    public static final String ACCESSIBLE_NAME_PROPERTY="AccessibleName";
    public static final String ACCESSIBLE_DESCRIPTION_PROPERTY="AccessibleDescription";
    public static final String ACCESSIBLE_STATE_PROPERTY="AccessibleState";
    public static final String ACCESSIBLE_VALUE_PROPERTY="AccessibleValue";
    public static final String ACCESSIBLE_SELECTION_PROPERTY="AccessibleSelection";
    public static final String ACCESSIBLE_CARET_PROPERTY="AccessibleCaret";
    public static final String ACCESSIBLE_VISIBLE_DATA_PROPERTY="AccessibleVisibleData";
    public static final String ACCESSIBLE_CHILD_PROPERTY="AccessibleChild";
    public static final String ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY="AccessibleActiveDescendant";
    public static final String ACCESSIBLE_TABLE_CAPTION_CHANGED=
            "accessibleTableCaptionChanged";
    public static final String ACCESSIBLE_TABLE_SUMMARY_CHANGED=
            "accessibleTableSummaryChanged";
    public static final String ACCESSIBLE_TABLE_MODEL_CHANGED=
            "accessibleTableModelChanged";
    public static final String ACCESSIBLE_TABLE_ROW_HEADER_CHANGED=
            "accessibleTableRowHeaderChanged";
    public static final String ACCESSIBLE_TABLE_ROW_DESCRIPTION_CHANGED=
            "accessibleTableRowDescriptionChanged";
    public static final String ACCESSIBLE_TABLE_COLUMN_HEADER_CHANGED=
            "accessibleTableColumnHeaderChanged";
    public static final String ACCESSIBLE_TABLE_COLUMN_DESCRIPTION_CHANGED=
            "accessibleTableColumnDescriptionChanged";
    public static final String ACCESSIBLE_ACTION_PROPERTY=
            "accessibleActionProperty";
    public static final String ACCESSIBLE_HYPERTEXT_OFFSET=
            "AccessibleHypertextOffset";
    public static final String ACCESSIBLE_TEXT_PROPERTY
            ="AccessibleText";
    public static final String ACCESSIBLE_INVALIDATE_CHILDREN=
            "accessibleInvalidateChildren";
    public static final String ACCESSIBLE_TEXT_ATTRIBUTES_CHANGED=
            "accessibleTextAttributesChanged";
    public static final String ACCESSIBLE_COMPONENT_BOUNDS_CHANGED=
            "accessibleComponentBoundsChanged";

    static{
        AWTAccessor.setAccessibleContextAccessor(new AWTAccessor.AccessibleContextAccessor(){
            @Override
            public void setAppContext(AccessibleContext accessibleContext,AppContext appContext){
                accessibleContext.targetAppContext=appContext;
            }

            @Override
            public AppContext getAppContext(AccessibleContext accessibleContext){
                return accessibleContext.targetAppContext;
            }
        });
    }

    protected Accessible accessibleParent=null;
    protected String accessibleName=null;
    protected String accessibleDescription=null;
    private volatile AppContext targetAppContext;
    private PropertyChangeSupport accessibleChangeSupport=null;
    private AccessibleRelationSet relationSet
            =new AccessibleRelationSet();
    private Object nativeAXResource;

    public String getAccessibleName(){
        return accessibleName;
    }

    public void setAccessibleName(String s){
        String oldName=accessibleName;
        accessibleName=s;
        firePropertyChange(ACCESSIBLE_NAME_PROPERTY,oldName,accessibleName);
    }

    public void firePropertyChange(String propertyName,
                                   Object oldValue,
                                   Object newValue){
        if(accessibleChangeSupport!=null){
            if(newValue instanceof PropertyChangeEvent){
                PropertyChangeEvent pce=(PropertyChangeEvent)newValue;
                accessibleChangeSupport.firePropertyChange(pce);
            }else{
                accessibleChangeSupport.firePropertyChange(propertyName,
                        oldValue,
                        newValue);
            }
        }
    }

    public String getAccessibleDescription(){
        return accessibleDescription;
    }

    public void setAccessibleDescription(String s){
        String oldDescription=accessibleDescription;
        accessibleDescription=s;
        firePropertyChange(ACCESSIBLE_DESCRIPTION_PROPERTY,
                oldDescription,accessibleDescription);
    }

    public abstract AccessibleRole getAccessibleRole();

    public abstract AccessibleStateSet getAccessibleStateSet();

    public Accessible getAccessibleParent(){
        return accessibleParent;
    }

    public void setAccessibleParent(Accessible a){
        accessibleParent=a;
    }

    public abstract int getAccessibleIndexInParent();

    public abstract int getAccessibleChildrenCount();

    public abstract Accessible getAccessibleChild(int i);

    public abstract Locale getLocale() throws IllegalComponentStateException;

    public void addPropertyChangeListener(PropertyChangeListener listener){
        if(accessibleChangeSupport==null){
            accessibleChangeSupport=new PropertyChangeSupport(this);
        }
        accessibleChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        if(accessibleChangeSupport!=null){
            accessibleChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public AccessibleAction getAccessibleAction(){
        return null;
    }

    public AccessibleComponent getAccessibleComponent(){
        return null;
    }

    public AccessibleSelection getAccessibleSelection(){
        return null;
    }

    public AccessibleText getAccessibleText(){
        return null;
    }

    public AccessibleEditableText getAccessibleEditableText(){
        return null;
    }

    public AccessibleValue getAccessibleValue(){
        return null;
    }

    public AccessibleIcon[] getAccessibleIcon(){
        return null;
    }

    public AccessibleRelationSet getAccessibleRelationSet(){
        return relationSet;
    }

    public AccessibleTable getAccessibleTable(){
        return null;
    }
}
