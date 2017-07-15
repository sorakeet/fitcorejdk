/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.*;

public class Finishings extends EnumSyntax
        implements DocAttribute, PrintRequestAttribute, PrintJobAttribute{
    public static final Finishings NONE=new Finishings(3);
    public static final Finishings STAPLE=new Finishings(4);
    public static final Finishings COVER=new Finishings(6);
    public static final Finishings BIND=new Finishings(7);
    public static final Finishings SADDLE_STITCH=
            new Finishings(8);
    public static final Finishings EDGE_STITCH=
            new Finishings(9);
    public static final Finishings STAPLE_TOP_LEFT=
            new Finishings(20);
    public static final Finishings STAPLE_BOTTOM_LEFT=
            new Finishings(21);
    public static final Finishings STAPLE_TOP_RIGHT=
            new Finishings(22);
    public static final Finishings STAPLE_BOTTOM_RIGHT=
            new Finishings(23);
    public static final Finishings EDGE_STITCH_LEFT=
            new Finishings(24);
    public static final Finishings EDGE_STITCH_TOP=
            new Finishings(25);
    public static final Finishings EDGE_STITCH_RIGHT=
            new Finishings(26);
    public static final Finishings EDGE_STITCH_BOTTOM=
            new Finishings(27);
    public static final Finishings STAPLE_DUAL_LEFT=
            new Finishings(28);
    public static final Finishings STAPLE_DUAL_TOP=
            new Finishings(29);
    public static final Finishings STAPLE_DUAL_RIGHT=
            new Finishings(30);
    public static final Finishings STAPLE_DUAL_BOTTOM=
            new Finishings(31);
    private static final long serialVersionUID=-627840419548391754L;
    private static final String[] myStringTable=
            {"none",
                    "staple",
                    null,
                    "cover",
                    "bind",
                    "saddle-stitch",
                    "edge-stitch",
                    null, // The next ten enum values are reserved.
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "staple-top-left",
                    "staple-bottom-left",
                    "staple-top-right",
                    "staple-bottom-right",
                    "edge-stitch-left",
                    "edge-stitch-top",
                    "edge-stitch-right",
                    "edge-stitch-bottom",
                    "staple-dual-left",
                    "staple-dual-top",
                    "staple-dual-right",
                    "staple-dual-bottom"
            };
    private static final Finishings[] myEnumValueTable=
            {NONE,
                    STAPLE,
                    null,
                    COVER,
                    BIND,
                    SADDLE_STITCH,
                    EDGE_STITCH,
                    null, // The next ten enum values are reserved.
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    STAPLE_TOP_LEFT,
                    STAPLE_BOTTOM_LEFT,
                    STAPLE_TOP_RIGHT,
                    STAPLE_BOTTOM_RIGHT,
                    EDGE_STITCH_LEFT,
                    EDGE_STITCH_TOP,
                    EDGE_STITCH_RIGHT,
                    EDGE_STITCH_BOTTOM,
                    STAPLE_DUAL_LEFT,
                    STAPLE_DUAL_TOP,
                    STAPLE_DUAL_RIGHT,
                    STAPLE_DUAL_BOTTOM
            };
    protected Finishings(int value){
        super(value);
    }

    protected String[] getStringTable(){
        return (String[])myStringTable.clone();
    }

    protected EnumSyntax[] getEnumValueTable(){
        return (EnumSyntax[])myEnumValueTable.clone();
    }

    protected int getOffset(){
        return 3;
    }

    public final Class<? extends Attribute> getCategory(){
        return Finishings.class;
    }

    public final String getName(){
        return "finishings";
    }
}
