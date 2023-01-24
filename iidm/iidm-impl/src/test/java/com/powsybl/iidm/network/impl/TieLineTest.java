/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TieLineTest {

    @Test
    public void tieLineTest0() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv0 = createCase0();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.ONE, caseSv0);
        TieLine tieLine = (TieLine) n.getLine("TWO + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv0, initialModelCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO, Branch.Side.ONE);
        assertTrue(compare(sv2, caseSv0.node2, caseSv0.line2, Branch.Side.ONE, isv2));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv0, initialModelCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO, Branch.Side.ONE);
        assertTrue(compare(sv1, caseSv0.node1, caseSv0.line1, Branch.Side.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv0, initialModelCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO);
        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv0.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv0.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv0, initialModelCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.ONE);
        assertTrue(compare(caseSv0.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv0.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv0.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv0.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    public void tieLineTest1() {

        // Line1 from node1 to boundaryNode, Line2 from node2 to boundaryNode
        CaseSv caseSv1 = createCase1();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.TWO, caseSv1);
        TieLine tieLine = (TieLine) n.getLine("TWO + TWO");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv1, initialModelCase(Branch.Side.TWO, Branch.Side.TWO), Branch.Side.TWO, Branch.Side.TWO);
        assertTrue(compare(sv2, caseSv1.node2, caseSv1.line2, Branch.Side.TWO, isv2));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv1, initialModelCase(Branch.Side.TWO, Branch.Side.TWO), Branch.Side.TWO, Branch.Side.TWO);
        assertTrue(compare(sv1, caseSv1.node1, caseSv1.line1, Branch.Side.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv1, initialModelCase(Branch.Side.TWO, Branch.Side.TWO), Branch.Side.TWO);
        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv1.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv1.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv1, initialModelCase(Branch.Side.TWO, Branch.Side.TWO), Branch.Side.TWO);
        assertTrue(compare(caseSv1.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv1.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv1.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv1.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    public void tieLineTest2() {

        // Line1 from boundaryNode to node1, Line2 from boundaryNode to node2
        CaseSv caseSv2 = createCase2();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.ONE, Branch.Side.ONE, caseSv2);
        TieLine tieLine = (TieLine) n.getLine("ONE + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv2, initialModelCase(Branch.Side.ONE, Branch.Side.ONE), Branch.Side.ONE, Branch.Side.ONE);
        assertTrue(compare(sv2, caseSv2.node2, caseSv2.line2, Branch.Side.ONE, isv2));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv2, initialModelCase(Branch.Side.ONE, Branch.Side.ONE), Branch.Side.ONE, Branch.Side.ONE);
        assertTrue(compare(sv1, caseSv2.node1, caseSv2.line1, Branch.Side.ONE, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv2, initialModelCase(Branch.Side.ONE, Branch.Side.ONE), Branch.Side.ONE);
        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv2.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv2.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv2, initialModelCase(Branch.Side.ONE, Branch.Side.ONE), Branch.Side.ONE);
        assertTrue(compare(caseSv2.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv2.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv2.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv2.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    public void tieLineTest3() {

        // Line1 from boundaryNode to node1, Line2 from node2 to boundaryNode
        CaseSv caseSv3 = createCase3();
        Network n = createNetworkWithTieLine(NetworkFactory.findDefault(), Branch.Side.ONE, Branch.Side.TWO, caseSv3);
        TieLine tieLine = (TieLine) n.getLine("ONE + TWO");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv3, initialModelCase(Branch.Side.ONE, Branch.Side.TWO), Branch.Side.ONE, Branch.Side.TWO);
        assertTrue(compare(sv2, caseSv3.node2, caseSv3.line2, Branch.Side.TWO, isv2));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv3, initialModelCase(Branch.Side.ONE, Branch.Side.TWO), Branch.Side.ONE, Branch.Side.TWO);
        assertTrue(compare(sv1, caseSv3.node1, caseSv3.line1, Branch.Side.ONE, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv3, initialModelCase(Branch.Side.ONE, Branch.Side.TWO), Branch.Side.ONE);
        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv3.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv3.line1, Branch.Side.ONE), tieLine.getHalf1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv3, initialModelCase(Branch.Side.ONE, Branch.Side.TWO), Branch.Side.TWO);
        assertTrue(compare(caseSv3.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv3.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv3.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv3.line2, Branch.Side.TWO), tieLine.getHalf2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    public void tieLineWithDifferentNominalVoltageAtEndsTest() {

        // Line1 from node1 to boundaryNode, Line2 from boundaryNode to node2
        CaseSv caseSv = createCaseDifferentNominalVoltageAtEnds();
        Network n = createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory.findDefault(), Branch.Side.TWO, Branch.Side.ONE, caseSv);
        TieLine tieLine = (TieLine) n.getLine("TWO + ONE");

        SV sv2 = new SV(tieLine.getTerminal1().getP(), tieLine.getTerminal1().getQ(),
            tieLine.getTerminal1().getBusView().getBus().getV(),
            tieLine.getTerminal1().getBusView().getBus().getAngle(),
            Branch.Side.ONE).otherSide(tieLine);
        SV isv2 = initialSv2(caseSv, initialModelDifferentVlCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO, Branch.Side.ONE);
        assertTrue(compare(sv2, caseSv.node2, caseSv.line2, Branch.Side.ONE, isv2));

        SV sv1 = new SV(tieLine.getTerminal2().getP(), tieLine.getTerminal2().getQ(),
            tieLine.getTerminal2().getBusView().getBus().getV(),
            tieLine.getTerminal2().getBusView().getBus().getAngle(),
            Branch.Side.TWO).otherSide(tieLine);
        SV isv1 = initialSv1(caseSv, initialModelDifferentVlCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO, Branch.Side.ONE);
        assertTrue(compare(sv1, caseSv.node1, caseSv.line1, Branch.Side.TWO, isv1));

        SV isvHalf1 = initialHalf1SvBoundary(caseSv, initialModelDifferentVlCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.TWO);
        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getHalf1().getBoundary().getV(), isvHalf1.getU()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getHalf1().getBoundary().getAngle(), isvHalf1.getA()));
        assertTrue(compare(getP(caseSv.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getP(), isvHalf1.getP()));
        assertTrue(compare(getQ(caseSv.line1, Branch.Side.TWO), tieLine.getHalf1().getBoundary().getQ(), isvHalf1.getQ()));

        SV isvHalf2 = initialHalf2SvBoundary(caseSv, initialModelDifferentVlCase(Branch.Side.TWO, Branch.Side.ONE), Branch.Side.ONE);
        assertTrue(compare(caseSv.nodeBoundary.v, tieLine.getHalf2().getBoundary().getV(), isvHalf2.getU()));
        assertTrue(compare(caseSv.nodeBoundary.a, tieLine.getHalf2().getBoundary().getAngle(), isvHalf2.getA()));
        assertTrue(compare(getP(caseSv.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getP(), isvHalf2.getP()));
        assertTrue(compare(getQ(caseSv.line2, Branch.Side.ONE), tieLine.getHalf2().getBoundary().getQ(), isvHalf2.getQ()));
    }

    @Test
    public void testDefaultValuesTieLine() {

        Network network = NoEquipmentNetworkFactory.create();

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(1.0)
                .setLowVoltageLimit(0.95)
                .setHighVoltageLimit(1.05)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS");

        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
                .setId("S2VL1")
                .setNominalV(1.0)
                .setLowVoltageLimit(0.95)
                .setHighVoltageLimit(1.05)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s2vl1, "S2VL1-BUS");

        Branch.Side boundarySide1 = Branch.Side.ONE;
        Branch.Side boundarySide2 = Branch.Side.TWO;
        TieLineAdder adder = network.newTieLine()
                .setId(boundarySide1.name() + " + " + boundarySide2.name())
                .setName(boundarySide1.name() + " + " + boundarySide2.name())
                .newHalf1()
                    .setId(boundarySide1.name())
                    .setName(boundarySide1.name())
                    .setR(1.0)
                    .setX(2.0)
                    .setBus("S1VL1-BUS")
                    .setUcteXnodeCode("UcteNode")
                .add()
                .newHalf2()
                    .setId(boundarySide2.name())
                    .setName(boundarySide2.name())
                    .setR(1.0)
                    .setX(2.0)
                    .setBus("S2VL1-BUS")
                    .setUcteXnodeCode("UcteNode")
                .add();

        TieLine tieLine = adder.add();

        assertEquals(0.0, tieLine.getHalf1().getG(), 0.0);
        assertEquals(0.0, tieLine.getHalf1().getB(), 0.0);
        assertEquals(0.0, tieLine.getHalf2().getG(), 0.0);
        assertEquals(0.0, tieLine.getHalf2().getB(), 0.0);

        assertSame(s1vl1, tieLine.getTerminal1().getVoltageLevel());
        assertSame(s2vl1, tieLine.getTerminal2().getVoltageLevel());
    }

    private static Network createNetworkWithTieLine(NetworkFactory networkFactory,
        Branch.Side boundarySide1, Branch.Side boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(1.0)
                .setLowVoltageLimit(0.95)
                .setHighVoltageLimit(1.05)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS");

        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(1.0)
            .setLowVoltageLimit(0.95)
            .setHighVoltageLimit(1.05)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        createBus(s2vl1, "S2VL1-BUS");

        // The initial parameters for AcLineSegment 1 are R = 0.019, X = 0.059, G1 = 0.02, B1 = 0.075, G2 = 0.03, B2 = 0.065
        // The initial parameters for AcLineSegment 2 are R = 0.038, X = 0.118, G1 = 0.015,B1 = 0.050, G2 = 0.025,B2 = 0.080
        // AcLinesegment 1 must be reoriented if boundary side is at end 1
        // AcLinesegment 2 must be reoriented if boundary side is at end 2
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient the AcLineSegments

        TieLineAdder adder = network.newTieLine()
            .setId(boundarySide1.name() + " + " + boundarySide2.name())
            .setName(boundarySide1.name() + " + " + boundarySide2.name())
            .newHalf1()
            .setBus("S1VL1-BUS")
            .setId(boundarySide1.name())
            .setName(boundarySide1.name())
            .setR(0.019)
            .setX(0.059)
            .setG(0.05)
            .setB(0.14)
            .add()
            .newHalf2()
            .setBus("S2VL1-BUS")
            .setId(boundarySide2.name())
            .setName(boundarySide2.name())
            .setR(0.038)
            .setX(0.118)
            .setG(0.04)
            .setB(0.13)
            .setUcteXnodeCode("UcteNode")
            .add();

        adder.setVoltageLevel1("S1VL1")
            .setVoltageLevel2("S2VL1");

        TieLine tieLine = adder.add();
        tieLine.getTerminal1().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getTerminal1().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getTerminal1().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getTerminal1().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getTerminal2().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getTerminal2().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getTerminal2().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getTerminal2().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static Network createNetworkWithTieLineWithDifferentNominalVoltageAtEnds(NetworkFactory networkFactory,
        Branch.Side boundarySide1, Branch.Side boundarySide2, CaseSv caseSv) {

        Network network = networkFactory.createNetwork("TieLine-BusBreaker", "test");
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(138.0)
                .setLowVoltageLimit(110.0)
                .setHighVoltageLimit(150.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        createBus(s1vl1, "S1VL1-BUS");

        Substation s2 = network.newSubstation()
            .setId("S2")
            .add();
        VoltageLevel s2vl1 = s2.newVoltageLevel()
            .setId("S2VL1")
            .setNominalV(220.0)
            .setLowVoltageLimit(195.0)
            .setHighVoltageLimit(240.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        createBus(s2vl1, "S2VL1-BUS");

        // The initial parameters for AcLineSegment 1 are R = 2.1672071999999996, X = 9.5543748, G1 = 0.0, B1 = 1.648813274522159E-4, G2 = 0.0, B2 = 1.648813274522159E-4
        // AcLinesegment 1 must be reoriented if boundary side is at end 1
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient it

        // The initial parameters for AcLineSegment 2 are R = 3.1513680000000006, X = 14.928011999999999, G1 = 0.008044414674299755, B1 = -0.03791520949675112, G2 = -0.005046041932060755, B2 = 0.023978278075869598
        // AcLinesegment 2 must be reoriented if boundary side is at end 2
        // Current model does not allow shunt admittances at both ends, so it does not make sense to reorient it
        TieLineAdder adder = network.newTieLine()
            .setId(boundarySide1.name() + " + " + boundarySide2.name())
            .setName(boundarySide1.name() + " + " + boundarySide2.name())
            .newHalf1()
            .setBus("S1VL1-BUS")
            .setId(boundarySide1.name())
            .setName(boundarySide1.name())
            .setR(2.1672071999999996)
            .setX(9.5543748)
            .setG(0.0)
            .setB(0.00032976265)
            .setUcteXnodeCode("UcteNode")
            .add()
            .newHalf2()
            .setBus("S2VL1-BUS")
            .setId(boundarySide2.name())
            .setName(boundarySide2.name())
            .setR(3.1513680000000006)
            .setX(14.928011999999999)
            .setG(0.00299837274)
            .setB(-0.01393693142)
            .add();

        adder.setVoltageLevel1("S1VL1")
            .setVoltageLevel2("S2VL1");

        TieLine tieLine = adder.add();
        tieLine.getTerminal1().getBusView().getBus().setV(caseSv.node1.v);
        tieLine.getTerminal1().getBusView().getBus().setAngle(caseSv.node1.a);
        tieLine.getTerminal1().setP(getOtherSideP(caseSv.line1, boundarySide1));
        tieLine.getTerminal1().setQ(getOtherSideQ(caseSv.line1, boundarySide1));

        tieLine.getTerminal2().getBusView().getBus().setV(caseSv.node2.v);
        tieLine.getTerminal2().getBusView().getBus().setAngle(caseSv.node2.a);
        tieLine.getTerminal2().setP(getOtherSideP(caseSv.line2, boundarySide2));
        tieLine.getTerminal2().setQ(getOtherSideQ(caseSv.line2, boundarySide2));

        return network;
    }

    private static void createBus(VoltageLevel voltageLevel, String id) {
        voltageLevel.getBusBreakerView()
            .newBus()
            .setName(id)
            .setId(id)
            .add();
    }

    private static double getOtherSideP(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.p2;
        } else {
            return line.p1;
        }
    }

    private static double getOtherSideQ(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.q2;
        } else {
            return line.q1;
        }
    }

    private static double getP(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.p1;
        } else {
            return line.p2;
        }
    }

    private static double getQ(LineSv line, Branch.Side boundarySide) {
        if (boundarySide == Branch.Side.ONE) {
            return line.q1;
        } else {
            return line.q2;
        }
    }

    // We define an error by value to adjust the case. The error is calculated by difference between
    // the calculated value with both models, the initial model of the case and the current model of the danglingLine
    // Errors are due to the danglingLine model (it does not allow shunt admittance at both ends)
    private static boolean compare(SV sv, NodeSv nodeSv, LineSv lineSv, Branch.Side boundarySide, SV initialModelSv) {
        double tol = 0.00001;
        double errorP = initialModelSv.getP() - sv.getP();
        double errorQ = initialModelSv.getQ() - sv.getQ();
        double errorU = initialModelSv.getU() - sv.getU();
        double errorA = initialModelSv.getA() - sv.getA();
        if (Math.abs(sv.getP() - getOtherSideP(lineSv, boundarySide)) > tol + Math.abs(errorP)) {
            return false;
        }
        if (Math.abs(sv.getQ() - getOtherSideQ(lineSv, boundarySide)) > tol + Math.abs(errorQ)) {
            return false;
        }
        if (Math.abs(sv.getU() - nodeSv.v) > tol + Math.abs(errorU)) {
            return false;
        }
        if (Math.abs(sv.getA() - nodeSv.a) > tol + Math.abs(errorA)) {
            return false;
        }
        return true;
    }

    // We define an error to adjust the case. The error is calculated by difference between
    // the calculated value with both models, the initial model of the case and the current model of the danglingLine
    // Errors are due to the danglingLine model (it does not allow shunt admittance at both ends)
    private static boolean compare(double expected, double actual, double initialActual) {
        double tol = 0.00001;
        double error = initialActual - actual;
        if (Math.abs(actual - expected) > tol + Math.abs(error)) {
            return false;
        }
        return true;
    }

    // Line1 from node1 to nodeBoundary, Line2 from nodeBoundary to node2
    private static CaseSv createCase0() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05913402, Math.toDegrees(-0.01700730));
        NodeSv node2 = new NodeSv(1.04546576, Math.toDegrees(-0.04168907));

        LineSv line1 = new LineSv(0.32101578, -0.16210107, -0.26328124, 0.00991455);
        LineSv line2 = new LineSv(0.26328124, -0.00991455, -0.21700000, -0.12700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from node1 to nodeBoundary, Line2 from node2 to nodeBoundary
    private static CaseSv createCase1() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05916756, Math.toDegrees(-0.01702560));
        NodeSv node2 = new NodeSv(1.04216358, Math.toDegrees(-0.03946400));

        LineSv line1 = new LineSv(0.32116645, -0.16274609, -0.26342655, 0.01056498);
        LineSv line2 = new LineSv(-0.21700000, -0.12700000, 0.26342655, -0.01056498);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from nodeBoundary to node2
    private static CaseSv createCase2() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.05998661, Math.toDegrees(-0.01660626));
        NodeSv node2 = new NodeSv(1.04634503, Math.toDegrees(-0.04125738));

        LineSv line1 = new LineSv(-0.26335112, 0.01016197, 0.32106283, -0.16270573);
        LineSv line2 = new LineSv(0.26335112, -0.01016197, -0.21700000, -0.12700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from node2 to nodeBoundary
    private static CaseSv createCase3() {
        NodeSv node1 = new NodeSv(1.06000000, Math.toDegrees(0.0));
        NodeSv nodeBoundary = new NodeSv(1.06002014, Math.toDegrees(-0.01662448));
        NodeSv node2 = new NodeSv(1.04304009, Math.toDegrees(-0.03903205));

        LineSv line1 = new LineSv(-0.26349561, 0.01081185, 0.32121215, -0.16335034);
        LineSv line2 = new LineSv(-0.21700000, -0.12700000, 0.26349561, -0.01081185);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    // Line1 from nodeBoundary to node1, Line2 from node2 to nodeBoundary
    // Line1 from node1 to nodeBoundary, Line2 from nodeBoundary to node2
    // Different nominal voltage at node1 and node2
    private static CaseSv createCaseDifferentNominalVoltageAtEnds() {
        NodeSv node1 = new NodeSv(145.2861673277147, Math.toDegrees(-0.01745197));
        NodeSv nodeBoundary = new NodeSv(145.42378472578227, Math.toDegrees(-0.02324020));
        NodeSv node2 = new NodeSv(231.30269602522478, Math.toDegrees(-0.02818192));

        LineSv line1 = new LineSv(11.729938, -8.196614, -11.713527, 1.301712);
        LineSv line2 = new LineSv(11.713527, -1.301712, -11.700000, -6.700000);
        return new CaseSv(node1, node2, nodeBoundary, line1, line2);
    }

    private static final class CaseSv {
        private final NodeSv node1;
        private final NodeSv node2;
        private final NodeSv nodeBoundary;
        private final LineSv line1;
        private final LineSv line2;

        private CaseSv(NodeSv node1, NodeSv node2, NodeSv nodeBoundary, LineSv line1, LineSv line2) {
            this.node1 = node1;
            this.node2 = node2;
            this.nodeBoundary = nodeBoundary;
            this.line1 = line1;
            this.line2 = line2;
        }
    }

    private static final class NodeSv {
        private final double v;
        private final double a;

        private NodeSv(double v, double a) {
            this.v = v;
            this.a = a;
        }
    }

    private static final class LineSv {
        private final double p1;
        private final double q1;
        private final double p2;
        private final double q2;

        private LineSv(double p1, double q1, double p2, double q2) {
            this.p1 = p1;
            this.q1 = q1;
            this.p2 = p2;
            this.q2 = q2;
        }
    }

    private static SV initialSv1(CaseSv initialCase, TieLineInitialModel tlim, Branch.Side half1Boundary, Branch.Side half2Boundary) {
        return new SV(getOtherSideP(initialCase.line2, half2Boundary),
            getOtherSideQ(initialCase.line2, half2Boundary),
            initialCase.node2.v, initialCase.node2.a, Branch.Side.TWO).otherSide(tlim.tieLine.r, tlim.tieLine.x,
                tlim.tieLine.g1, tlim.tieLine.b1, tlim.tieLine.g2, tlim.tieLine.b2, 1.0, 0.0);
    }

    private static SV initialSv2(CaseSv initialCase, TieLineInitialModel tlim, Branch.Side half1Boundary, Branch.Side half2Boundary) {
        return new SV(getOtherSideP(initialCase.line1, half1Boundary),
            getOtherSideQ(initialCase.line1, half1Boundary),
            initialCase.node1.v, initialCase.node1.a, Branch.Side.ONE).otherSide(tlim.tieLine.r, tlim.tieLine.x,
                tlim.tieLine.g1, tlim.tieLine.b1, tlim.tieLine.g2, tlim.tieLine.b2, 1.0, 0.0);
    }

    private static SV initialHalf1SvBoundary(CaseSv initialCase, TieLineInitialModel tlim, Branch.Side half1Boundary) {
        return new SV(getOtherSideP(initialCase.line1, half1Boundary),
            getOtherSideQ(initialCase.line1, half1Boundary),
            initialCase.node1.v, initialCase.node1.a,
            half1Boundary.equals(Branch.Side.ONE) ? Branch.Side.TWO : Branch.Side.ONE).otherSide(tlim.half1.r,
                tlim.half1.x, tlim.half1.g1, tlim.half1.b1, tlim.half1.g2, tlim.half1.b2, 1.0, 0.0);
    }

    private static SV initialHalf2SvBoundary(CaseSv initialCase, TieLineInitialModel tlim, Branch.Side half2Boundary) {
        return new SV(getOtherSideP(initialCase.line2, half2Boundary),
            getOtherSideQ(initialCase.line2, half2Boundary),
            initialCase.node2.v, initialCase.node2.a,
            half2Boundary.equals(Branch.Side.ONE) ? Branch.Side.TWO : Branch.Side.ONE).otherSide(tlim.half2.r,
                tlim.half2.x, tlim.half2.g1, tlim.half2.b1, tlim.half2.g2, tlim.half2.b2, 1.0, 0.0);
    }

    private static TieLineInitialModel initialModelCase(Branch.Side half1Boundary, Branch.Side half2Boundary) {
        return new TieLineInitialModel(new LineInitialModel(0.019, 0.059, 0.02, 0.075, 0.03, 0.065), half1Boundary,
            new LineInitialModel(0.038, 0.118, 0.015, 0.050, 0.025, 0.080), half2Boundary);
    }

    private static TieLineInitialModel initialModelDifferentVlCase(Branch.Side half1Boundary, Branch.Side half2Boundary) {
        return new TieLineInitialModel(
            new LineInitialModel(2.1672071999999996, 9.5543748, 0.0, 1.648813274522159E-4, 0.0, 1.648813274522159E-4), half1Boundary,
            new LineInitialModel(3.1513680000000006, 14.928011999999999, 0.008044414674299755, -0.03791520949675112,
                -0.005046041932060755, 0.023978278075869598), half2Boundary);
    }

    private static final class TieLineInitialModel {
        private final LineInitialModel half1;
        private final LineInitialModel half2;
        private final LineInitialModel tieLine;

        private TieLineInitialModel(LineInitialModel half1, Branch.Side half1Boundary, LineInitialModel half2, Branch.Side half2Boundary) {
            this.half1 = half1;
            this.half2 = half2;

            BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(half1.r, half1.x, 1.0, 0.0, 1.0, 0.0,
                new Complex(half1.g1, half1.b1), new Complex(half1.g2, half1.b2));
            BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(half2.r, half2.x, 1.0, 0.0, 1.0, 0.0,
                new Complex(half2.g1, half2.b1), new Complex(half2.g2, half2.b2));

            BranchAdmittanceMatrix adm = LinkData.kronChain(adm1, half1Boundary, adm2, half2Boundary);
            this.tieLine = new LineInitialModel(adm.y12().negate().reciprocal().getReal(),
                adm.y12().negate().reciprocal().getImaginary(),
                adm.y11().add(adm.y12()).getReal(),
                adm.y11().add(adm.y12()).getImaginary(),
                adm.y22().add(adm.y21()).getReal(),
                adm.y22().add(adm.y21()).getImaginary());
        }
    }

    private static final class LineInitialModel {
        private final double r;
        private final double x;
        private final double g1;
        private final double b1;
        private final double g2;
        private final double b2;

        private LineInitialModel(double r, double x, double g1, double b1, double g2, double b2) {
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
        }
    }
}
