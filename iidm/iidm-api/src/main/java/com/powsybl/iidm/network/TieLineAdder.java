/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TieLineAdder extends IdentifiableAdder<TieLineAdder> {

    TieLineAdder setVoltageLevel1(String voltageLevelId1);

    TieLineAdder setVoltageLevel2(String voltageLevelId2);

    MergedDanglingLineAdder newHalf1();

    MergedDanglingLineAdder newHalf2();

    TieLine add();

}
