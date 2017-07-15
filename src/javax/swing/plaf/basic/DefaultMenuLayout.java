/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;

public class DefaultMenuLayout extends BoxLayout implements UIResource{
    public DefaultMenuLayout(Container target,int axis){
        super(target,axis);
    }

    public Dimension preferredLayoutSize(Container target){
        if(target instanceof JPopupMenu){
            JPopupMenu popupMenu=(JPopupMenu)target;
            sun.swing.MenuItemLayoutHelper.clearUsedClientProperties(popupMenu);
            if(popupMenu.getComponentCount()==0){
                return new Dimension(0,0);
            }
        }
        // Make BoxLayout recalculate cached preferred sizes
        super.invalidateLayout(target);
        return super.preferredLayoutSize(target);
    }
}
