/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.io.File;

public abstract class FileChooserUI extends ComponentUI{
    public abstract FileFilter getAcceptAllFileFilter(JFileChooser fc);

    public abstract FileView getFileView(JFileChooser fc);

    public abstract String getApproveButtonText(JFileChooser fc);

    public abstract String getDialogTitle(JFileChooser fc);

    public abstract void rescanCurrentDirectory(JFileChooser fc);

    public abstract void ensureFileIsVisible(JFileChooser fc,File f);

    public JButton getDefaultButton(JFileChooser fc){
        return null;
    }
}
