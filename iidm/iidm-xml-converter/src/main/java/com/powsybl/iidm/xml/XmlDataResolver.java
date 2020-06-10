/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.util.Arrays;
import java.util.List;

import com.powsybl.commons.datastore.AbstractDataResolver;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class XmlDataResolver extends AbstractDataResolver {

    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml", "iidm.xml"};

    private static final String DATA_FORMAT_ID = "XIIDM";

    @Override
    public String getDataFormatId() {
        return DATA_FORMAT_ID;
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

}