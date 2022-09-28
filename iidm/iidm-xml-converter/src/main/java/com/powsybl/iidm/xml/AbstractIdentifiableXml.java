/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableAdder;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractIdentifiableXml<T extends Identifiable, A extends IdentifiableAdder<A>, P extends Identifiable> {

    protected abstract String getRootElementName();

    protected boolean isValid(T identifiable, P parent) {
        return true;
    }

    protected abstract void writeRootElementAttributes(T identifiable, P parent, NetworkXmlWriterContext context);

    protected void writeSubElements(T identifiable, P parent, NetworkXmlWriterContext context) {
    }

    public final void write(T identifiable, P parent, NetworkXmlWriterContext context) {
        if (!isValid(identifiable, parent)) {
            return;
        }
        context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), getRootElementName());
        context.getWriter().writeStringAttribute("id", context.getAnonymizer().anonymizeString(identifiable.getId()));
        ((Identifiable<?>) identifiable).getOptionalName().ifPresent(name -> {
            context.getWriter().writeStringAttribute("name", context.getAnonymizer().anonymizeString(name));
        });

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> context.getWriter().writeBooleanAttribute("fictitious", identifiable.isFictitious(), false));

        writeRootElementAttributes(identifiable, parent, context);

        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            AliasesXml.write(identifiable, getRootElementName(), context);
        });

        PropertiesXml.write(identifiable, context);

        writeSubElements(identifiable, parent, context);

        context.getWriter().writeEndNode();

        context.addExportedEquipment(identifiable);
    }

    protected abstract A createAdder(P parent);

    protected abstract T readRootElementAttributes(A adder, NetworkXmlReaderContext context);

    protected void readSubElements(T identifiable, NetworkXmlReaderContext context) {
        if (context.getReader().getNodeName().equals(PropertiesXml.PROPERTY)) {
            PropertiesXml.read(identifiable, context);
        } else if (context.getReader().getNodeName().equals(AliasesXml.ALIAS)) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
            AliasesXml.read(identifiable, context);
        } else {
            throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <" + identifiable.getId() + ">");
        }
    }

    protected void readElement(String id, A adder, NetworkXmlReaderContext context) {
        T identifiable = readRootElementAttributes(adder, context);
        if (identifiable != null) {
            readSubElements(identifiable, context);
        }
    }

    public final void read(P parent, NetworkXmlReaderContext context) {
        A adder = createAdder(parent);
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        String name = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("name"));
        adder.setId(id)
                .setName(name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean fictitious = context.getReader().readBooleanAttribute("fictitious", false);
            adder.setFictitious(fictitious);
        });
        readElement(id, adder, context);
    }
}
