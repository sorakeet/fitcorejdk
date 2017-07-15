/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.SunGraphicsCallback;

import java.awt.peer.LightweightPeer;

abstract class GraphicsCallback extends SunGraphicsCallback{
    static final class PaintCallback extends GraphicsCallback{
        private static PaintCallback instance=new PaintCallback();

        private PaintCallback(){
        }

        static PaintCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.paint(cg);
        }
    }

    static final class PrintCallback extends GraphicsCallback{
        private static PrintCallback instance=new PrintCallback();

        private PrintCallback(){
        }

        static PrintCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.print(cg);
        }
    }

    static final class PaintAllCallback extends GraphicsCallback{
        private static PaintAllCallback instance=new PaintAllCallback();

        private PaintAllCallback(){
        }

        static PaintAllCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.paintAll(cg);
        }
    }

    static final class PrintAllCallback extends GraphicsCallback{
        private static PrintAllCallback instance=new PrintAllCallback();

        private PrintAllCallback(){
        }

        static PrintAllCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.printAll(cg);
        }
    }

    static final class PeerPaintCallback extends GraphicsCallback{
        private static PeerPaintCallback instance=new PeerPaintCallback();

        private PeerPaintCallback(){
        }

        static PeerPaintCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.validate();
            if(comp.peer instanceof LightweightPeer){
                comp.lightweightPaint(cg);
            }else{
                comp.peer.paint(cg);
            }
        }
    }

    static final class PeerPrintCallback extends GraphicsCallback{
        private static PeerPrintCallback instance=new PeerPrintCallback();

        private PeerPrintCallback(){
        }

        static PeerPrintCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            comp.validate();
            if(comp.peer instanceof LightweightPeer){
                comp.lightweightPrint(cg);
            }else{
                comp.peer.print(cg);
            }
        }
    }

    static final class PaintHeavyweightComponentsCallback
            extends GraphicsCallback{
        private static PaintHeavyweightComponentsCallback instance=
                new PaintHeavyweightComponentsCallback();

        private PaintHeavyweightComponentsCallback(){
        }

        static PaintHeavyweightComponentsCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            if(comp.peer instanceof LightweightPeer){
                comp.paintHeavyweightComponents(cg);
            }else{
                comp.paintAll(cg);
            }
        }
    }

    static final class PrintHeavyweightComponentsCallback
            extends GraphicsCallback{
        private static PrintHeavyweightComponentsCallback instance=
                new PrintHeavyweightComponentsCallback();

        private PrintHeavyweightComponentsCallback(){
        }

        static PrintHeavyweightComponentsCallback getInstance(){
            return instance;
        }

        public void run(Component comp,Graphics cg){
            if(comp.peer instanceof LightweightPeer){
                comp.printHeavyweightComponents(cg);
            }else{
                comp.printAll(cg);
            }
        }
    }
}
