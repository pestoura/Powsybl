/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.util;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringAnonymizerTest {

    private static String toCsv(StringAnonymizer anonymizer) throws IOException {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        try {
            anonymizer.writeCsv(writer);
        } finally {
            writer.close();
        }
        return stringWriter.toString();
    }

    private static StringAnonymizer fromCsv(String csv) throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            anonymizer.readCsv(reader);
        }
        return anonymizer;
    }

    @Test
    public void test() throws IOException {
        StringAnonymizer anonymizer = new StringAnonymizer();
        String anonymizedFoo = anonymizer.anonymize("foo");
        String anonymizedBar = anonymizer.anonymize("bar");
        assertNotEquals(anonymizedFoo, anonymizedBar);
        assertEquals("A", anonymizedFoo);
        assertEquals("B", anonymizedBar);
        assertEquals(anonymizedFoo, anonymizer.anonymize("foo"));
        assertEquals("foo", anonymizer.deanonymize(anonymizedFoo));
        assertEquals("bar", anonymizer.deanonymize(anonymizedBar));
        assertNull(anonymizer.anonymize(null));
        assertNull(anonymizer.deanonymize(null));
        try {
            anonymizer.deanonymize("baz");
            fail();
        } catch (Exception e) {
        }
        String csv = toCsv(anonymizer);
        assertEquals(String.join(System.lineSeparator(), "foo;A", "bar;B") + System.lineSeparator(),
                     csv.toString());
        StringAnonymizer anonymizer2 = fromCsv(csv);
        assertEquals("foo", anonymizer2.deanonymize(anonymizedFoo));
        assertEquals("bar", anonymizer2.deanonymize(anonymizedBar));
    }
}