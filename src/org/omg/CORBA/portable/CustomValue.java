/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Defines the base interface for all custom value types
 * generated from IDL.
 * <p>
 * All value types implement ValueBase either directly
 * or indirectly by implementing either the StreamableValue
 * or CustomValue interface.
 *
 * @author OMG
 * <p>
 * Defines the base interface for all custom value types
 * generated from IDL.
 * <p>
 * All value types implement ValueBase either directly
 * or indirectly by implementing either the StreamableValue
 * or CustomValue interface.
 * @author OMG
 */
/**
 * Defines the base interface for all custom value types
 * generated from IDL.
 * <p>
 * All value types implement ValueBase either directly
 * or indirectly by implementing either the StreamableValue
 * or CustomValue interface.
 *
 * @author OMG
 */
package org.omg.CORBA.portable;

import org.omg.CORBA.CustomMarshal;

public interface CustomValue extends ValueBase, CustomMarshal{
}
