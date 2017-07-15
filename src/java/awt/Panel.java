/**
 * Copyright (c) 1995, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

public class Panel extends Container implements Accessible{
    private static final String base="panel";
    private static final long serialVersionUID=-2728009084054400034L;
    private static int nameCounter=0;

    public Panel(){
        this(new FlowLayout());
    }

    public Panel(LayoutManager layout){
        setLayout(layout);
    }

    String constructComponentName(){
        synchronized(Panel.class){
            return base+nameCounter++;
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTPanel();
        }
        return accessibleContext;
    }
/////////////////
// Accessibility support
////////////////

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createPanel(this);
            super.addNotify();
        }
    }

    protected class AccessibleAWTPanel extends AccessibleAWTContainer{
        private static final long serialVersionUID=-6409552226660031050L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PANEL;
        }
    }
}
