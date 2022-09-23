/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Line;

import javax.xml.stream.XMLStreamException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesLineBoundaryNodeXmlSerializer extends AbstractExtensionXmlSerializer<Line, CgmesLineBoundaryNode> {

    public CgmesLineBoundaryNodeXmlSerializer() {
        super("cgmesLineBoundaryNode", "network", CgmesLineBoundaryNode.class,
                false, "cgmesLineBoundaryNode.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_line_boundary_node/1_0", "clbn");
    }

    @Override
    public void write(CgmesLineBoundaryNode extension, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeBooleanAttribute("isHvdc", extension.isHvdc());
        extension.getLineEnergyIdentificationCodeEic()
                .ifPresent(lineEnergyIdentificationCodeEic -> {
                    context.getWriter().writeStringAttribute("lineEnergyIdentificationCodeEic", lineEnergyIdentificationCodeEic);
                });
    }

    @Override
    public CgmesLineBoundaryNode read(Line extendable, XmlReaderContext context) {
        boolean isHvdc = context.getReader().readBooleanAttribute("isHvdc");
        String lineEnergyIdentificationCodeEic = context.getReader().readStringAttribute("lineEnergyIdentificationCodeEic");
        extendable.newExtension(CgmesLineBoundaryNodeAdder.class).setHvdc(isHvdc).setLineEnergyIdentificationCodeEic(lineEnergyIdentificationCodeEic).add();
        return extendable.getExtension(CgmesLineBoundaryNode.class);
    }
}
