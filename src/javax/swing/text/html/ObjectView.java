/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class ObjectView extends ComponentView{
    public ObjectView(Element elem){
        super(elem);
    }

    protected Component createComponent(){
        AttributeSet attr=getElement().getAttributes();
        String classname=(String)attr.getAttribute(HTML.Attribute.CLASSID);
        try{
            ReflectUtil.checkPackageAccess(classname);
            Class c=Class.forName(classname,true,Thread.currentThread().
                    getContextClassLoader());
            Object o=c.newInstance();
            if(o instanceof Component){
                Component comp=(Component)o;
                setParameters(comp,attr);
                return comp;
            }
        }catch(Throwable e){
            // couldn't create a component... fall through to the
            // couldn't load representation.
        }
        return getUnloadableRepresentation();
    }

    Component getUnloadableRepresentation(){
        // PENDING(prinz) get some artwork and return something
        // interesting here.
        Component comp=new JLabel("??");
        comp.setForeground(Color.red);
        return comp;
    }

    private void setParameters(Component comp,AttributeSet attr){
        Class k=comp.getClass();
        BeanInfo bi;
        try{
            bi=Introspector.getBeanInfo(k);
        }catch(IntrospectionException ex){
            System.err.println("introspector failed, ex: "+ex);
            return;             // quit for now
        }
        PropertyDescriptor props[]=bi.getPropertyDescriptors();
        for(int i=0;i<props.length;i++){
            //      System.err.println("checking on props[i]: "+props[i].getName());
            Object v=attr.getAttribute(props[i].getName());
            if(v instanceof String){
                // found a property parameter
                String value=(String)v;
                Method writer=props[i].getWriteMethod();
                if(writer==null){
                    // read-only property. ignore
                    return;     // for now
                }
                Class[] params=writer.getParameterTypes();
                if(params.length!=1){
                    // zero or more than one argument, ignore
                    return;     // for now
                }
                Object[] args={value};
                try{
                    MethodUtil.invoke(writer,comp,args);
                }catch(Exception ex){
                    System.err.println("Invocation failed");
                    // invocation code
                }
            }
        }
    }
}
