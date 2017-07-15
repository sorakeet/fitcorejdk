/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.multi;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.util.Vector;

public class MultiTabbedPaneUI extends TabbedPaneUI{
    protected Vector uis=new Vector();
////////////////////
// Common UI methods
////////////////////

    public static ComponentUI createUI(JComponent a){
        ComponentUI mui=new MultiTabbedPaneUI();
        return MultiLookAndFeel.createUIs(mui,
                ((MultiTabbedPaneUI)mui).uis,
                a);
    }
////////////////////
// TabbedPaneUI methods
////////////////////

    public ComponentUI[] getUIs(){
        return MultiLookAndFeel.uisToArray(uis);
    }

    public int tabForCoordinate(JTabbedPane a,int b,int c){
        int returnValue=
                ((TabbedPaneUI)(uis.elementAt(0))).tabForCoordinate(a,b,c);
        for(int i=1;i<uis.size();i++){
            ((TabbedPaneUI)(uis.elementAt(i))).tabForCoordinate(a,b,c);
        }
        return returnValue;
    }

    public Rectangle getTabBounds(JTabbedPane a,int b){
        Rectangle returnValue=
                ((TabbedPaneUI)(uis.elementAt(0))).getTabBounds(a,b);
        for(int i=1;i<uis.size();i++){
            ((TabbedPaneUI)(uis.elementAt(i))).getTabBounds(a,b);
        }
        return returnValue;
    }
////////////////////
// ComponentUI methods
////////////////////

    public int getTabRunCount(JTabbedPane a){
        int returnValue=
                ((TabbedPaneUI)(uis.elementAt(0))).getTabRunCount(a);
        for(int i=1;i<uis.size();i++){
            ((TabbedPaneUI)(uis.elementAt(i))).getTabRunCount(a);
        }
        return returnValue;
    }

    public void installUI(JComponent a){
        for(int i=0;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).installUI(a);
        }
    }

    public void uninstallUI(JComponent a){
        for(int i=0;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).uninstallUI(a);
        }
    }

    public void paint(Graphics a,JComponent b){
        for(int i=0;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).paint(a,b);
        }
    }

    public void update(Graphics a,JComponent b){
        for(int i=0;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).update(a,b);
        }
    }

    public Dimension getPreferredSize(JComponent a){
        Dimension returnValue=
                ((ComponentUI)(uis.elementAt(0))).getPreferredSize(a);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).getPreferredSize(a);
        }
        return returnValue;
    }

    public Dimension getMinimumSize(JComponent a){
        Dimension returnValue=
                ((ComponentUI)(uis.elementAt(0))).getMinimumSize(a);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).getMinimumSize(a);
        }
        return returnValue;
    }

    public Dimension getMaximumSize(JComponent a){
        Dimension returnValue=
                ((ComponentUI)(uis.elementAt(0))).getMaximumSize(a);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).getMaximumSize(a);
        }
        return returnValue;
    }

    public boolean contains(JComponent a,int b,int c){
        boolean returnValue=
                ((ComponentUI)(uis.elementAt(0))).contains(a,b,c);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).contains(a,b,c);
        }
        return returnValue;
    }

    public int getAccessibleChildrenCount(JComponent a){
        int returnValue=
                ((ComponentUI)(uis.elementAt(0))).getAccessibleChildrenCount(a);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).getAccessibleChildrenCount(a);
        }
        return returnValue;
    }

    public Accessible getAccessibleChild(JComponent a,int b){
        Accessible returnValue=
                ((ComponentUI)(uis.elementAt(0))).getAccessibleChild(a,b);
        for(int i=1;i<uis.size();i++){
            ((ComponentUI)(uis.elementAt(i))).getAccessibleChild(a,b);
        }
        return returnValue;
    }
}
