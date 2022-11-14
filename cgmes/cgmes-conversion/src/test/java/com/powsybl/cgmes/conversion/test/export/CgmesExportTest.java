/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportTest {

    @Test
    public void testFromIidm() throws IOException {
        // Test from IIDM with configuration that does not exist in CGMES (disconnected node on switch and HVDC line)

        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vl = network.getVoltageLevel("C");

        // Add disconnected node on switch (side 2)
        vl.getNodeBreakerView().newSwitch().setId("TEST_SW")
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setNode1(0)
                .setNode2(6)
                .add();

        // Add disconnected node on DC converter station (side 2)
        vl.newVscConverterStation()
                .setId("C1")
                .setNode(5)
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(5).add();
        vl.newVscConverterStation()
                .setId("C2")
                .setNode(6)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("hvdc_line")
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1("C1")
                .setConverterStationId2("C2")
                .add();

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            network.write("CGMES", null, tmpDir.resolve("tmp"));
            Network n2 = Network.read(new GenericReadOnlyDataSource(tmpDir, "tmp"));
            VoltageLevel c = n2.getVoltageLevel("C");
            assertNull(Networks.getEquivalentTerminal(c, c.getNodeBreakerView().getNode2("TEST_SW")));
            assertNull(n2.getVscConverterStation("C2").getTerminal().getBusView().getBus());
        }
    }

    @Test
    public void testSynchronousMachinesWithSameGeneratingUnit() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseBEGenUnitWithTwoSyncMachines().dataSource();
        Network n = Importers.importData("CGMES", ds, null);
        String exportFolder = "/test-gu-with-2sm";
        String baseName = "testGU2SMs";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            // Export to CGMES and add boundary EQ for reimport
            n.write("CGMES", null, tmpDir.resolve(baseName));
            String eqbd = ds.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
            if (eqbd != null) {
                try (InputStream is = ds.newInputStream(eqbd)) {
                    Files.copy(is, tmpDir.resolve(baseName + "_EQ_BD.xml"));
                }
            }

            Network n2 = Network.read(new GenericReadOnlyDataSource(tmpDir, baseName), null);
            Generator g1 = n2.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
            Generator g2 = n2.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
            String gu1 = g1.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            String gu2 = g1.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            assertEquals(gu1, gu2);
        }
    }

    @Test
    public void testPhaseTapChangerFixedTapNotExported() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network n = Importers.importData("CGMES", ds, null);
        TwoWindingsTransformer transformer = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        String regulatingControlId = "5fc492ab-fe33-423b-84f1-a47f87552427";
        String exportFolder = "/test-ptc-rc-not-exported";
        String baseName = "testPtcRcNotExported";

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));

            // With original regulating mode the regulating control should be written in the EQ output
            String baseNameWithRc = baseName + "-with-rc";
            n.write("CGMES", null, tmpDir.resolve(baseNameWithRc));
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameWithRc, "EQ"));
            assertTrue(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameWithRc, "SSH"));

            transformer.getPhaseTapChanger().setRegulating(false);
            transformer.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
            String baseNameNoRc = baseName + "-no-rc";
            n.write("CGMES", null, tmpDir.resolve(baseNameNoRc));
            assertFalse(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameNoRc, "EQ"));
            assertFalse(cgmesFileContainsRegulatingControl(regulatingControlId, tmpDir, baseNameNoRc, "SSH"));
        }
    }

    private static boolean cgmesFileContainsRegulatingControl(String regulatingControlId, Path folder, String baseName, String instanceFile) throws XMLStreamException, IOException {
        String file = String.format("%s_%s.xml", baseName, instanceFile);
        String rdfIdAttributeName;
        String expectedRdfIdAttributeValue;
        if (instanceFile.equals("EQ")) {
            rdfIdAttributeName = "ID";
            expectedRdfIdAttributeValue = "_" + regulatingControlId;
        } else if (instanceFile.equals("SSH")) {
            rdfIdAttributeName = "about";
            expectedRdfIdAttributeValue = "#_" + regulatingControlId;
        } else {
            return false;
        }
        return xmlFileContainsRegulatingControl(expectedRdfIdAttributeValue, rdfIdAttributeName, folder.resolve(file));
    }

    private static boolean xmlFileContainsRegulatingControl(String expectedRdfIdAttributeValue, String rdfIdAttributeName, Path file) throws IOException, XMLStreamException {
        try (InputStream is = Files.newInputStream(file)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("TapChangerControl")) {
                    String id = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, rdfIdAttributeName);
                    if (expectedRdfIdAttributeValue.equals(id)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        }
        return false;
    }

    @Test
    public void testPhaseTapChangerType16() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        String transformerId = "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";
        String phaseTapChangerId = "6ebbef67-3061-4236-a6fd-6ccc4595f6c3";
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 16);
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 100);
    }

    @Test
    public void testPhaseTapChangerType14() throws IOException {
        ReadOnlyDataSource ds = Cim14SmallCasesCatalog.m7buses().dataSource();
        String transformerId = "FP.AND11-FTDPRA11-1_PT";
        String phaseTapChangerId = "FP.AND11-FTDPRA11-1_PTC_OR";
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 16);
        testPhaseTapChangerType(ds, transformerId, phaseTapChangerId, 100);
    }

    private static void testPhaseTapChangerType(ReadOnlyDataSource ds, String transformerId, String phaseTapChangerId, int cimVersion) throws IOException {
        Network network = Importers.importData("CGMES", ds, null);
        String exportFolder = "/test-ptc-type";
        String baseName = "testPtcType";
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer(transformerId);
        String typeOriginal = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));

            // When exporting only SSH (or SSH and SV), original type of tap changer should be kept
            Properties paramsOnlySsh = new Properties();
            paramsOnlySsh.put(CgmesExport.PROFILES, List.of("SSH"));
            paramsOnlySsh.put(CgmesExport.CIM_VERSION, "" + cimVersion);
            network.write("CGMES", paramsOnlySsh, tmpDir.resolve(baseName));
            String typeOnlySsh = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
            assertEquals(typeOriginal, typeOnlySsh);

            // If we export EQ and SSH (or all instance fiels), type of tap changer should be changed to tabular
            Properties paramsEqAndSsh = new Properties();
            paramsEqAndSsh.put(CgmesExport.CIM_VERSION, "" + cimVersion);
            network.write("CGMES", paramsEqAndSsh, tmpDir.resolve(baseName));
            String typeEqAndSsh = CgmesExportUtil.cgmesTapChangerType(transformer, phaseTapChangerId).orElseThrow(RuntimeException::new);
            assertEquals(CgmesNames.PHASE_TAP_CHANGER_TABULAR, typeEqAndSsh);
        }
    }

    @Test
    public void testDoNotExportFictitiousSwitchesCreatedForDisconnectedTerminals() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.miniNodeBreakerTerminalDisconnected().dataSource();
        Network network = Importers.importData("CGMES", ds, null);

        String disconnectedTerminalId = "4dec53ca-3ea6-4bd0-a225-b559c8293e91";
        String fictitiousSwitchId = "4dec53ca-3ea6-4bd0-a225-b559c8293e91_SW_fict";

        // Verify that a fictitious switch has been created for the disconnected terminal
        Switch fictitiousSwitch = network.getSwitch(fictitiousSwitchId);
        assertNotNull(fictitiousSwitch);
        assertTrue(fictitiousSwitch.isFictitious());
        assertTrue(fictitiousSwitch.isOpen());
        assertEquals("true", fictitiousSwitch.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));

        String exportFolder = "/test-terminal-disconnected-fictitious-switch";
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            // Export to CGMES and add boundary EQ for reimport
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            String baseName = "testTerminalDisconnectedFictitiousSwitchExported";
            ReadOnlyDataSource exportedCgmes = exportAndAddBoundaries(network, tmpDir, baseName, ds);

            // Check that the exported CGMES model does not contain the fictitious switch
            // And that the corresponding terminal is disconnected
            CgmesModel cgmes = CgmesModelFactory.create(exportedCgmes, TripleStoreFactory.defaultImplementation());
            assertTrue(cgmes.isNodeBreaker());
            assertFalse(cgmes.switches().stream().anyMatch(sw -> sw.getId("Switch").equals(fictitiousSwitchId)));
            assertFalse(cgmes.terminal(disconnectedTerminalId).connected());

            // Verify that the fictitious switch is created again when we re-import the exported CGMES data
            Network networkReimported = Network.read(exportedCgmes, null);
            Switch fictitiousSwitchReimported = networkReimported.getSwitch(fictitiousSwitchId);
            assertNotNull(fictitiousSwitchReimported);
            assertTrue(fictitiousSwitchReimported.isFictitious());
            assertTrue(fictitiousSwitchReimported.isOpen());
            assertEquals("true", fictitiousSwitch.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));

            // Verify that if close the switch the terminal is exported as connected
            // And the fictitious switch is not crated when re-importing
            fictitiousSwitch.setOpen(false);
            String baseName1 = "testTerminalDisconnectedFictitiousSwitchClosedExported";
            ReadOnlyDataSource exportedCgmes1 = exportAndAddBoundaries(network, tmpDir, baseName1, ds);
            CgmesModel cgmes1 = CgmesModelFactory.create(exportedCgmes1, TripleStoreFactory.defaultImplementation());
            assertTrue(cgmes1.isNodeBreaker());
            assertFalse(cgmes1.switches().stream().anyMatch(sw -> sw.getId("Switch").equals(fictitiousSwitchId)));
            assertTrue(cgmes1.terminal(disconnectedTerminalId).connected());
            Network networkReimported1 = Network.read(exportedCgmes1, null);
            Switch fictitiousSwitchReimported1 = networkReimported1.getSwitch(fictitiousSwitchId);
            assertNull(fictitiousSwitchReimported1);
        }
    }

    private static ReadOnlyDataSource exportAndAddBoundaries(Network network, Path tmpDir, String baseName, ReadOnlyDataSource originalDataSource) throws IOException {
        network.write("CGMES", null, tmpDir.resolve(baseName));
        String eqbd = originalDataSource.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
        if (eqbd != null) {
            try (InputStream is = originalDataSource.newInputStream(eqbd)) {
                Files.copy(is, tmpDir.resolve(baseName + "_EQ_BD.xml"));
            }
        }
        return new GenericReadOnlyDataSource(tmpDir, baseName);
    }
}
