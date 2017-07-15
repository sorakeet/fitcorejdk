/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.SunToolkit;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

public class LayoutFocusTraversalPolicy extends SortingFocusTraversalPolicy
        implements Serializable{
    // Delegate most of our fitness test to Default so that we only have to
    // code the algorithm once.
    private static final SwingDefaultFocusTraversalPolicy fitnessTestPolicy=
            new SwingDefaultFocusTraversalPolicy();

    public LayoutFocusTraversalPolicy(){
        super(new LayoutComparator());
    }

    LayoutFocusTraversalPolicy(Comparator<? super Component> c){
        super(c);
    }

    public Component getComponentAfter(Container aContainer,
                                       Component aComponent){
        if(aContainer==null||aComponent==null){
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator comparator=getComparator();
        if(comparator instanceof LayoutComparator){
            ((LayoutComparator)comparator).
                    setComponentOrientation(aContainer.
                            getComponentOrientation());
        }
        return super.getComponentAfter(aContainer,aComponent);
    }

    public Component getComponentBefore(Container aContainer,
                                        Component aComponent){
        if(aContainer==null||aComponent==null){
            throw new IllegalArgumentException("aContainer and aComponent cannot be null");
        }
        Comparator comparator=getComparator();
        if(comparator instanceof LayoutComparator){
            ((LayoutComparator)comparator).
                    setComponentOrientation(aContainer.
                            getComponentOrientation());
        }
        return super.getComponentBefore(aContainer,aComponent);
    }

    public Component getFirstComponent(Container aContainer){
        if(aContainer==null){
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator comparator=getComparator();
        if(comparator instanceof LayoutComparator){
            ((LayoutComparator)comparator).
                    setComponentOrientation(aContainer.
                            getComponentOrientation());
        }
        return super.getFirstComponent(aContainer);
    }

    public Component getLastComponent(Container aContainer){
        if(aContainer==null){
            throw new IllegalArgumentException("aContainer cannot be null");
        }
        Comparator comparator=getComparator();
        if(comparator instanceof LayoutComparator){
            ((LayoutComparator)comparator).
                    setComponentOrientation(aContainer.
                            getComponentOrientation());
        }
        return super.getLastComponent(aContainer);
    }

    protected boolean accept(Component aComponent){
        if(!super.accept(aComponent)){
            return false;
        }else if(SunToolkit.isInstanceOf(aComponent,"javax.swing.JTable")){
            // JTable only has ancestor focus bindings, we thus force it
            // to be focusable by returning true here.
            return true;
        }else if(SunToolkit.isInstanceOf(aComponent,"javax.swing.JComboBox")){
            JComboBox box=(JComboBox)aComponent;
            return box.getUI().isFocusTraversable(box);
        }else if(aComponent instanceof JComponent){
            JComponent jComponent=(JComponent)aComponent;
            InputMap inputMap=jComponent.getInputMap(JComponent.WHEN_FOCUSED,
                    false);
            while(inputMap!=null&&inputMap.size()==0){
                inputMap=inputMap.getParent();
            }
            if(inputMap!=null){
                return true;
            }
            // Delegate to the fitnessTestPolicy, this will test for the
            // case where the developer has overriden isFocusTraversable to
            // return true.
        }
        return fitnessTestPolicy.accept(aComponent);
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.writeObject(getComparator());
        out.writeBoolean(getImplicitDownCycleTraversal());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        setComparator((Comparator)in.readObject());
        setImplicitDownCycleTraversal(in.readBoolean());
    }
}

// Create our own subclass and change accept to public so that we can call
// accept.
class SwingDefaultFocusTraversalPolicy
        extends java.awt.DefaultFocusTraversalPolicy{
    public boolean accept(Component aComponent){
        return super.accept(aComponent);
    }
}
