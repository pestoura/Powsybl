/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.Terminal;

import java.util.OptionalInt;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapter extends AbstractInjectionAdapter<ShuntCompensator> implements ShuntCompensator {

    ShuntCompensatorAdapter(final ShuntCompensator delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getSectionCount() {
        return getDelegate().getSectionCount();
    }

    @Override
    public OptionalInt findSectionCount() {
        return getDelegate().findSectionCount();
    }

    @Override
    public int getMaximumSectionCount() {
        return getDelegate().getMaximumSectionCount();
    }

    @Override
    public ShuntCompensator setSectionCount(final int sectionCount) {
        getDelegate().setSectionCount(sectionCount);
        return this;
    }

    @Override
    public ShuntCompensator unsetSectionCount() {
        getDelegate().unsetSectionCount();
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public double getG() {
        return getDelegate().getG();
    }

    @Override
    public double getB(int sectionCount) {
        return getDelegate().getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        return getDelegate().getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return getDelegate().getModelType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        return getDelegate().getModel();
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> modelType) {
        return getDelegate().getModel(modelType);
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return getIndex().getTerminal(getDelegate().getRegulatingTerminal());
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        Terminal terminal = regulatingTerminal;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        getDelegate().setRegulatingTerminal(terminal);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getDelegate().isVoltageRegulatorOn();
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public double getTargetV() {
        return getDelegate().getTargetV();
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        getDelegate().setTargetV(targetV);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getDelegate().getTargetDeadband();
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        getDelegate().setTargetDeadband(targetDeadband);
        return this;
    }

}
