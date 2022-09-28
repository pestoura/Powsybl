/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineXml extends AbstractIdentifiableXml<HvdcLine, HvdcLineAdder, Network> {

    static final HvdcLineXml INSTANCE = new HvdcLineXml();

    static final String ROOT_ELEMENT_NAME = "hvdcLine";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(HvdcLine l, Network parent, NetworkXmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("r", l.getR());
        context.getWriter().writeDoubleAttribute("nominalV", l.getNominalV());
        context.getWriter().writeEnumAttribute("convertersMode", l.getConvertersMode());
        context.getWriter().writeDoubleAttribute("activePowerSetpoint", l.getActivePowerSetpoint());
        context.getWriter().writeDoubleAttribute("maxP", l.getMaxP());
        context.getWriter().writeStringAttribute("converterStation1", context.getAnonymizer().anonymizeString(l.getConverterStation1().getId()));
        context.getWriter().writeStringAttribute("converterStation2", context.getAnonymizer().anonymizeString(l.getConverterStation2().getId()));
    }

    @Override
    protected HvdcLineAdder createAdder(Network n) {
        return n.newHvdcLine();
    }

    @Override
    protected HvdcLine readRootElementAttributes(HvdcLineAdder adder, NetworkXmlReaderContext context) {
        double r = context.getReader().readDoubleAttribute("r");
        double nominalV = context.getReader().readDoubleAttribute("nominalV");
        HvdcLine.ConvertersMode convertersMode = context.getReader().readEnumAttribute("convertersMode", HvdcLine.ConvertersMode.class);
        double activePowerSetpoint = context.getReader().readDoubleAttribute("activePowerSetpoint");
        double maxP = context.getReader().readDoubleAttribute("maxP");
        String converterStation1 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("converterStation1"));
        String converterStation2 = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("converterStation2"));
        return adder.setR(r)
                .setNominalV(nominalV)
                .setConvertersMode(convertersMode)
                .setActivePowerSetpoint(activePowerSetpoint)
                .setMaxP(maxP)
                .setConverterStationId1(converterStation1)
                .setConverterStationId2(converterStation2)
                .add();
    }

    @Override
    protected void readSubElements(HvdcLine l, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> HvdcLineXml.super.readSubElements(l, context));
    }
}
