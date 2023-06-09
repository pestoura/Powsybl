/**
 * Copyright (c) 2018-1019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationType;
import org.assertj.core.api.Assertions;

import static com.powsybl.iidm.network.util.LimitViolationUtils.PERMANENT_LIMIT_NAME;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class DefaultLimitViolationDetectorTest {

    private Network network;
    private Line line1;
    private Line line2;
    private VoltageLevel voltageLevelHv1;
    private LimitViolationDetector detector;
    private List<LimitViolation> violationsCollector;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");
        voltageLevelHv1 = network.getVoltageLevel("VLHV1");

        detector = new DefaultLimitViolationDetector();
        violationsCollector = new ArrayList<>();
    }

    @Test
    void detectPermanentLimitOverloadOnSide2OfLine1() {

        detector.checkCurrent(line1, Branch.Side.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void testLimitReductionOnCurrentPermanentLimit() {
        final double i = 460;
        Optional<? extends LoadingLimits> line1Limits = line1.getLimits(LimitType.CURRENT, Branch.Side.ONE);
        assertTrue(line1Limits.isPresent()
            && line1Limits.get().getTemporaryLimits().isEmpty()
            && line1Limits.get().getPermanentLimit() > i); // no overload expected

        // no violation if limitReduction is 1
        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(line1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        assertTrue(violationsCollector.isEmpty());

        // violation reported if limitReduction is 0.9
        cdetector = new DefaultLimitViolationDetector(0.9f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(line1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(500, l.getLimit(), 0);
                    assertEquals(460, l.getValue(), 0);
                    assertEquals(0.9f, l.getLimitReduction());
                });
    }

    @Test
    void detectTemporaryLimitOverloadOnSide2OfLine1() {

        detector.checkCurrent(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighestTemporaryLimitOverloadOnSide1OfLine2() {

        detector.checkCurrent(line2, Branch.Side.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(Branch.Side.ONE, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighVoltageViolation() {

        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> detector.checkVoltage(b, 520, violationsCollector::add));

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.HIGH_VOLTAGE, v.getLimitType());
                    assertEquals(500, v.getLimit(), 0d);
                    assertEquals(520, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    void detectLowVoltageViolation() {

        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> detector.checkVoltage(b, 380, violationsCollector::add));

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.LOW_VOLTAGE, v.getLimitType());
                    assertEquals(400, v.getLimit(), 0d);
                    assertEquals(380, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    void detectPermanentActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");

        detector.checkPermanentLimit(line1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectPermanentApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");

        detector.checkPermanentLimit(line1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                      assertEquals(1.0f, l.getLimitReduction());
                  });
    }

    @Test
    void detectTemporaryActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, Branch.Side.TWO, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectTemporaryApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, Branch.Side.TWO, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkActivePower(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                  });
    }

    @Test
    void detectAllApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkApparentPower(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getSide());
                  });
    }

    @Test
    void testCheckLimitViolationUnsupportedVoltage() {

        assertThrows(UnsupportedOperationException.class, () -> {
            network = EurostagTutorialExample1Factory.createWithFixedLimits();
            line1 = network.getLine("NHV1_NHV2_1");

            DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
            cdetector.checkLimitViolation(line1, Branch.Side.ONE, 1201, violationsCollector::add, LimitType.VOLTAGE);
        });
    }
}
