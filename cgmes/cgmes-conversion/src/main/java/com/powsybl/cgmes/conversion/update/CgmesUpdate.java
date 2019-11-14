/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.Profiling;
import com.powsybl.cgmes.conversion.update.elements16.IidmToCgmes16;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesUpdate {

    public CgmesUpdate(Network network) {
        this.changelog = new Changelog(network);
    }

    public Changelog changelog() {
        return changelog;
    }

    /**
     * Update the CGMES model given as a parameter. This update only supports CGMES
     * models implemented on a Triplestore.
     *
     * @param cgmes CGMES model to be updated
     * @param variantId Update CGMES model with changes made in this variant
     */
    public void update(CgmesModel cgmes, String variantId, Profiling profiling) {
        requireCgmesModelTripleStore(cgmes);
        CgmesModelTripleStore cgmests = (CgmesModelTripleStore) cgmes;
        List<IidmChange> changes = changelog.getChangesForVariant(variantId);
        if (changes.isEmpty()) {
            return;
        }
        UpdateContext context = new UpdateContext(cgmests, profiling);
        for (IidmChange change : changes) {
            List<TripleStoreChange> tsChanges = convert(change, context);
            update(cgmests, tsChanges, context);
        }
    }

    private void requireCgmesModelTripleStore(CgmesModel cgmes) {
        if (!(cgmes instanceof CgmesModelTripleStore)) {
            throw new ConversionException("Unsupported conversion to CGMES model implementation " + cgmes.getClass().getSimpleName());
        }
    }

    private List<TripleStoreChange> convert(IidmChange change, UpdateContext context) {
        // Right now we only know how to deal with update changes
        requireChangeIsUpdate(change);
        IidmToCgmes c = findConversion(change, context);
        if (c == null) {
            LOG.error("Unsupported conversion for IIDM change {}", change);
            return Collections.emptyList();
        }
        return c.convert(change);
    }

    private void requireChangeIsUpdate(IidmChange change) {
        if (!(change instanceof IidmChangeUpdate)) {
            throw new ConversionException("Unsupported conversion to CGMES for IIDM change type " + change.getClass().getSimpleName());
        }
    }

    private IidmToCgmes findConversion(IidmChange change, UpdateContext context) {
        if (context.cimVersion == 16) {
            return IIDM_TO_CGMES16.findConversion(change);
        }
        return null;
    }

    private void update(CgmesModelTripleStore ts, List<TripleStoreChange> tsChanges, UpdateContext context) {
        for (TripleStoreChange tsChange : tsChanges) {
            context.profiling.startLoopIteration();
            String predicate = tsChange.params().predicate();
            String newValue = tsChange.params().value();
            String valueIsNode = String.valueOf(tsChange.params().valueIsNode());
            ts.update(
                tsChange.queryName(),
                context.context(tsChange.params().context()),
                context.basename,
                tsChange.subject(),
                predicate,
                newValue,
                valueIsNode);
            context.profiling.endLoopIteration();
        }
    }

    private static class UpdateContext {
        UpdateContext(CgmesModelTripleStore cgmes, Profiling profiling) {
            this.basename = cgmes.getBasename();
            computeActualContexts(cgmes.tripleStore());
            this.profiling = profiling;
            this.cimVersion = cgmes.getCimVersion();
        }

        String context(String context) {
            return actualContexts.get(context);
        }

        private Map<String, String> computeActualContexts(TripleStore ts) {
            for (String context : ts.contextNames()) {
                if (context.toUpperCase().contains("BD")
                    || context.toUpperCase().contains("BOUNDARY")) {
                    continue;
                }
                if (context.toUpperCase().contains("_SSH")) {
                    actualContexts.put("_SSH", context);
                }
                if (context.toUpperCase().contains("_EQ")) {
                    actualContexts.put("_EQ", context);
                }
                if (context.toUpperCase().contains("_SV")) {
                    actualContexts.put("_SV", context);
                }
                if (context.toUpperCase().contains("_TP")) {
                    actualContexts.put("_TP", context);
                }
            }
            return actualContexts;
        }

        private final String basename;
        private final Map<String, String> actualContexts = new HashMap<>();
        private final Profiling profiling;
        private final int cimVersion;
    }

    private Changelog changelog;

    private static final IidmToCgmes16 IIDM_TO_CGMES16 = new IidmToCgmes16();

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdate.class);
}
