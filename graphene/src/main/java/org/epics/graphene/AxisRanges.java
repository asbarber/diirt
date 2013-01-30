/**
 * Copyright (C) 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.graphene;

/**
 *
 * @author carcassi
 */
public class AxisRanges {
    
    private AxisRanges() {
    }
    
    public static AxisRange absolute(final double min, final double max) {
        final Range axisRange = RangeUtil.range(min, max);
        return new AxisRange() {

            @Override
            public Range axisRange(Range range) {
                return axisRange;
            }
        };
    }
    
    public static AxisRange relative() {
        return new AxisRange() {

            @Override
            public Range axisRange(Range range) {
                return range;
            }
        };
    }
    
    
    // TODO horrible name
    // TODO we may need the integrated to "jump", so that the plot
    //      gets stretched fewer times, but its unclear how to do it
    //      in general
    public static AxisRange integrated() {
        return new AxisRange() {
            private Range pastRange;

            @Override
            public Range axisRange(Range range) {
                if (pastRange == null) {
                    pastRange = range;
                    return range;
                } else {
                    pastRange = RangeUtil.sum(pastRange, range);
                    return pastRange;
                }
            }
        };
    }
}
