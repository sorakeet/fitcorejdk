/**
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import javax.swing.*;
import java.io.Serializable;

class OptionComboBoxModel<E> extends DefaultComboBoxModel<E> implements Serializable{
    private Option selectedOption=null;

    public Option getInitialSelection(){
        return selectedOption;
    }

    public void setInitialSelection(Option option){
        selectedOption=option;
    }
}
