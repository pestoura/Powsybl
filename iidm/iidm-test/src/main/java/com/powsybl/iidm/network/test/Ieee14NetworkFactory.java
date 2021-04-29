package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * This is a network test based on IEEE 14 PSSE raw file
 *
 * @author Jean-Baptiste Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
public final class Ieee14NetworkFactory {

    private Ieee14NetworkFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("ieee14", "test");
        Substation s6 = network.newSubstation()
                .setId("S6")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s8 = network.newSubstation()
                .setId("S8")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s7 = network.newSubstation()
                .setId("S7")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s1 = network.newSubstation()
                .setId("S1")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s3 = network.newSubstation()
                .setId("S3")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s9 = network.newSubstation()
                .setId("S9")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s10 = network.newSubstation()
                .setId("S10")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s11 = network.newSubstation()
                .setId("S11")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s4 = network.newSubstation()
                .setId("S4")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        Substation s5 = network.newSubstation()
                .setId("S5")
                //.setCountry(Country.FR)
                //.setTso("RTE")
                //.setGeographicalTags("A")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl2 = s8.newVoltageLevel()
                .setId("VL2")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl3 = s7.newVoltageLevel()
                .setId("VL3")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl4 = s1.newVoltageLevel()
                .setId("VL4")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl7 = s1.newVoltageLevel()
                .setId("VL7")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl9 = s1.newVoltageLevel()
                .setId("VL9")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl5 = s2.newVoltageLevel()
                .setId("VL5")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl6 = s2.newVoltageLevel()
                .setId("VL6")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl8 = s3.newVoltageLevel()
                .setId("VL8")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl10 = s9.newVoltageLevel()
                .setId("VL10")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl11 = s10.newVoltageLevel()
                .setId("VL11")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl12 = s11.newVoltageLevel()
                .setId("VL12")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl13 = s4.newVoltageLevel()
                .setId("VL13")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vl14 = s5.newVoltageLevel()
                .setId("VL14")
                .setNominalV(138.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();

        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        Bus b3 = vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        Bus b4 = vl4.getBusBreakerView().newBus()
                .setId("B4")
                .add();
        Bus b7 = vl7.getBusBreakerView().newBus()
                .setId("B7")
                .add();
        Bus b9 = vl9.getBusBreakerView().newBus()
                .setId("B9")
                .add();
        Bus b5 = vl5.getBusBreakerView().newBus()
                .setId("B5")
                .add();
        Bus b6 = vl6.getBusBreakerView().newBus()
                .setId("B6")
                .add();
        Bus b8 = vl8.getBusBreakerView().newBus()
                .setId("B8")
                .add();
        Bus b10 = vl10.getBusBreakerView().newBus()
                .setId("B10")
                .add();
        Bus b11 = vl11.getBusBreakerView().newBus()
                .setId("B11")
                .add();
        Bus b12 = vl12.getBusBreakerView().newBus()
                .setId("B12")
                .add();
        Bus b13 = vl13.getBusBreakerView().newBus()
                .setId("B13")
                .add();
        Bus b14 = vl14.getBusBreakerView().newBus()
                .setId("B14")
                .add();
        network.newLine()
                .setId("L-1-2-1 ")
                .setVoltageLevel1(vl1.getId())
                .setBus1(b1.getId())
                .setConnectableBus1(b1.getId())
                .setVoltageLevel2(vl2.getId())
                .setBus2(b2.getId())
                .setConnectableBus2(b2.getId())
                .setR(3.6907272)
                .setX(11.2683348)
                .setG1(0.0)
                .setB1(1.3862633900441084E-4)
                .setG2(0.0)
                .setB2(1.3862633900441084E-4)
                .add();
        network.newLine()
                .setId("L-1-5-1 ")
                .setVoltageLevel1(vl1.getId())
                .setBus1(b1.getId())
                .setConnectableBus1(b1.getId())
                .setVoltageLevel2(vl5.getId())
                .setBus2(b5.getId())
                .setConnectableBus2(b5.getId())
                .setR(10.2894732)
                .setX(42.475737599999995)
                .setG1(0.0)
                .setB1(1.2917454316320101E-4)
                .setG2(0.0)
                .setB2(1.2917454316320101E-4)
                .add();
        network.newLine()
                .setId("L-2-3-1 ")
                .setVoltageLevel1(vl2.getId())
                .setBus1(b2.getId())
                .setConnectableBus1(b2.getId())
                .setVoltageLevel2(vl3.getId())
                .setBus2(b3.getId())
                .setConnectableBus2(b3.getId())
                .setR(8.9487756)
                .setX(37.7014068)
                .setG1(0.0)
                .setB1(1.1499684940138626E-4)
                .setG2(0.0)
                .setB2(1.1499684940138626E-4)
                .add();
        network.newLine()
                .setId("L-2-4-1 ")
                .setVoltageLevel1(vl2.getId())
                .setBus1(b2.getId())
                .setConnectableBus1(b2.getId())
                .setVoltageLevel2(vl4.getId())
                .setBus2(b4.getId())
                .setConnectableBus2(b4.getId())
                .setR(11.066468400000002)
                .setX(33.578380800000005)
                .setG1(0.0)
                .setB1(8.926696072253729E-5)
                .setG2(0.0)
                .setB2(8.926696072253729E-5)
                .add();
        network.newLine()
                .setId("L-2-5-1 ")
                .setVoltageLevel1(vl2.getId())
                .setBus1(b2.getId())
                .setConnectableBus1(b2.getId())
                .setVoltageLevel2(vl5.getId())
                .setBus2(b5.getId())
                .setConnectableBus2(b5.getId())
                .setR(10.845557999999999)
                .setX(33.1137072)
                .setG1(0.0)
                .setB1(9.084226002940558E-5)
                .setG2(0.0)
                .setB2(9.084226002940558E-5)
                .add();
        network.newLine()
                .setId("L-3-4-1 ")
                .setVoltageLevel1(vl3.getId())
                .setBus1(b3.getId())
                .setConnectableBus1(b3.getId())
                .setVoltageLevel2(vl4.getId())
                .setBus2(b4.getId())
                .setConnectableBus2(b4.getId())
                .setR(12.761384399999999)
                .setX(32.5709532)
                .setG1(0.0)
                .setB1(3.3606385213190505E-5)
                .setG2(0.0)
                .setB2(3.3606385213190505E-5)
                .add();
        network.newLine()
                .setId("L-4-5-1 ")
                .setVoltageLevel1(vl4.getId())
                .setBus1(b4.getId())
                .setConnectableBus1(b4.getId())
                .setVoltageLevel2(vl5.getId())
                .setBus2(b5.getId())
                .setConnectableBus2(b5.getId())
                .setR(2.542374)
                .setX(8.0194284)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-6-11-1 ")
                .setVoltageLevel1(vl6.getId())
                .setBus1(b6.getId())
                .setConnectableBus1(b6.getId())
                .setVoltageLevel2(vl11.getId())
                .setBus2(b11.getId())
                .setConnectableBus2(b11.getId())
                .setR(18.087991199999998)
                .setX(37.878516)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-6-12-1 ")
                .setVoltageLevel1(vl6.getId())
                .setBus1(b6.getId())
                .setConnectableBus1(b6.getId())
                .setVoltageLevel2(vl12.getId())
                .setBus2(b12.getId())
                .setConnectableBus2(b12.getId())
                .setR(23.406980400000002)
                .setX(48.7164564)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-6-13-1 ")
                .setVoltageLevel1(vl6.getId())
                .setBus1(b6.getId())
                .setConnectableBus1(b6.getId())
                .setVoltageLevel2(vl13.getId())
                .setBus2(b13.getId())
                .setConnectableBus2(b13.getId())
                .setR(12.597606)
                .setX(24.808618800000005)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-7-8-1 ")
                .setVoltageLevel1(vl7.getId())
                .setBus1(b7.getId())
                .setConnectableBus1(b7.getId())
                .setVoltageLevel2(vl8.getId())
                .setBus2(b8.getId())
                .setConnectableBus2(b8.getId())
                .setR(0.0)
                .setX(33.546006000000006)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-7-9-1 ")
                .setVoltageLevel1(vl7.getId())
                .setBus1(b7.getId())
                .setConnectableBus1(b7.getId())
                .setVoltageLevel2(vl9.getId())
                .setBus2(b9.getId())
                .setConnectableBus2(b9.getId())
                .setR(0.0)
                .setX(20.9503044)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-9-10-1 ")
                .setVoltageLevel1(vl9.getId())
                .setBus1(b9.getId())
                .setConnectableBus1(b9.getId())
                .setVoltageLevel2(vl10.getId())
                .setBus2(b10.getId())
                .setConnectableBus2(b10.getId())
                .setR(6.0578964)
                .setX(16.092180000000003)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-9-14-1 ")
                .setVoltageLevel1(vl9.getId())
                .setBus1(b9.getId())
                .setConnectableBus1(b9.getId())
                .setVoltageLevel2(vl14.getId())
                .setBus2(b14.getId())
                .setConnectableBus2(b14.getId())
                .setR(24.2068284)
                .setX(51.4911672)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-10-11-1 ")
                .setVoltageLevel1(vl10.getId())
                .setBus1(b10.getId())
                .setConnectableBus1(b10.getId())
                .setVoltageLevel2(vl11.getId())
                .setBus2(b11.getId())
                .setConnectableBus2(b11.getId())
                .setR(15.625602)
                .setX(36.5778108)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-12-13-1 ")
                .setVoltageLevel1(vl12.getId())
                .setBus1(b12.getId())
                .setConnectableBus1(b12.getId())
                .setVoltageLevel2(vl13.getId())
                .setBus2(b13.getId())
                .setConnectableBus2(b13.getId())
                .setR(42.0720048)
                .setX(38.0651472)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        network.newLine()
                .setId("L-13-14-1 ")
                .setVoltageLevel1(vl13.getId())
                .setBus1(b13.getId())
                .setConnectableBus1(b13.getId())
                .setVoltageLevel2(vl14.getId())
                .setBus2(b14.getId())
                .setConnectableBus2(b14.getId())
                .setR(32.5519092)
                .setX(66.2769288)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        TwoWindingsTransformer s1t47 = s1.newTwoWindingsTransformer()
                .setId("T-4-7-1 ")
                .setVoltageLevel1(vl4.getId())
                .setBus1(b4.getId())
                .setConnectableBus1(b4.getId())
                .setRatedU1(138.0)
                .setVoltageLevel2(vl7.getId())
                .setBus2(b7.getId())
                .setConnectableBus2(b7.getId())
                .setRatedU2(138.0)
                .setR(0.0)
                .setX(39.8248128)
                .setG(0.0)
                .setB(0.0)
                .add();
        s1t47.newRatioTapChanger()
                .beginStep()
                .setRho(1.0224948875255624)
                .setR(0.0)
                .setX(0.0)
                .setG(4.54957950159125)
                .setB(4.54957950159125)
                .endStep()
                .setTapPosition(0)
                .setLoadTapChangingCapabilities(false)
                .add();

        TwoWindingsTransformer s1t49 = s1.newTwoWindingsTransformer()
                .setId("T-4-9-1 ")
                .setVoltageLevel1(vl4.getId())
                .setBus1(b4.getId())
                .setConnectableBus1(b4.getId())
                .setRatedU1(138.0)
                .setVoltageLevel2(vl9.getId())
                .setBus2(b9.getId())
                .setConnectableBus2(b9.getId())
                .setRatedU2(138.0)
                .setR(0.0)
                .setX(105.9189192)
                .setG(0.0)
                .setB(0.0)
                .add();
        s1t49.newRatioTapChanger()
                .beginStep()
                .setRho(1.0319917440660475)
                .setR(0.0)
                .setX(0.0)
                .setG(6.500695982048255)
                .setB(6.500695982048255)
                .endStep()
                .setTapPosition(0)
                .setLoadTapChangingCapabilities(false)
                .add();

        TwoWindingsTransformer s2t56 = s2.newTwoWindingsTransformer()
                .setId("T-5-6-1 ")
                .setVoltageLevel1(vl5.getId())
                .setBus1(b5.getId())
                .setConnectableBus1(b5.getId())
                .setRatedU1(138.0)
                .setVoltageLevel2(vl6.getId())
                .setBus2(b6.getId())
                .setConnectableBus2(b6.getId())
                .setRatedU2(138.0)
                .setR(0.0)
                .setX(47.9946888)
                .setG(0.0)
                .setB(0.0)
                .add();
        s2t56.newRatioTapChanger()
                .beginStep()
                .setRho(1.0729613733905579)
                .setR(0.0)
                .setX(0.0)
                .setG(15.124610878815226)
                .setB(15.124610878815226)
                .endStep()
                .setTapPosition(0)
                .setLoadTapChangingCapabilities(false)
                .add();

        vl2.newLoad()
                .setId("B2-L1 ")
                .setBus(b2.getId())
                .setConnectableBus(b2.getId())
                .setP0(21.7)
                .setQ0(12.7)
                .add();
        vl3.newLoad()
                .setId("B3-L1 ")
                .setBus(b3.getId())
                .setConnectableBus(b3.getId())
                .setP0(94.2)
                .setQ0(19.0)
                .add();
        vl4.newLoad()
                .setId("B4-L1 ")
                .setBus(b4.getId())
                .setConnectableBus(b4.getId())
                .setP0(47.8)
                .setQ0(-3.9)
                .add();
        vl9.newLoad()
                .setId("B9-L1 ")
                .setBus(b9.getId())
                .setConnectableBus(b9.getId())
                .setP0(29.5)
                .setQ0(16.6)
                .add();
        vl5.newLoad()
                .setId("B5-L1 ")
                .setBus(b5.getId())
                .setConnectableBus(b5.getId())
                .setP0(7.6)
                .setQ0(1.6)
                .add();
        vl6.newLoad()
                .setId("B6-L1 ")
                .setBus(b6.getId())
                .setConnectableBus(b6.getId())
                .setP0(11.2)
                .setQ0(7.5)
                .add();
        vl10.newLoad()
                .setId("B10-L1 ")
                .setBus(b10.getId())
                .setConnectableBus(b10.getId())
                .setP0(9.0)
                .setQ0(5.8)
                .add();
        vl11.newLoad()
                .setId("B11-L1 ")
                .setBus(b11.getId())
                .setConnectableBus(b11.getId())
                .setP0(3.5)
                .setQ0(1.8)
                .add();
        vl12.newLoad()
                .setId("B12-L1 ")
                .setBus(b12.getId())
                .setConnectableBus(b12.getId())
                .setP0(6.1)
                .setQ0(1.6)
                .add();
        vl13.newLoad()
                .setId("B13-L1 ")
                .setBus(b13.getId())
                .setConnectableBus(b13.getId())
                .setP0(13.5)
                .setQ0(5.8)
                .add();
        vl14.newLoad()
                .setId("B14-L1 ")
                .setBus(b14.getId())
                .setConnectableBus(b14.getId())
                .setP0(14.9)
                .setQ0(5.0)
                .add();

        Generator g1 = vl1.newGenerator()
                .setId("B1-G1 ")
                .setBus(b1.getId())
                .setConnectableBus(b1.getId())
                .setMinP(-10000.0)
                .setMaxP(10000.0)
                .setVoltageRegulatorOn(false)
                .setTargetV(146.28)
                .setTargetP(232.392)
                .setTargetQ(-16.549)
                .add();
        g1.newMinMaxReactiveLimits()
                .setMinQ(0.0)
                .setMaxQ(0.0)
                .add();

        Generator g2 = vl2.newGenerator()
                .setId("B2-G1 ")
                .setBus(b2.getId())
                .setConnectableBus(b2.getId())
                .setMinP(-10000.0)
                .setMaxP(10000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(144.21)
                .setTargetP(40.0)
                .setTargetQ(43.556)
                .add();
        //g2.setRegulatingTerminal()
        g2.newMinMaxReactiveLimits()
                .setMinQ(-40.0)
                .setMaxQ(50.0)
                .add();

        Generator g3 = vl3.newGenerator()
                .setId("B3-G1 ")
                .setBus(b3.getId())
                .setConnectableBus(b3.getId())
                .setMinP(-10000.0)
                .setMaxP(10000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(139.38)
                .setTargetP(0.0)
                .setTargetQ(25.075)
                .add();
        //g3.setRegulatingTerminal()
        g3.newMinMaxReactiveLimits()
                .setMinQ(0.0)
                .setMaxQ(40.0)
                .add();

        Generator g6 = vl6.newGenerator()
                .setId("B6-G1 ")
                .setBus(b6.getId())
                .setConnectableBus(b6.getId())
                .setMinP(-10000.0)
                .setMaxP(10000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(147.66)
                .setTargetP(0.0)
                .setTargetQ(12.73)
                .add();
        //g6.setRegulatingTerminal()
        g6.newMinMaxReactiveLimits()
                .setMinQ(-6.0)
                .setMaxQ(24.0)
                .add();

        Generator g8 = vl8.newGenerator()
                .setId("B8-G1 ")
                .setBus(b8.getId())
                .setConnectableBus(b8.getId())
                .setMinP(-10000.0)
                .setMaxP(10000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(150.42)
                .setTargetP(0.0)
                .setTargetQ(17.623)
                .add();
        g8.newMinMaxReactiveLimits()
                .setMinQ(-6.0)
                .setMaxQ(24.0)
                .add();

        ShuntCompensator sh = vl9.newShuntCompensator()
                .setId("B9-SH 1")
                .setBus(b9.getId())
                .setConnectableBus(b9.getId())
                .setSectionCount(1)
                .setVoltageRegulatorOn(false)
                .newLinearModel()
                    .setMaximumSectionCount(1)
                    .setBPerSection(9.976895610165932E-4)
                .add()
                .add();

        return network;
    }

    public static Network createWithLFResults() {
        return createWithLFResults(NetworkFactory.findDefault());
    }

    public static Network createWithLFResults(NetworkFactory factory) {
        Network network = create(factory);
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00.000+01:00"));

        network.getBusBreakerView().getBus("B1")
                .setV(146.28)
                .setAngle(0.0);
        network.getBusBreakerView().getBus("B2")
                .setV(144.21)
                .setAngle(-4.9826);
        network.getBusBreakerView().getBus("B3")
                .setV(139.38)
                .setAngle(-12.725);
        network.getBusBreakerView().getBus("B4")
                .setV(140.4384)
                .setAngle(-10.3128);
        network.getBusBreakerView().getBus("B7")
                .setV(146.48976)
                .setAngle(-13.3596);
        network.getBusBreakerView().getBus("B9")
                .setV(145.71834)
                .setAngle(-14.9385);
        network.getBusBreakerView().getBus("B5")
                .setV(147.66)
                .setAngle(-14.2209);
        network.getBusBreakerView().getBus("B6")
                .setV(147.66)
                .setAngle(-14.2209);
        network.getBusBreakerView().getBus("B8")
                .setV(150.42)
                .setAngle(-13.3596);
        network.getBusBreakerView().getBus("B10")
                .setV(145.03662)
                .setAngle(-15.0972);
        network.getBusBreakerView().getBus("B11")
                .setV(145.85358)
                .setAngle(-14.7906);
        network.getBusBreakerView().getBus("B12")
                .setV(145.61622)
                .setAngle(-15.0755);
        network.getBusBreakerView().getBus("B13")
                .setV(144.95244)
                .setAngle(-15.1562);
        network.getBusBreakerView().getBus("B14")
                .setV(142.90314)
                .setAngle(-16.0336);

        return network;
    }

}
