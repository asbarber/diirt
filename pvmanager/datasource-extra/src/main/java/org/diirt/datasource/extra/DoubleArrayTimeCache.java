/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.extra;

import java.util.List;
import org.diirt.vtype.Display;
import org.diirt.util.array.ListNumber;
import org.diirt.util.time.Timestamp;


/**
 *
 * @author carcassi
 */
public interface DoubleArrayTimeCache {
    
    public interface Data {
        public Timestamp getBegin();
        
        public Timestamp getEnd();
        
        public int getNArrays();
        
        public ListNumber getArray(int index);
        
        public Timestamp getTimestamp(int index);
    }
    
    public Data getData(Timestamp begin, Timestamp end);
    
    /**
     * Each segment of the new data ends with an array of old data.
     * Two regions can be requested: an update region, where only updates
     * are going to be returned, and a new region, where all data is going to
     * be returned.
     * 
     * @return the new data chunks
     */
    public List<Data> newData(Timestamp beginUpdate, Timestamp endUpdate, Timestamp beginNew, Timestamp endNew);
    
    public Display getDisplay();
}
