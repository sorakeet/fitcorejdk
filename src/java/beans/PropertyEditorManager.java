/**
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans;

public class PropertyEditorManager{
    public static void registerEditor(Class<?> targetType,Class<?> editorClass){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getPropertyEditorFinder().register(targetType,editorClass);
    }

    public static PropertyEditor findEditor(Class<?> targetType){
        return ThreadGroupContext.getContext().getPropertyEditorFinder().find(targetType);
    }

    public static String[] getEditorSearchPath(){
        return ThreadGroupContext.getContext().getPropertyEditorFinder().getPackages();
    }

    public static void setEditorSearchPath(String[] path){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPropertiesAccess();
        }
        ThreadGroupContext.getContext().getPropertyEditorFinder().setPackages(path);
    }
}
