/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.jca;

import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_TIME_Float;
import org.epics.pvmanager.vtype.VFloatArray;
import org.epics.pvmanager.vtype.VTypeToString;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListFloat;
import org.epics.util.array.ListInt;

/**
 *
 * @author carcassi
 */
class VFloatArrayFromDbr extends VNumberMetadata<DBR_TIME_Float, DBR_CTRL_Double> implements VFloatArray {

    public VFloatArrayFromDbr(DBR_TIME_Float dbrValue, DBR_CTRL_Double metadata, JCAConnectionPayload connPayload) {
        super(dbrValue, metadata, connPayload);
    }

    @Override
    public ListInt getSizes() {
        return new ArrayInt(dbrValue.getFloatValue().length);
    }

    @Override
    public ListFloat getData() {
        return new ArrayFloat(dbrValue.getFloatValue());
    }
    
    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }

}
