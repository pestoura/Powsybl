/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReader;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesSvMetadataXmlSerializer extends AbstractExtensionXmlSerializer<Network, CgmesSvMetadata> {

    public CgmesSvMetadataXmlSerializer() {
        super("cgmesSvMetadata", "network", CgmesSvMetadata.class, true, "cgmesSvMetadata.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_sv_metadata/1_0", "csm");
    }

    @Override
    public void write(CgmesSvMetadata extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XmlWriter writer = networkContext.getWriter();
        writer.writeStringAttribute("description", extension.getDescription());
        writer.writeIntAttribute("svVersion", extension.getSvVersion());
        writer.writeStringAttribute("modelingAuthoritySet", extension.getModelingAuthoritySet());
        for (String dep : extension.getDependencies()) {
            writer.writeStartNode(getNamespaceUri(), "dependentOn");
            writer.writeNodeContent(dep);
            writer.writeEndNode();
        }
    }

    @Override
    public CgmesSvMetadata read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XmlReader reader = networkContext.getReader();
        CgmesSvMetadataAdder adder = extendable.newExtension(CgmesSvMetadataAdder.class);
        adder.setDescription(reader.readStringAttribute("description"))
                .setSvVersion(reader.readIntAttribute("svVersion"))
                .setModelingAuthoritySet(reader.readStringAttribute("modelingAuthoritySet"));
        reader.readUntilEndNode("cgmesSvMetadata", () -> {
            if (reader.getNodeName().equals("dependentOn")) {
                adder.addDependency(reader.readContent());
            } else {
                throw new PowsyblException("Unknown element name <" + reader.getNodeName() + "> in <cgmesSvMetadata>");
            }
        });
        adder.add();
        return extendable.getExtension(CgmesSvMetadata.class);
    }
}
