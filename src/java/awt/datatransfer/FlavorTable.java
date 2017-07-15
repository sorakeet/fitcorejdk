/**
 * Copyright (c) 2000, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt.datatransfer;

import java.util.List;

public interface FlavorTable extends FlavorMap{
    List<String> getNativesForFlavor(DataFlavor flav);

    List<DataFlavor> getFlavorsForNative(String nat);
}
