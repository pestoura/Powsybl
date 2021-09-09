/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class RemoteReactivePowerAdderImpl extends AbstractExtensionAdder<Generator, RemoteReactivePowerControl> implements RemoteReactivePowerControlAdder {

    static final String VALIDABLE_TYPE_DESCRIPTION = "generator";

    private double targetQ;

    private Terminal regulatingTerminal;

    private boolean enabled;

    protected RemoteReactivePowerAdderImpl(final Generator extendable) {
        super(extendable);
    }

    @Override
    protected RemoteReactivePowerControl createExtension(final Generator extendable) {
        return new RemoteReactivePowerControlImpl(targetQ, regulatingTerminal, enabled);
    }

    @Override
    public RemoteReactivePowerControlAdder withTargetQ(double targetQ) {
        ValidationUtil.checkReactivePowerSetpoint((Validable) super.extendable, VALIDABLE_TYPE_DESCRIPTION, targetQ, enabled);
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal((Validable) super.extendable, VALIDABLE_TYPE_DESCRIPTION, regulatingTerminal, enabled, super.extendable.getNetwork());
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withEnabled(boolean enabled) {
        ValidationUtil.checkRegulatingReactivePowerControl((Validable) super.extendable, regulatingTerminal,
            targetQ, enabled, super.extendable.getNetwork());
        this.enabled = enabled;
        return this;
    }
}
