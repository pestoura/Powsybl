/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryLineAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BoundaryLineAdderImpl extends AbstractInjectionAdder<BoundaryLineAdderImpl> implements BoundaryLineAdder {

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private String ucteXnodeCode;

    BoundaryLineAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Boundary line";
    }

    @Override
    public BoundaryLineAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public BoundaryLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public BoundaryLineImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        BoundaryLineImpl boundaryLine = new BoundaryLineImpl(getNetwork().getRef(), id, getName(), p0, q0, r, x, g, b, ucteXnodeCode);
        boundaryLine.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(boundaryLine);
        getNetwork().getListeners().notifyCreation(boundaryLine);
        return boundaryLine;
    }

}
