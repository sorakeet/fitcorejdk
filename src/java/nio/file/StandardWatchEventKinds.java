/**
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

public final class StandardWatchEventKinds{
    public static final WatchEvent.Kind<Object> OVERFLOW=
            new StdWatchEventKind<Object>("OVERFLOW",Object.class);
    public static final WatchEvent.Kind<Path> ENTRY_CREATE=
            new StdWatchEventKind<Path>("ENTRY_CREATE",Path.class);
    public static final WatchEvent.Kind<Path> ENTRY_DELETE=
            new StdWatchEventKind<Path>("ENTRY_DELETE",Path.class);
    public static final WatchEvent.Kind<Path> ENTRY_MODIFY=
            new StdWatchEventKind<Path>("ENTRY_MODIFY",Path.class);

    private StandardWatchEventKinds(){
    }

    private static class StdWatchEventKind<T> implements WatchEvent.Kind<T>{
        private final String name;
        private final Class<T> type;

        StdWatchEventKind(String name,Class<T> type){
            this.name=name;
            this.type=type;
        }

        @Override
        public String name(){
            return name;
        }

        @Override
        public Class<T> type(){
            return type;
        }

        @Override
        public String toString(){
            return name;
        }
    }
}
