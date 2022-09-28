/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadXml extends AbstractConnectableXml<Load, LoadAdder, VoltageLevel> {

    static final LoadXml INSTANCE = new LoadXml();

    static final String ROOT_ELEMENT_NAME = "load";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Load l, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeEnumAttribute("loadType", l.getLoadType());
        context.getWriter().writeDoubleAttribute("p0", l.getP0());
        context.getWriter().writeDoubleAttribute("q0", l.getQ0());
        writeNodeOrBus(null, l.getTerminal(), context);
        writePQ(null, l.getTerminal(), context.getWriter());
    }

    @Override
    protected LoadAdder createAdder(VoltageLevel vl) {
        return vl.newLoad();
    }

    @Override
    protected Load readRootElementAttributes(LoadAdder adder, NetworkXmlReaderContext context) {
        LoadType loadType = context.getReader().readEnumAttribute("loadType", LoadType.class, LoadType.UNDEFINED);
        double p0 = context.getReader().readDoubleAttribute("p0");
        double q0 = context.getReader().readDoubleAttribute("q0");
        readNodeOrBus(adder, context);
        Load l = adder.setLoadType(loadType)
                .setP0(p0)
                .setQ0(q0)
                .add();
        readPQ(null, l.getTerminal(), context.getReader());
        return l;
    }

    @Override
    protected void readSubElements(Load l, NetworkXmlReaderContext context) throws XMLStreamException {
        context.getReader().readUntilEndNode(getRootElementName(), () -> LoadXml.super.readSubElements(l, context));
    }
}
