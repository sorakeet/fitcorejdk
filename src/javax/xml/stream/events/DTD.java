/**
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
 * Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
/** Copyright (c) 2009 by Oracle Corporation. All Rights Reserved.
 */
package javax.xml.stream.events;

import java.util.List;

public interface DTD extends XMLEvent{
    String getDocumentTypeDeclaration();

    Object getProcessedDTD();

    List getNotations();

    List getEntities();
}
