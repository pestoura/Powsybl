/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ConnectablePositionXmlSerializer<C extends Connectable<C>> extends AbstractVersionableNetworkExtensionXmlSerializer<C, ConnectablePosition<C>> {

    private static final String V_1_0 = "1.0";
    private static final String V_1_1 = "1.1";

    public ConnectablePositionXmlSerializer() {
        super(ConnectablePosition.NAME, ConnectablePosition.class, "cp",
                ImmutableMap.<IidmXmlVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmXmlVersion.V_1_0, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_1, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_2, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_3, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_4, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_5, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_6, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_7, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .put(IidmXmlVersion.V_1_8, ImmutableSortedSet.of(V_1_0, V_1_1))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put(V_1_0, "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0")
                        .put(V_1_1, "http://www.powsybl.org/schema/iidm/ext/connectable_position/1_1")
                        .build());
    }

    private void writePosition(String connectableId, ConnectablePosition.Feeder feeder, Integer i, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeStartNode(context.getExtensionVersion(ConnectablePosition.NAME)
                .map(this::getNamespaceUri)
                .orElseGet(this::getNamespaceUri), "feeder" + (i != null ? i : ""));
        String extVersionStr = context.getExtensionVersion(ConnectablePosition.NAME)
                .orElseGet(() -> getVersion(context.getVersion()));
        switch (extVersionStr) {
            case V_1_0:
                context.getWriter().writeStringAttribute("name", feeder.getName().orElse(connectableId));
                break;
            case V_1_1:
                feeder.getName().ifPresent(name -> context.getWriter().writeStringAttribute("name", name));
                break;
            default:
                throw new PowsyblException("Unsupported version (" + extVersionStr + ") for " + ConnectablePosition.NAME);
        }
        Optional<Integer> oOrder = feeder.getOrder();
        oOrder.ifPresent(integer -> context.getWriter().writeIntAttribute("order", integer));
        context.getWriter().writeStringAttribute("direction", feeder.getDirection().name());
        context.getWriter().writeEndNode();
    }

    @Override
    public void write(ConnectablePosition<C> connectablePosition, XmlWriterContext context) {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        String connectableId = connectablePosition.getExtendable().getId();
        if (connectablePosition.getFeeder() != null) {
            writePosition(connectableId, connectablePosition.getFeeder(), null, networkContext);
        }
        if (connectablePosition.getFeeder1() != null) {
            writePosition(connectableId, connectablePosition.getFeeder1(), 1, networkContext);
        }
        if (connectablePosition.getFeeder2() != null) {
            writePosition(connectableId, connectablePosition.getFeeder2(), 2, networkContext);
        }
        if (connectablePosition.getFeeder3() != null) {
            writePosition(connectableId, connectablePosition.getFeeder3(), 3, networkContext);
        }
    }

    private void readPosition(XmlReaderContext context, ConnectablePositionAdder.FeederAdder<C> adder) {
        String name = context.getReader().readStringAttribute("name");
        Optional.ofNullable(context.getReader().readIntAttribute("order")).
                ifPresent(adder::withOrder);
        ConnectablePosition.Direction direction = context.getReader().readEnumAttribute("direction", ConnectablePosition.Direction.class);
        if (name != null) {
            adder.withName(name);
        } else {
            NetworkXmlReaderContext networkXmlReaderContext = (NetworkXmlReaderContext) context;
            String extensionVersionStr = networkXmlReaderContext.getExtensionVersion(this).orElseThrow(AssertionError::new);
            if (V_1_1.compareTo(extensionVersionStr) > 0) {
                throw new PowsyblException("Feeder name is mandatory for version < 1.1");
            }
        }
        adder.withDirection(direction).add();
    }

    @Override
    public ConnectablePosition<C> read(C connectable, XmlReaderContext context) {
        ConnectablePositionAdder<C> adder = connectable.newExtension(ConnectablePositionAdder.class);
        context.getReader().readUntilEndNode(getExtensionName(), () -> {

            switch (context.getReader().getNodeName()) {
                case "feeder":
                    readPosition(context, adder.newFeeder());
                    break;

                case "feeder1":
                    readPosition(context, adder.newFeeder1());
                    break;

                case "feeder2":
                    readPosition(context, adder.newFeeder2());
                    break;

                case "feeder3":
                    readPosition(context, adder.newFeeder3());
                    break;

                default:
                    throw new AssertionError();
            }
        });
        return adder.add();
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/connectablePosition_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/connectablePosition_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/connectablePosition_V1_0.xsd"));
    }
}
