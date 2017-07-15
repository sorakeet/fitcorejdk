/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
 * Created on Apr 28, 2005
 */
/**
 * Created on Apr 28, 2005
 */
package javax.sql;

public interface StatementEventListener extends java.util.EventListener{
    void statementClosed(StatementEvent event);

    void statementErrorOccurred(StatementEvent event);
}
