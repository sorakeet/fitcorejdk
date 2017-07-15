/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.filechooser;

import javax.swing.*;
import java.io.File;

public abstract class FileView{
    public String getName(File f){
        return null;
    }

    ;

    public String getDescription(File f){
        return null;
    }

    public String getTypeDescription(File f){
        return null;
    }

    public Icon getIcon(File f){
        return null;
    }

    public Boolean isTraversable(File f){
        return null;
    }
}
