/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.*;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.NetworkMetadata;
import com.powsybl.security.results.*;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.results.OperatorStrategyResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class SecurityAnalysisResultDeserializer extends StdDeserializer<SecurityAnalysisResult> {

    private static final String CONTEXT_NAME = "SecurityAnalysisResult";
    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    SecurityAnalysisResultDeserializer() {
        super(SecurityAnalysisResult.class);
    }

    @Override
    public SecurityAnalysisResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String version = null;
        NetworkMetadata networkMetadata = null;
        LimitViolationsResult limitViolationsResult = null;
        List<PostContingencyResult> postContingencyResults = Collections.emptyList();
        List<Extension<SecurityAnalysisResult>> extensions = Collections.emptyList();
        PreContingencyResult preContingencyResult = null;
        List<OperatorStrategyResult> operatorStrategyResults = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(ctx, version, SOURCE_VERSION_ATTRIBUTE);
                    break;

                case "network":
                    parser.nextToken();
                    networkMetadata = parser.readValueAs(NetworkMetadata.class);
                    break;

                case "preContingencyResult":
                    parser.nextToken();
                    if (version != null && version.equals("1.0")) {
                        limitViolationsResult = ctx.readValue(parser, LimitViolationsResult.class);
                    } else {
                        preContingencyResult = ctx.readValue(parser, PreContingencyResult.class);
                    }
                    break;

                case "postContingencyResults":
                    parser.nextToken();
                    JavaType postContingencyResultsCollection = ctx.getTypeFactory().constructCollectionType(List.class, PostContingencyResult.class);
                    postContingencyResults = ctx.readValue(parser, postContingencyResultsCollection);
                    break;

                case "operatorStrategyResults":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: operatorStrategyResults", version, "1.2");
                    parser.nextToken();
                    JavaType operatorStrategyResultsCollection = ctx.getTypeFactory().constructCollectionType(List.class, OperatorStrategyResult.class);
                    operatorStrategyResults = ctx.readValue(parser, operatorStrategyResultsCollection);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, ctx, SUPPLIER.get());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        SecurityAnalysisResult result = null;
        if (preContingencyResult == null) {
            result = new SecurityAnalysisResult(limitViolationsResult, postContingencyResults, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), operatorStrategyResults);
        } else {
            result = new SecurityAnalysisResult(preContingencyResult, postContingencyResults, operatorStrategyResults);
        }
        result.setNetworkMetadata(networkMetadata);
        SUPPLIER.get().addExtensions(result, extensions);

        return result;
    }

    public static SecurityAnalysisResult read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SecurityAnalysisResult read(InputStream is) {
        Objects.requireNonNull(is);

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule());
        try {
            return objectMapper.readValue(is, SecurityAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
