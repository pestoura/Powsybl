/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.TreeDataWriter;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.anonymizer.Anonymizer;

import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlWriterContext extends AbstractNetworkXmlContext<ExportOptions> implements XmlWriterContext {

    private final TreeDataWriter writer;
    private final ExportOptions options;
    private final BusFilter filter;
    private final boolean valid;
    private final Set<Identifiable> exportedEquipments;

    NetworkXmlWriterContext(Anonymizer anonymizer, TreeDataWriter writer, ExportOptions options, BusFilter filter, IidmXmlVersion version, boolean valid) {
        super(anonymizer, version);
        this.writer = Objects.requireNonNull(writer);
        this.options = options;
        this.filter = filter;
        this.valid = valid;
        this.exportedEquipments = new HashSet<>();
    }

    NetworkXmlWriterContext(Anonymizer anonymizer, XmlWriter writer, ExportOptions options, BusFilter filter, IidmXmlVersion version) {
        this(anonymizer, writer, options, filter, version, true);
    }

    NetworkXmlWriterContext(Anonymizer anonymizer, XmlWriter writer, ExportOptions options, BusFilter filter) {
        this(anonymizer, writer, options, filter, IidmXmlConstants.CURRENT_IIDM_XML_VERSION);
    }

    @Override
    public TreeDataWriter getWriter() {
        return writer;
    }

    @Override
    public ExportOptions getOptions() {
        return options;
    }

    public BusFilter getFilter() {
        return filter;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * @deprecated Should not be used anymore.
     */
    @Deprecated
    public void setExtensionsWriter(XMLStreamWriter extensionsWriter) {
        // does nothing
        // only kept to prevent breaking change
    }

    public Set<Identifiable> getExportedEquipments() {
        return Collections.unmodifiableSet(exportedEquipments);
    }

    public void addExportedEquipment(Identifiable<?> equipment) {
        exportedEquipments.add(equipment);
    }

    public boolean isExportedEquipment(Identifiable<?> equipment) {
        return exportedEquipments.contains(equipment);
    }

    public Optional<String> getExtensionVersion(String extensionName) {
        return options.getExtensionVersion(extensionName);
    }
}
