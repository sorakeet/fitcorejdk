/**
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * *******************************************************************
 * *********************************************************************
 * *********************************************************************
 * ** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 * ** As  an unpublished  work pursuant to Title 17 of the United    ***
 * ** States Code.  All rights reserved.                             ***
 * *********************************************************************
 * *********************************************************************
 **********************************************************************/
/** ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/
package com.sun.image.codec.jpeg;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class TruncatedFileException extends RuntimeException{
    private Raster ras=null;
    private BufferedImage bi=null;

    public TruncatedFileException(BufferedImage bi){
        super("Premature end of input file");
        this.bi=bi;
        this.ras=bi.getData();
    }

    public TruncatedFileException(Raster ras){
        super("Premature end of input file");
        this.ras=ras;
    }

    public Raster getRaster(){
        return ras;
    }

    public BufferedImage getBufferedImage(){
        return bi;
    }
}
