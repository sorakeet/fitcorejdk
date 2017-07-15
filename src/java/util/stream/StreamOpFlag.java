/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.EnumMap;
import java.util.Map;
import java.util.Spliterator;

enum StreamOpFlag{
    // The following flags correspond to characteristics on Spliterator
    // and the values MUST be equal.
    //
    // 0, 0x00000001
    // Matches Spliterator.DISTINCT
    DISTINCT(0,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),
    // 1, 0x00000004
    // Matches Spliterator.SORTED
    SORTED(1,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),
    // 2, 0x00000010
    // Matches Spliterator.ORDERED
    ORDERED(2,
            set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP).clear(Type.TERMINAL_OP)
                    .clear(Type.UPSTREAM_TERMINAL_OP)),
    // 3, 0x00000040
    // Matches Spliterator.SIZED
    SIZED(3,
            set(Type.SPLITERATOR).set(Type.STREAM).clear(Type.OP)),
    // The following Spliterator characteristics are not currently used but a
    // gap in the bit set is deliberately retained to enable corresponding
    // stream flags if//when required without modification to other flag values.
    //
    // 4, 0x00000100 NONNULL(4, ...
    // 5, 0x00000400 IMMUTABLE(5, ...
    // 6, 0x00001000 CONCURRENT(6, ...
    // 7, 0x00004000 SUBSIZED(7, ...
    // The following 4 flags are currently undefined and a free for any further
    // spliterator characteristics.
    //
    //  8, 0x00010000
    //  9, 0x00040000
    // 10, 0x00100000
    // 11, 0x00400000
    // The following flags are specific to streams and operations
    //
    // 12, 0x01000000
    SHORT_CIRCUIT(12,
            set(Type.OP).set(Type.TERMINAL_OP));
    // The following 2 flags are currently undefined and a free for any further
    // stream flags if/when required
    //
    // 13, 0x04000000
    // 14, 0x10000000
    // 15, 0x40000000
    static final int SPLITERATOR_CHARACTERISTICS_MASK=createMask(Type.SPLITERATOR);
    static final int STREAM_MASK=createMask(Type.STREAM);
    static final int OP_MASK=createMask(Type.OP);
    static final int TERMINAL_OP_MASK=createMask(Type.TERMINAL_OP);
    static final int UPSTREAM_TERMINAL_OP_MASK=createMask(Type.UPSTREAM_TERMINAL_OP);
    static final int IS_DISTINCT=DISTINCT.set;
    static final int NOT_DISTINCT=DISTINCT.clear;
    static final int IS_SORTED=SORTED.set;
    static final int NOT_SORTED=SORTED.clear;
    static final int IS_ORDERED=ORDERED.set;
    static final int NOT_ORDERED=ORDERED.clear;
    static final int IS_SIZED=SIZED.set;
    static final int NOT_SIZED=SIZED.clear;
    static final int IS_SHORT_CIRCUIT=SHORT_CIRCUIT.set;
    private static final int SET_BITS=0b01;
    private static final int CLEAR_BITS=0b10;
    private static final int PRESERVE_BITS=0b11;
    private static final int FLAG_MASK=createFlagMask();
    private static final int FLAG_MASK_IS=STREAM_MASK;
    private static final int FLAG_MASK_NOT=STREAM_MASK<<1;
    static final int INITIAL_OPS_VALUE=FLAG_MASK_IS|FLAG_MASK_NOT;
    private final Map<Type,Integer> maskTable;
    private final int bitPosition;
    private final int set;
    private final int clear;
    private final int preserve;

    private StreamOpFlag(int position,MaskBuilder maskBuilder){
        this.maskTable=maskBuilder.build();
        // Two bits per flag
        position*=2;
        this.bitPosition=position;
        this.set=SET_BITS<<position;
        this.clear=CLEAR_BITS<<position;
        this.preserve=PRESERVE_BITS<<position;
    }

    private static MaskBuilder set(Type t){
        return new MaskBuilder(new EnumMap<>(Type.class)).set(t);
    }

    private static int createMask(Type t){
        int mask=0;
        for(StreamOpFlag flag : StreamOpFlag.values()){
            mask|=flag.maskTable.get(t)<<flag.bitPosition;
        }
        return mask;
    }

    private static int createFlagMask(){
        int mask=0;
        for(StreamOpFlag flag : StreamOpFlag.values()){
            mask|=flag.preserve;
        }
        return mask;
    }

    static int combineOpFlags(int newStreamOrOpFlags,int prevCombOpFlags){
        // 0x01 or 0x10 nibbles are transformed to 0x11
        // 0x00 nibbles remain unchanged
        // Then all the bits are flipped
        // Then the result is logically or'ed with the operation flags.
        return (prevCombOpFlags&StreamOpFlag.getMask(newStreamOrOpFlags))|newStreamOrOpFlags;
    }

    private static int getMask(int flags){
        return (flags==0)
                ?FLAG_MASK
                :~(flags|((FLAG_MASK_IS&flags)<<1)|((FLAG_MASK_NOT&flags)>>1));
    }

    static int toStreamFlags(int combOpFlags){
        // By flipping the nibbles 0x11 become 0x00 and 0x01 become 0x10
        // Shift left 1 to restore set flags and mask off anything other than the set flags
        return ((~combOpFlags)>>1)&FLAG_MASK_IS&combOpFlags;
    }

    static int toCharacteristics(int streamFlags){
        return streamFlags&SPLITERATOR_CHARACTERISTICS_MASK;
    }

    static int fromCharacteristics(Spliterator<?> spliterator){
        int characteristics=spliterator.characteristics();
        if((characteristics&Spliterator.SORTED)!=0&&spliterator.getComparator()!=null){
            // Do not propagate the SORTED characteristic if it does not correspond
            // to a natural sort order
            return characteristics&SPLITERATOR_CHARACTERISTICS_MASK&~Spliterator.SORTED;
        }else{
            return characteristics&SPLITERATOR_CHARACTERISTICS_MASK;
        }
    }

    static int fromCharacteristics(int characteristics){
        return characteristics&SPLITERATOR_CHARACTERISTICS_MASK;
    }

    int set(){
        return set;
    }

    int clear(){
        return clear;
    }

    boolean isStreamFlag(){
        return maskTable.get(Type.STREAM)>0;
    }

    boolean isKnown(int flags){
        return (flags&preserve)==set;
    }

    boolean isCleared(int flags){
        return (flags&preserve)==clear;
    }

    boolean isPreserved(int flags){
        return (flags&preserve)==preserve;
    }

    boolean canSet(Type t){
        return (maskTable.get(t)&SET_BITS)>0;
    }

    enum Type{
        SPLITERATOR,
        STREAM,
        OP,
        TERMINAL_OP,
        UPSTREAM_TERMINAL_OP
    }

    private static class MaskBuilder{
        final Map<Type,Integer> map;

        MaskBuilder(Map<Type,Integer> map){
            this.map=map;
        }

        MaskBuilder set(Type t){
            return mask(t,SET_BITS);
        }

        MaskBuilder mask(Type t,Integer i){
            map.put(t,i);
            return this;
        }

        MaskBuilder clear(Type t){
            return mask(t,CLEAR_BITS);
        }

        MaskBuilder setAndClear(Type t){
            return mask(t,PRESERVE_BITS);
        }

        Map<Type,Integer> build(){
            for(Type t : Type.values()){
                map.putIfAbsent(t,0b00);
            }
            return map;
        }
    }
}
