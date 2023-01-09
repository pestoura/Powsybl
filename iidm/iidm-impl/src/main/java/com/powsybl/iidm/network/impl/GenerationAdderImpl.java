/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLineCharacteristicsAdder;
import com.powsybl.iidm.network.ValidationUtil;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class GenerationAdderImpl<H extends DanglingLineCharacteristicsAdder<H>> implements DanglingLineCharacteristicsAdder.GenerationAdder<H> {

    private final GenerationAdderHolder<H> parent;

    private double minP = Double.NaN;
    private double maxP = Double.NaN;
    private double targetP = Double.NaN;
    private double targetQ = Double.NaN;
    private boolean voltageRegulationOn = false;
    private double targetV = Double.NaN;

    GenerationAdderImpl(GenerationAdderHolder<H> parent) {
        this.parent = parent;
    }

    @Override
    public GenerationAdderImpl<H> setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public GenerationAdderImpl<H> setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public GenerationAdderImpl<H> setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    @Override
    public GenerationAdderImpl<H> setTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public GenerationAdderImpl<H> setVoltageRegulationOn(boolean voltageRegulationOn) {
        this.voltageRegulationOn = voltageRegulationOn;
        return this;
    }

    @Override
    public GenerationAdderImpl<H> setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public H add() {
        NetworkImpl network = parent.getNetwork();
        ValidationUtil.checkActivePowerLimits(parent, minP, maxP);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkActivePowerSetpoint(parent, targetP, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(parent, voltageRegulationOn, targetV, targetQ, network.getMinValidationLevel()));

        parent.setGenerationAdder(this);
        return (H) parent;
    }

    DanglingLineCharacteristics.GenerationImpl build() {
        return new DanglingLineCharacteristics.GenerationImpl(parent.getNetwork(), minP, maxP, targetP, targetQ, targetV, voltageRegulationOn);
    }
}
