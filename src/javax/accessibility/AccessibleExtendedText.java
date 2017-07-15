/**
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.accessibility;

import java.awt.*;

public interface AccessibleExtendedText{
    public static final int LINE=4; // BugID: 4849720
    public static final int ATTRIBUTE_RUN=5; // BugID: 4849720

    public String getTextRange(int startIndex,int endIndex);

    public AccessibleTextSequence getTextSequenceAt(int part,int index);

    public AccessibleTextSequence getTextSequenceAfter(int part,int index);

    public AccessibleTextSequence getTextSequenceBefore(int part,int index);

    public Rectangle getTextBounds(int startIndex,int endIndex);
}
