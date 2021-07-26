/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.RegulatingControlMapping.RegulatingControl;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class RegulatingControlMappingForGenerators {

    private static final String QPERCENT = "qPercent";

    RegulatingControlMappingForGenerators(RegulatingControlMapping parent, Context context) {
        this.parent = parent;
        this.context = context;
        mapping = new HashMap<>();
    }

    public static void initialize(GeneratorAdder adder) {
        adder.setVoltageRegulatorOn(false);
    }

    public void add(String generatorId, PropertyBag sm) {
        String cgmesRegulatingControlId = RegulatingControlMapping.getRegulatingControlId(sm);
        double qPercent = sm.asDouble(QPERCENT);

        if (mapping.containsKey(generatorId)) {
            throw new CgmesModelException("Generator already added, IIDM Generator Id: " + generatorId);
        }

        CgmesRegulatingControlForGenerator rd = new CgmesRegulatingControlForGenerator();
        rd.regulatingControlId = cgmesRegulatingControlId;
        rd.qPercent = qPercent;
        rd.controlEnabled = sm.asBoolean("controlEnabled", false);
        mapping.put(generatorId, rd);
    }

    void applyRegulatingControls(Network network) {
        network.getGeneratorStream().forEach(this::apply);
    }

    private void apply(Generator gen) {
        CgmesRegulatingControlForGenerator rd = mapping.get(gen.getId());
        apply(gen, rd);
    }

    private void apply(Generator gen, CgmesRegulatingControlForGenerator rc) {
        if (rc == null) {
            return;
        }

        String controlId = rc.regulatingControlId;
        if (controlId == null) {
            context.missing("Regulating control Id not defined");
            return;
        }

        RegulatingControl control = parent.cachedRegulatingControls().get(controlId);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            return;
        }

        boolean okSet = false;
        if (RegulatingControlMapping.isControlModeVoltage(control.mode)) {
            okSet = setRegulatingControlVoltage(controlId, control, rc.qPercent, rc.controlEnabled, gen);
        } else if (RegulatingControlMapping.isControlModeReactivePower(control.mode)) {
            okSet = setRegulatingControlReactivePower(controlId, control, rc.qPercent, rc.controlEnabled, gen);
        } else {
            context.ignored(control.mode, "Unsupported regulation mode for generator " + gen.getId());
        }
        control.setCorrectlySet(okSet);
    }

    private boolean setRegulatingControlVoltage(String controlId, RegulatingControl control, double qPercent,
        boolean eqControlEnabled, Generator gen) {

        // Take the terminal defined in CGMES file (can be null)
        // Take the targetV (can be NaN)
        Terminal terminal = parent.getRegulatingTerminal(control.cgmesTerminal);
        double targetV = control.targetValue;

        boolean valid = ValidationUtil.validRegulatingVoltageControl(terminal, targetV, gen.getNetwork());
        boolean voltageRegulatorOn = false;
        // Regulating control is enabled AND this equipment participates in regulating control
        // Control attributes must be valid
        if (control.enabled && eqControlEnabled && valid) {
            voltageRegulatorOn = true;
        }

        gen.setRegulatingTerminal(terminal)
            .setTargetV(targetV)
            .setVoltageRegulatorOn(voltageRegulatorOn);

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }
        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);

        return valid;
    }

    private boolean setRegulatingControlReactivePower(String controlId, RegulatingControl control, double qPercent, boolean eqControlEnabled, Generator gen) {
        Terminal terminal = parent.getRegulatingTerminal(control.cgmesTerminal);
        double targetQ = control.targetValue;

        boolean valid = ValidationUtil.validRegulatingReactivePowerControl(terminal, targetQ, gen.getNetwork());
        boolean controlReactiveRegulatorOn = false;
        if (control.enabled && eqControlEnabled && valid) {
            controlReactiveRegulatorOn = true;
        }

        gen.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .withEnabled(controlReactiveRegulatorOn)
                .add();

        // add qPercent as an extension
        if (!Double.isNaN(qPercent)) {
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                    .withQPercent(qPercent)
                    .add();
        }

        gen.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", controlId);

        return valid;
    }

    private static class CgmesRegulatingControlForGenerator {
        String regulatingControlId;
        double qPercent;
        boolean controlEnabled;
    }

    private final RegulatingControlMapping parent;
    private final Map<String, CgmesRegulatingControlForGenerator> mapping;
    private final Context context;
}
