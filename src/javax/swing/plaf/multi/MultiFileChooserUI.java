/**
 * Copyright (c) 1997, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.multi;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FileChooserUI;
import java.awt.*;
import java.io.File;
import java.util.Vector;

public class MultiFileChooserUI extends FileChooserUI{
    protected Vector uis=new Vector();
////////////////////
// Common UI methods
////////////////////

    public static ComponentUI createUI(JComponent a){
        ComponentUI mui=new MultiFileChooserUI();
        return MultiLookAndFeel.createUIs(mui,
                ((MultiFileChooserUI)mui).uis,
                a);
    }
////////////////////
// FileChooserUI methods
////////////////////

    public ComponentUI[] getUIs(){
        return MultiLookAndFeel.uisToArray(uis);
    }

    public FileFilter getAcceptAllFileFilter(JFileChooser a){
        FileFilter returnValue=
                ((FileChooserUI)(uis.elementAt(0))).getAcceptAllFileFilter(a);
        for(int i=1;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).getAcceptAllFileFilter(a);
        }
        return returnValue;
    }

    public FileView getFileView(JFileChooser a){
        FileView returnValue=
                ((FileChooserUI)(uis.elementAt(0))).getFileView(a);
        for(int i=1;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).getFileView(a);
        }
        return returnValue;
    }

    public String getApproveButtonText(JFileChooser a){
        String returnValue=
                ((FileChooserUI)(uis.elementAt(0))).getApproveButtonText(a);
        for(int i=1;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).getApproveButtonText(a);
        }
        return returnValue;
    }

    public String getDialogTitle(JFileChooser a){
        String returnValue=
                ((FileChooserUI)(uis.elementAt(0))).getDialogTitle(a);
        for(int i=1;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).getDialogTitle(a);
        }
        return returnValue;
    }

    public void rescanCurrentDirectory(JFileChooser a){
        for(int i=0;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).rescanCurrentDirectory(a);
        }
    }
////////////////////
// ComponentUI methods
////////////////////

    public void ensureFileIsVisible(JFileChooser a,File b){
        for(int i=0;i<uis.size();i++){
            ((FileChooserUI)(uis.elementAt(i))).ensureFileIsVisible(a,b);
        }
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
