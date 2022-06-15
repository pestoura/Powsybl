/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Optional;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.DanglingLine;

import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DanglingLineData {

    private final DanglingLine danglingLine;

    double boundaryBusU;
    double boundaryBusTheta;

    double boundaryP;
    double boundaryQ;
    double p;
    double q;

    public DanglingLineData(DanglingLine danglingLine) {
        this(danglingLine, true);
    }

    public DanglingLineData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        Optional<DanglingLine.Generation> generation = Optional.ofNullable(danglingLine.getGeneration());
        double targetP = generation.isPresent() ? generation.get().getTargetP() : 0.0;
        double targetQ = generation.isPresent() ? generation.get().getTargetQ() : 0.0;
        // Flow at the boundary side
        boundaryP = -(danglingLine.getP0() - targetP);
        boundaryQ = -(danglingLine.getQ0() - targetQ);

        boolean isZ0 = isZ0(danglingLine);
        Complex pq = networkPQZ0(danglingLine, isZ0);
        p = pq.getReal();
        q = pq.getImaginary();

        double u1 = getV(danglingLine);
        double theta1 = getTheta(danglingLine);

        if (!valid(u1, theta1)) {
            boundaryBusU = Double.NaN;
            boundaryBusTheta = Double.NaN;
            return;
        }

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);

        Complex vBoundaryBus = boundaryVoltageAndAngle(v1, isZ0, splitShuntAdmittance);
        boundaryBusU = vBoundaryBus.abs();
        boundaryBusTheta = vBoundaryBus.getArgument();

        pq = networkPQ(danglingLine, v1, vBoundaryBus, isZ0, splitShuntAdmittance);
        p = pq.getReal();
        q = pq.getImaginary();
    }

    private Complex boundaryVoltageAndAngle(Complex v1, boolean isZ0, boolean splitShuntAdmittance) {

        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;

        Complex vBoundaryBus = new Complex(Double.NaN, Double.NaN);
        if (isZ0) {
            vBoundaryBus = v1;
        } else if (boundaryP == 0.0 && boundaryQ == 0.0) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(danglingLine.getR(), danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(g1, b1), new Complex(g2, b2));
            vBoundaryBus = adm.y21().multiply(v1).negate().divide(adm.y22());
        } else {

            // Two buses Loadflow
            Complex sBoundary = new Complex(boundaryP, boundaryQ);
            Complex ytr = new Complex(danglingLine.getR(), danglingLine.getX()).reciprocal();

            Complex ysh2 = new Complex(g2, b2);
            Complex zt = ytr.add(ysh2).reciprocal();
            Complex v0 = ytr.multiply(v1).divide(ytr.add(ysh2));
            double v02 = v0.abs() * v0.abs();

            Complex sigma = zt.multiply(sBoundary.conjugate()).multiply(1.0 / v02);
            double d = 0.25 + sigma.getReal() - sigma.getImaginary() * sigma.getImaginary();
            // d < 0 Collapsed network
            if (d >= 0) {
                vBoundaryBus = new Complex(0.5 + Math.sqrt(d), sigma.getImaginary()).multiply(v0);
            }
        }
        return vBoundaryBus;
    }

    private Complex networkPQZ0(DanglingLine danglingLine, boolean isZ0) {
        double networkP = Double.NaN;
        double networkQ = Double.NaN;

        if (!Double.isNaN(danglingLine.getTerminal().getP()) && !Double.isNaN(danglingLine.getTerminal().getQ())) {
            networkP = danglingLine.getTerminal().getP();
            networkQ = danglingLine.getTerminal().getQ();
        } else if (isZ0) {
            networkP = -boundaryP;
            networkQ = -boundaryQ;
        }
        return new Complex(networkP, networkQ);
    }

    private Complex networkPQ(DanglingLine danglingLine, Complex vNetwork, Complex vBoundary, boolean isZ0, boolean splitShuntAdmittance) {
        double networkP = Double.NaN;
        double networkQ = Double.NaN;

        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;

        if ((Double.isNaN(danglingLine.getTerminal().getP()) || Double.isNaN(danglingLine.getTerminal().getQ())) && !isZ0) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(danglingLine.getR(), danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(g1, b1), new Complex(g2, b2));
            Complex s1 = adm.y11().multiply(vNetwork).add(adm.y12().multiply(vBoundary)).multiply(vNetwork.conjugate()).conjugate();
            networkP = s1.getReal();
            networkQ = s1.getImaginary();
        }
        return new Complex(networkP, networkQ);
    }

    private static boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private static double getV(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    private static double getTheta(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected()
            ? Math.toRadians(danglingLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean valid(double v, double theta) {
        if (Double.isNaN(v) || v <= 0.0) {
            return false;
        }
        return !Double.isNaN(theta);
    }

    public String getId() {
        return danglingLine.getId();
    }

    public double getBoundaryBusU() {
        return boundaryBusU;
    }

    public double getBoundaryBusTheta() {
        return boundaryBusTheta;
    }

    public double getP() {
        return p;
    }

    public double getQ() {
        return q;
    }
}
