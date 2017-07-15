/**
 * Copyright (c) 2000, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// jmx import
//

import javax.management.JMException;

public class OpenDataException extends JMException{
    private static final long serialVersionUID=8346311255433349870L;

    public OpenDataException(){
        super();
    }

    public OpenDataException(String msg){
        super(msg);
    }
}
