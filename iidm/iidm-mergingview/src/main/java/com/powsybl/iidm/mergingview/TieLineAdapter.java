/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends LineAdapter implements TieLine {

    private final DanglingLine half1;
    private final DanglingLine half2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.half1 = new DanglingLineAdapter(delegate.getHalf1(), index);
        this.half2 = new DanglingLineAdapter(delegate.getHalf2(), index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getUcteXnodeCode() {
        return ((TieLine) getDelegate()).getUcteXnodeCode();
    }

    @Override
    public DanglingLine getHalf1() {
        return half1;
    }

    @Override
    public DanglingLine getHalf2() {
        return half2;
    }

    @Override
    public DanglingLine getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }
}
