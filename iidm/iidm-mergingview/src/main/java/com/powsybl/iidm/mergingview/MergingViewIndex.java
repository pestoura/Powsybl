/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergingViewIndex {

    /** Local storage for adapters created */
    private final Map<Identifiable<?>, AbstractAdapter<?>> identifiableAdaptersCached = new WeakHashMap<>();

    private final Map<Component, AbstractAdapter<?>> componentAdaptersCached = new WeakHashMap<>();

    private final Map<Terminal, AbstractAdapter<?>> terminalAdaptersCached = new WeakHashMap<>();

    private final Map<PhaseTapChanger, AbstractAdapter<?>> ptcAdaptersCached = new WeakHashMap<>();

    private final Map<RatioTapChanger, AbstractAdapter<?>> rtcAdaptersCached = new WeakHashMap<>();

    /** Network asked to be merged */
    private final Collection<Network> networks = new ArrayList<>();

    /** Current merging view reference */
    private final MergingView currentView;

    /** Constructor */
    MergingViewIndex(final MergingView currentView) {
        // Keep reference on current view
        this.currentView = Objects.requireNonNull(currentView);
    }

    /** @return current merging view instance */
    MergingView getView() {
        return currentView;
    }

    /** @return stream of merging network */
    Stream<Network> getNetworkStream() {
        return networks.stream();
    }

    /** Validate all networks added into merging network list */
    void checkAndAdd(final Network other) {
        // Check multi-variants network
        ValidationUtil.checkSingleyVariant(other);
        // Check unique identifiable network
        ValidationUtil.checkUniqueIds(other, this);
        // Local storage for mergeable network
        networks.add(other);
    }

    /** @return adapter according to given parameter */
    Identifiable<?> getIdentifiable(final Identifiable<?> identifiable) {
        if (identifiable instanceof Substation) {
            return getSubstation((Substation) identifiable); // container
        } else if (identifiable instanceof Bus) {
            return getBus((Bus) identifiable);
        } else if (identifiable instanceof VoltageLevel) {
            return getVoltageLevel((VoltageLevel) identifiable); // container
        } else if (identifiable instanceof Connectable) {
            return getConnectable((Connectable) identifiable);
        } else if (identifiable instanceof HvdcLine) {
            return getHvdcLine((HvdcLine) identifiable);
        } else if (identifiable instanceof Switch) {
            return getSwitch((Switch) identifiable);
        } else if (identifiable instanceof Network) {
            return currentView;
        } else {
            throw new PowsyblException(identifiable.getClass() + " type is not managed in MergingViewIndex.");
        }
    }

    /** @return all adapters according to all Identifiables */
    Stream<Identifiable<?>> getIdentifiableStream() {
        // Search Identifiables into merging & working networks
        return getNetworkStream()
                .map(Network::getIdentifiables)
                .filter(n -> !(n instanceof Network))
                .flatMap(Collection::stream)
                .map(this::getIdentifiable);
    }

    /** @return all adapters according to all Identifiables */
    Collection<Identifiable<?>> getIdentifiables() {
        // Search Identifiables into merging & working networks
        return getIdentifiableStream().collect(Collectors.toList());
    }

    /** @return all Adapters according to all Substations into merging view */
    Collection<Substation> getSubstations() {
        // Search Substations into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getSubstationStream)
                .map(this::getSubstation)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all Batteries into merging view */
    Collection<Battery> getBatteries() {
        // Search Batteries into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getBatteryStream)
                .map(this::getBattery)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all VscConverterStations into merging view */
    public Collection<VscConverterStation> getVscConverterStations() {
        // Search VscConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getVscConverterStationStream)
                .map(this::getVscConverterStation)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all TwoWindingsTransformers into merging view */
    public Collection<TwoWindingsTransformer> getTwoWindingsTransformers() {
        // Search TwoWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getTwoWindingsTransformerStream)
                .map(this::getTwoWindingsTransformer)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all Switches into merging view */
    public Collection<Switch> getSwitches() {
        // Search TwoWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getSwitchStream)
                .map(this::getSwitch)
                .collect(Collectors.toList());
    }

    /** @return all Adapters according to all StaticVarCompensators into merging view */
    public Collection<StaticVarCompensator> getStaticVarCompensators() {
        // Search StaticVarCompensator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getStaticVarCompensatorStream)
                .map(this::getStaticVarCompensator)
                .collect(Collectors.toList());
    }

    public Collection<ShuntCompensator> getShuntCompensators() {
        // Search ShuntCompensator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getShuntCompensatorStream)
                .map(this::getShuntCompensator)
                .collect(Collectors.toList());
    }

    public Collection<VoltageLevel> getVoltageLevels() {
        // Search VoltageLevel into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getVoltageLevelStream)
                .map(this::getVoltageLevel)
                .collect(Collectors.toList());
    }

    public Collection<Load> getLoads() {
        // Search Load into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getLoadStream)
                .map(this::getLoad)
                .collect(Collectors.toList());
    }

    public Collection<Generator> getGenerators() {
        // Search Generator into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getGeneratorStream)
                .map(this::getGenerator)
                .collect(Collectors.toList());
    }

    public Collection<BusbarSection> getBusbarSections() {
        // Search BusbarSection into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getBusbarSectionStream)
                .map(this::getBusbarSection)
                .collect(Collectors.toList());
    }

    public Collection<LccConverterStation> getLccConverterStations() {
        // Search LccConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getLccConverterStationStream)
                .map(this::getLccConverterStation)
                .collect(Collectors.toList());
    }

    public Collection<HvdcConverterStation<?>> getHvdcConverterStations() {
        // Search HvdcConverterStation into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getHvdcConverterStationStream)
                .map(this::getHvdcConverterStation)
                .collect(Collectors.toList());
    }

    public Collection<Branch> getBranches() {
        // Search Branch into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getBranchStream)
                .map(this::getBranch)
                .collect(Collectors.toList());
    }

    public Collection<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        // Search ThreeWindingsTransformer into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getThreeWindingsTransformerStream)
                .map(this::getThreeWindingsTransformer)
                .collect(Collectors.toList());
    }

    public Collection<Bus> getBuses() {
        // Search ThreeWindingsTransformer into merging & working networks
        return getNetworkStream()
                .map(Network::getBusBreakerView)
                .flatMap(Network.BusBreakerView::getBusStream)
                .map(this::getBus)
                .collect(Collectors.toList());
    }

    public Collection<Line> getLines() {
        // Search Line into merging & working networks
        return getNetworkStream()
                .flatMap(Network::getLineStream)
                .map(this::getLine)
                .collect(Collectors.toList());
    }

    /** @return adapter according to given Substation */
    SubstationAdapter getSubstation(final Substation substation) {
        return substation == null ? null : (SubstationAdapter) identifiableAdaptersCached.computeIfAbsent(substation, key -> new SubstationAdapter(substation, this));
    }

    /** @return adapter according to given VoltageLevel */
    VoltageLevelAdapter getVoltageLevel(final VoltageLevel vl) {
        return vl == null ? null : (VoltageLevelAdapter) identifiableAdaptersCached.computeIfAbsent(vl, key -> new VoltageLevelAdapter(vl, this));
    }

    /** @return adapter according to given Switch */
    SwitchAdapter getSwitch(final Switch sw) {
        return sw == null ? null : (SwitchAdapter) identifiableAdaptersCached.computeIfAbsent(sw, key -> new SwitchAdapter(sw, this));
    }

    /** @return adapter according to given HvdcLine */
    HvdcLineAdapter getHvdcLine(final HvdcLine hvdcLine) {
        return hvdcLine == null ? null : (HvdcLineAdapter) identifiableAdaptersCached.computeIfAbsent(hvdcLine, key -> new HvdcLineAdapter(hvdcLine, this));
    }

    /** @return adapter according to given Bus */
    BusAdapter getBus(final Bus bus) {
        return bus == null ? null : (BusAdapter) identifiableAdaptersCached.computeIfAbsent(bus, key -> new BusAdapter(bus, this));
    }

    /** @return adapter according to given TwoWindingsTransformer */
    TwoWindingsTransformerAdapter getTwoWindingsTransformer(final TwoWindingsTransformer twt) {
        return twt == null ? null : (TwoWindingsTransformerAdapter) identifiableAdaptersCached.computeIfAbsent(twt, key -> new TwoWindingsTransformerAdapter(twt, this));
    }

    ThreeWindingsTransformerAdapter getThreeWindingsTransformer(final ThreeWindingsTransformer twt) {
        return twt == null ? null : (ThreeWindingsTransformerAdapter) identifiableAdaptersCached.computeIfAbsent(twt, key -> new ThreeWindingsTransformerAdapter(twt, this));
    }

    BusbarSectionAdapter getBusbarSection(final BusbarSection bs) {
        return bs == null ? null : (BusbarSectionAdapter) identifiableAdaptersCached.computeIfAbsent(bs, key -> new BusbarSectionAdapter(bs, this));
    }

    GeneratorAdapter getGenerator(final Generator generator) {
        return generator == null ? null : (GeneratorAdapter) identifiableAdaptersCached.computeIfAbsent(generator, key -> new GeneratorAdapter(generator, this));
    }

    LoadAdapter getLoad(final Load load) {
        return load == null ? null : (LoadAdapter) identifiableAdaptersCached.computeIfAbsent(load, key -> new LoadAdapter(load, this));
    }

    BatteryAdapter getBattery(final Battery battery) {
        return battery == null ? null : (BatteryAdapter) identifiableAdaptersCached.computeIfAbsent(battery, key -> new BatteryAdapter(battery, this));
    }

    ComponentAdapter getComponent(final Component component) {
        return component == null ? null : (ComponentAdapter) componentAdaptersCached.computeIfAbsent(component, key -> new ComponentAdapter(component, this));
    }

    TerminalAdapter getTerminal(final Terminal terminal) {
        return terminal == null ? null : (TerminalAdapter) terminalAdaptersCached.computeIfAbsent(terminal, key -> new TerminalAdapter(terminal, this));
    }

    PhaseTapChangerAdapter getPhaseTapChanger(final PhaseTapChanger ptc) {
        return ptc == null ? null : (PhaseTapChangerAdapter) ptcAdaptersCached.computeIfAbsent(ptc, key -> new PhaseTapChangerAdapter(ptc, this));
    }

    RatioTapChangerAdapter getRatioTapChanger(final RatioTapChanger rtc) {
        return rtc == null ? null : (RatioTapChangerAdapter) rtcAdaptersCached.computeIfAbsent(rtc, key -> new RatioTapChangerAdapter(rtc, this));
    }

    Line getLine(final Line line) {
        return line == null ? null : (Line) identifiableAdaptersCached.computeIfAbsent(line, k -> {
            if (line.isTieLine()) {
                return new TieLineAdapter((TieLine) line, this);
            } else {
                return new LineAdapter(line, this);
            }
        });
    }

    HvdcConverterStation<?> getHvdcConverterStation(final HvdcConverterStation<?> cs) {
        if (cs == null) {
            return null;
        }
        if (cs instanceof LccConverterStation) {
            return getLccConverterStation((LccConverterStation) cs);
        } else if (cs instanceof VscConverterStation) {
            return getVscConverterStation((VscConverterStation) cs);
        } else {
            throw new PowsyblException("Invalid type " + cs.getClass() + " to be adapted");
        }
    }

    VscConverterStationAdapter getVscConverterStation(final VscConverterStation vsc) {
        return vsc == null ? null : (VscConverterStationAdapter) identifiableAdaptersCached.computeIfAbsent(vsc, key -> new VscConverterStationAdapter((VscConverterStation) key, this));
    }

    LccConverterStationAdapter getLccConverterStation(final LccConverterStation lcc) {
        return lcc == null ? null : (LccConverterStationAdapter) identifiableAdaptersCached.computeIfAbsent(lcc, key -> new LccConverterStationAdapter((LccConverterStation) key, this));
    }

    ShuntCompensatorAdapter getShuntCompensator(final ShuntCompensator shuntCompensator) {
        return shuntCompensator == null ? null
                : (ShuntCompensatorAdapter) identifiableAdaptersCached.computeIfAbsent(shuntCompensator, key -> new ShuntCompensatorAdapter((ShuntCompensator) key, this));
    }

    StaticVarCompensatorAdapter getStaticVarCompensator(final StaticVarCompensator svc) {
        return svc == null ? null : (StaticVarCompensatorAdapter) identifiableAdaptersCached.computeIfAbsent(svc, key -> new StaticVarCompensatorAdapter((StaticVarCompensator) key, this));
    }

    DanglingLineAdapter getDanglingLine(final DanglingLine dll) {
        return dll == null ? null : (DanglingLineAdapter) identifiableAdaptersCached.computeIfAbsent(dll, key -> new DanglingLineAdapter((DanglingLine) key, this));
    }

    Connectable getConnectable(final Connectable connectable) {
        if (connectable == null) {
            return null;
        }
        switch (connectable.getType()) {
            case BUSBAR_SECTION:
                return getBusbarSection((BusbarSection) connectable);
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
                return getBranch((Branch) connectable);
            case THREE_WINDINGS_TRANSFORMER:
                return getThreeWindingsTransformer((ThreeWindingsTransformer) connectable);
            case GENERATOR:
                return getGenerator((Generator) connectable);
            case BATTERY:
                return getBattery((Battery) connectable);
            case LOAD:
                return getLoad((Load) connectable);
            case SHUNT_COMPENSATOR:
                return getShuntCompensator((ShuntCompensator) connectable);
            case DANGLING_LINE:
                return getDanglingLine((DanglingLine) connectable);
            case STATIC_VAR_COMPENSATOR:
                return getStaticVarCompensator((StaticVarCompensator) connectable);
            case HVDC_CONVERTER_STATION:
                return getHvdcConverterStation((HvdcConverterStation) connectable);
            default:
                throw new AssertionError(connectable.getType().name() + " is not valid to be immutablized to connectable");
        }
    }

    Branch getBranch(final Branch b) {
        if (b == null) {
            return null;
        }
        switch (b.getType()) {
            case LINE:
                return getLine((Line) b);
            case TWO_WINDINGS_TRANSFORMER:
                return getTwoWindingsTransformer((TwoWindingsTransformer) b);
            default:
                throw new AssertionError(b.getType().name() + " is not valid to be immutablized to branch");
        }
    }
}