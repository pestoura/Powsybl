/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class MeasurementValidationUtil {

    public static <C extends Connectable<C>> void checkId(String id, Measurements<C> measurements) {
        if (id != null && measurements.getMeasurement(id) != null) {
            throw new PowsyblException(String.format("There is already a measurement with ID %s", id));
        }
    }

    public static void checkValue(double value) {
        if (Double.isNaN(value)) {
            throw new PowsyblException("Undefined value for measurement");
        }
    }

    public static <C extends Connectable<C>> void checkSide(Measurement.Side side, Connectable<C> c) {
        if (side == null) {
            if (c instanceof Branch || c instanceof ThreeWindingsTransformer) {
                throw new PowsyblException("Inconsistent null side for measurement of branch or three windings transformer");
            }
        } else if (c instanceof Injection) {
            throw new PowsyblException("Inconsistent side for measurement of injection");
        }
    }

    private MeasurementValidationUtil() {
    }
}
