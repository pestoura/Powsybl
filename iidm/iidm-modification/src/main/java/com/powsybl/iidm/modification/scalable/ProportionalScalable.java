/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Scalable that divides scale proportionally between multiple scalable.
 * Scale may be iterative or not.
 * If the iterative mode is activated, the residues due to scalable saturation is divided between the
 * other scalable composing the ProportionalScalable.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class ProportionalScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-2;

    private static final class ScalablePercentage {
        private final Scalable scalable;
        private final float percentage;
        private double iterationPercentage;

        private ScalablePercentage(Scalable scalable, float percentage) {
            this.scalable = scalable;
            this.percentage = percentage;
            this.iterationPercentage = percentage;
        }

        Scalable getScalable() {
            return scalable;
        }

        float getPercentage() {
            return percentage;
        }

        boolean isSaturated() {
            return iterationPercentage == 0.0;
        }

        boolean notSaturated() {
            return iterationPercentage != 0.0;
        }

        double getIterationPercentage() {
            return iterationPercentage;
        }

        void setIterationPercentage(double iterationPercentage) {
            this.iterationPercentage = iterationPercentage;
        }
    }

    private final List<ScalablePercentage> scalablePercentageList;

    private final boolean iterative;

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        this(percentages, scalables, false);
    }

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables, boolean iterative) {
        super(-Double.MAX_VALUE, Double.MAX_VALUE, ScalingConvention.GENERATOR);
        checkPercentages(percentages, scalables);
        this.scalablePercentageList = new ArrayList<>();
        for (int i = 0; i < scalables.size(); i++) {
            this.scalablePercentageList.add(new ScalablePercentage(scalables.get(i), percentages.get(i)));
        }
        this.iterative = iterative;
        scalableActivityMap = scalables.stream().collect(Collectors.toMap(scalable -> scalable, scalable -> true, (first, second) -> first));
    }

    @Override
    public AbstractCompoundScalable shallowCopy() {
        List<Float> percentages = new ArrayList<>();
        List<Scalable> scalables = new ArrayList<>();
        Set<Scalable> deactivatedScalables = new HashSet<>();
        for (ScalablePercentage scalablePercentage : scalablePercentageList) {
            percentages.add(scalablePercentage.getPercentage());
            Scalable scalable = scalablePercentage.getScalable();
            if (scalable instanceof CompoundScalable) {
                scalables.add(((CompoundScalable) scalable).shallowCopy());
            } else {
                scalables.add(scalable);
            }
            if (Boolean.FALSE.equals(scalableActivityMap.get(scalable))) {
                deactivatedScalables.add(scalable);
            }
        }
        ProportionalScalable proportionalScalable = new ProportionalScalable(percentages, scalables, iterative);
        proportionalScalable.deactivateScalables(deactivatedScalables);
        return proportionalScalable;
    }

    private static void checkPercentages(List<Float> percentages, List<Scalable> scalables) {
        Objects.requireNonNull(percentages);
        Objects.requireNonNull(scalables);

        if (scalables.size() != percentages.size()) {
            throw new IllegalArgumentException("percentage and scalable list must have the same size");
        }
        if (scalables.isEmpty()) {
            return;
        }
        if (percentages.stream().anyMatch(p -> Float.isNaN(p))) {
            throw new IllegalArgumentException("There is at least one undefined percentage");
        }
        double sum = percentages.stream().mapToDouble(Double::valueOf).sum();
        if (Math.abs(100 - sum) > EPSILON) {
            throw new IllegalArgumentException(String.format("Sum of percentages must be equals to 100 (%.2f)", sum));
        }
    }

    private boolean notSaturated() {
        return scalablePercentageList.stream().anyMatch(ScalablePercentage::notSaturated);
    }

    private void checkIterationPercentages() {
        double iterationPercentagesSum = scalablePercentageList.stream()
            .filter(scalablePercentage -> scalableActivityMap.get(scalablePercentage.getScalable()))
            .mapToDouble(ScalablePercentage::getIterationPercentage).sum();
        if (Math.abs(100 - iterationPercentagesSum) > EPSILON) {
            throw new AssertionError(String.format("Error in proportional scalable ventilation. Sum of percentages must be equals to 100 (%.2f)", iterationPercentagesSum));
        }
    }

    private void updateIterationPercentages() {
        double unsaturatedPercentagesSum = scalablePercentageList.stream()
            .filter(scalablePercentage -> scalablePercentage.notSaturated() && scalableActivityMap.get(scalablePercentage.getScalable()))
            .mapToDouble(ScalablePercentage::getIterationPercentage).sum();
        scalablePercentageList.forEach(scalablePercentage -> {
            if (!scalablePercentage.isSaturated() && Boolean.TRUE.equals(scalableActivityMap.get(scalablePercentage.getScalable()))) {
                scalablePercentage.setIterationPercentage(scalablePercentage.getIterationPercentage() / unsaturatedPercentagesSum * 100);
            }
        });
    }

    private double iterativeScale(Network n, double asked, ScalingConvention scalingConvention, boolean constantPowerFactor) {
        double done = 0;
        while (Math.abs(asked - done) > EPSILON && notSaturated()) {
            checkIterationPercentages();
            done += scaleIteration(n, asked - done, scalingConvention, constantPowerFactor);
            updateIterationPercentages();
        }
        return done;
    }

    private double scaleIteration(Network n, double asked, ScalingConvention scalingConvention, boolean constantPowerFactor) {
        AtomicReference<Double> done = new AtomicReference<>(0.);
        scalablePercentageList.stream().filter(scalablePercentage -> scalableActivityMap.get(scalablePercentage.getScalable())).forEach(scalablePercentage -> {
            Scalable s = scalablePercentage.getScalable();
            double iterationPercentage = scalablePercentage.getIterationPercentage();
            double askedOnScalable = iterationPercentage / 100 * asked;
            double doneOnScalable = 0;
            if (constantPowerFactor) {
                doneOnScalable = s.scaleWithConstantPowerFactor(n, askedOnScalable, scalingConvention);
            } else {
                doneOnScalable = s.scale(n, askedOnScalable, scalingConvention);
            }
            if (Math.abs(doneOnScalable - askedOnScalable) > EPSILON) {
                scalablePercentage.setIterationPercentage(0);
            }
            done.set(done.get() + doneOnScalable);
        });
        return done.get();
    }

    @Override
    public double scaleWithConstantPowerFactor(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);
        reinitIterationPercentage();
        updateIterationPercentages();
        if (iterative) {
            return iterativeScale(n, asked, scalingConvention, true);
        } else {
            return scaleIteration(n, asked, scalingConvention, true);
        }
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(scalingConvention);
        double done = 0;
        double remaining = asked;
        double oldScaled = this.getCurrentInjection(n, scalingConvention) - this.getInitialInjection(scalingConvention);
        //if oldScaled and asked are of opposite signs
        if (oldScaled * asked < -1e-6) {
            this.reset(n);
            done = -oldScaled;
            remaining += oldScaled;
        }
        reinitIterationPercentage();
        updateIterationPercentages();
        if (iterative) {
            return done + iterativeScale(n, remaining, scalingConvention, false);
        } else {
            return done + scaleIteration(n, remaining, scalingConvention, false);
        }
    }

    private void reinitIterationPercentage() {
        scalablePercentageList.forEach(scalablePercentage -> scalablePercentage.setIterationPercentage(scalablePercentage.getPercentage()));
    }

}

