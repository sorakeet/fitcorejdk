/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

import java.util.Properties;

public interface Templates{
    Transformer newTransformer() throws TransformerConfigurationException;

    Properties getOutputProperties();
}
