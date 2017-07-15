/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.util.Map;

public interface FlavorMap{
    Map<DataFlavor,String> getNativesForFlavors(DataFlavor[] flavors);

    Map<String,DataFlavor> getFlavorsForNatives(String[] natives);
}
