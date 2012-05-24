/*
 * Copyright 2011 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.util.array;

import java.util.Arrays;

/**
 * Wraps a {@code short[]} into a {@link ListShort}.
 *
 * @author Gabriele Carcassi
 */
public final class ArrayShort extends ListShort {
    
    private final short[] array;
    private final boolean readOnly;
    
    /**
     * A new {@code ArrayShort} that wraps around the given array.
     * 
     * @param array an array
     */

    public ArrayShort(short[] array) {
        this(array, true);
    }
    
    /**
     * A new {@code ArrayShort} that wraps around the given array.
     * 
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */

    public ArrayShort(short[] array, boolean readOnly) {
        this.array = array;
        this.readOnly = readOnly;
    }

    @Override
    public boolean deepEquals(ListNumber other) {
        if (other instanceof ArrayDouble)
            return Arrays.equals(array, ((ArrayShort) other).array);
        
        return super.deepEquals(other);
    }

    @Override
    public final IteratorShort iterator() {
        return new IteratorShort() {
            
            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            public short nextShort() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return array.length;
    }
    
    @Override
    public short getShort(int index) {
        return array[index];
    }

    @Override
    public void setShort(int index, short value) {
        if (!readOnly) {
            array[index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }
    
}
