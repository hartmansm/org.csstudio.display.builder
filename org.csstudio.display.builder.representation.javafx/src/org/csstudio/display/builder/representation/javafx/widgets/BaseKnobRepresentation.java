/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.display.builder.representation.javafx.widgets;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.util.FormatOptionHandler;
import org.csstudio.display.builder.model.util.VTypeUtil;
import org.csstudio.display.builder.model.widgets.KnobWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.Styles;
import org.diirt.vtype.Display;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueUtil;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import se.ess.knobs.Knob;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 21 Aug 2017
 */
public abstract class BaseKnobRepresentation<C extends Knob, W extends KnobWidget> extends RegionBaseRepresentation<C, W> {

    private final DirtyFlag               dirtyContent  = new DirtyFlag();
    private final DirtyFlag               dirtyGeometry = new DirtyFlag();
    private final DirtyFlag               dirtyLimits   = new DirtyFlag();
    private final DirtyFlag               dirtyLook     = new DirtyFlag();
    private final DirtyFlag               dirtyStyle    = new DirtyFlag();
    private final DirtyFlag               dirtyUnit     = new DirtyFlag();
    private final DirtyFlag               dirtyValue    = new DirtyFlag();
    private volatile double               high          = Double.NaN;
    private volatile double               hihi          = Double.NaN;
    private volatile double               lolo          = Double.NaN;
    private volatile double               low           = Double.NaN;
    private volatile double               max           = 100.0;
    private volatile double               min           = 0.0;
    private final AtomicBoolean           updatingValue = new AtomicBoolean(false);
    private volatile boolean              firstUsage    = true;

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        if ( dirtyContent.checkAndClear() ) {
            jfx_node.setDecimals(FormatOptionHandler.actualPrecision(model_widget.runtimePropValue().getValue(), model_widget.propPrecision().getValue()));
        }

        if ( dirtyGeometry.checkAndClear() ) {
            jfx_node.setLayoutX(model_widget.propX().getValue());
            jfx_node.setLayoutY(model_widget.propY().getValue());
            jfx_node.setPrefWidth(model_widget.propWidth().getValue());
            jfx_node.setPrefHeight(model_widget.propHeight().getValue());
            jfx_node.setVisible(model_widget.propVisible().getValue());
        }

        if ( dirtyLook.checkAndClear() ) {
            jfx_node.setBackgroundColor(model_widget.propTransparent().getValue() ? Color.TRANSPARENT : JFXUtil.convert(model_widget.propBackgroundColor().getValue()));
            jfx_node.setColor(JFXUtil.convert(model_widget.propColor().getValue()));
            jfx_node.setCurrentValueColor(JFXUtil.convert(model_widget.propValueColor().getValue()));
            jfx_node.setExtremaVisible(model_widget.propExtremaVisible().getValue());
            jfx_node.setIndicatorColor(JFXUtil.convert(model_widget.propThumbColor().getValue()));
            jfx_node.setTagColor(JFXUtil.convert(model_widget.propTagColor().getValue()));
            jfx_node.setTagVisible(model_widget.propTagVisible().getValue());
            jfx_node.setTargetValueAlwaysVisible(model_widget.propTargetVisible().getValue());
            jfx_node.setTextColor(JFXUtil.convert(model_widget.propTextColor().getValue()));
        }

        if ( dirtyLimits.checkAndClear() ) {
            jfx_node.setGradientStops(computeGradientStops());
            jfx_node.setMinValue(min);
            jfx_node.setMaxValue(max);
        }

        if ( dirtyUnit.checkAndClear() ) {
            jfx_node.setUnit(getUnit());
        }

        if ( dirtyStyle.checkAndClear() ) {
            jfx_node.setDragDisabled(model_widget.propDragDisabled().getValue());
            Styles.update(jfx_node, Styles.NOT_ENABLED, !model_widget.propEnabled().getValue());
        }

        if ( dirtyValue.checkAndClear() && updatingValue.compareAndSet(false, true) ) {
            try {

                double newVal = VTypeUtil.getValueNumber(model_widget.runtimePropValue().getValue()).doubleValue();
                double rbNewVal = isReadbackPVNameValid() ? VTypeUtil.getValueNumber(model_widget.propReadbackPVValue().getValue()).doubleValue() : newVal;

                if ( !Double.isNaN(rbNewVal) ) {
                    jfx_node.setCurrentValue(clamp(rbNewVal, min, max));
                } else {
                    //  TODO: CR: do something!!!
                }

                if ( !Double.isNaN(newVal) && ( firstUsage || model_widget.propSyncedKnob().getValue() ) ) {

                    firstUsage = false;

                    jfx_node.setTargetValue(clamp(newVal, min, max));

                } else {
                    //  TODO: CR: do something!!!
                }

            } finally {
                updatingValue.set(false);
            }
        }

    }

    @Override
    protected C createJFXNode ( ) throws Exception {

        updateLimits();

        C knob = createKnob();

        knob.setOnTargetSet(e -> {
            if ( !toolkit.isEditMode() ) {
                toolkit.fireWrite(model_widget, jfx_node.getTargetValue());
            }
        });
        knob.targetValueProperty().addListener((observable, oldValue, newValue) -> {
            if ( !toolkit.isEditMode() && !model_widget.propWriteOnRelease().getValue() ) {
                toolkit.fireWrite(model_widget, newValue);
            }
        });

        dirtyContent.mark();
        dirtyGeometry.mark();
        dirtyLimits.mark();
        dirtyLook.mark();
        dirtyStyle.mark();
        dirtyUnit.mark();
        toolkit.scheduleUpdate(this);

        return knob;

    }

    protected abstract C createKnob();

    /**
     * @return The unit string to be displayed.
     */
    protected String getUnit ( ) {

        //  Model's values.
        String newUnit = model_widget.propUnit().getValue();

        if ( model_widget.propUnitFromPV().getValue() ) {

            //  Try to get engineering unit from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {
                newUnit = display_info.getUnits();
            }

        }

        return newUnit;

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propPrecision().addUntypedPropertyListener(this::contentChanged);
        model_widget.propPVName().addPropertyListener(this::contentChanged);

        model_widget.propVisible().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propX().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propY().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propWidth().addUntypedPropertyListener(this::geometryChanged);
        model_widget.propHeight().addUntypedPropertyListener(this::geometryChanged);

        model_widget.propBackgroundColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propExtremaVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propThumbColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTagColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTagVisible().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTextColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTransparent().addUntypedPropertyListener(this::lookChanged);
        model_widget.propValueColor().addUntypedPropertyListener(this::lookChanged);
        model_widget.propTargetVisible().addUntypedPropertyListener(this::lookChanged);

        model_widget.propColorHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propColorHigh().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propColorLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propColorLow().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propColorOK().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelHigh().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLevelLow().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propLimitsFromPV().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowHiHi().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowHigh().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLoLo().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propShowLow().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMaximum().addUntypedPropertyListener(this::limitsChanged);
        model_widget.propMinimum().addUntypedPropertyListener(this::limitsChanged);

        model_widget.propUnit().addUntypedPropertyListener(this::unitChanged);
        model_widget.propUnitFromPV().addUntypedPropertyListener(this::unitChanged);

        model_widget.propDragDisabled().addUntypedPropertyListener(this::styleChanged);
        model_widget.propEnabled().addUntypedPropertyListener(this::styleChanged);

        if ( toolkit.isEditMode() ) {
            dirtyValue.checkAndClear();
        } else {
            model_widget.runtimePropValue().addPropertyListener(this::valueChanged);
            model_widget.propReadbackPVValue().addPropertyListener(this::valueChanged);
            model_widget.propSyncedKnob().addUntypedPropertyListener(this::synchChanged);
        }

    }

    private double clamp ( double value, double minValue, double maxValue ) {
        return ( value < minValue ) ? minValue : ( value > maxValue ) ? maxValue : value;
    }

    private List<Stop> computeGradientStops ( ) {

        List<Stop> stops = new ArrayList<>(6);
        double range = max - min;
        boolean loloNaN = Double.isNaN(lolo);
        boolean lowNaN = Double.isNaN(low);

        if ( !loloNaN ) {

            stops.add(new Stop(0.0, JFXUtil.convert(model_widget.propColorLoLo().getValue())));

            if ( !lowNaN ) {
                stops.add(new Stop(( lolo - min ) / range, JFXUtil.convert(model_widget.propColorLow().getValue())));
                stops.add(new Stop(( low - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
            } else {
                stops.add(new Stop(( lolo - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
            }

        } else if ( !lowNaN ) {
            stops.add(new Stop(0.0, JFXUtil.convert(model_widget.propColorLow().getValue())));
            stops.add(new Stop(( low - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
        } else {
            stops.add(new Stop(0.0, JFXUtil.convert(model_widget.propColorOK().getValue())));
        }

        boolean highNaN = Double.isNaN(high);
        boolean hihiNaN = Double.isNaN(hihi);

        if ( !hihiNaN ) {

            stops.add(new Stop(1.0, JFXUtil.convert(model_widget.propColorHiHi().getValue())));

            if ( !highNaN ) {
                stops.add(new Stop(( hihi - min )  / range, JFXUtil.convert(model_widget.propColorHigh().getValue())));
                stops.add(new Stop(( high - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
            } else {
                stops.add(new Stop(( hihi - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
            }

        } else if ( !highNaN ) {
            stops.add(new Stop(1.0, JFXUtil.convert(model_widget.propColorHigh().getValue())));
            stops.add(new Stop(( high - min )  / range, JFXUtil.convert(model_widget.propColorOK().getValue())));
        } else {
            stops.add(new Stop(1.0, JFXUtil.convert(model_widget.propColorOK().getValue())));
        }

        return stops.stream().sorted(Comparator.comparingDouble(s -> s.getOffset())).collect(Collectors.toList());

    }

    private void contentChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyContent.mark();
        toolkit.scheduleUpdate(this);
    }

    private void geometryChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyGeometry.mark();
        toolkit.scheduleUpdate(this);
    }

    private boolean isReadbackPVNameValid ( ) {

        String rbpvName = model_widget.propReadbackPVName().getValue();

        return ( rbpvName != null && !rbpvName.trim().isEmpty() );

    }

    private void limitsChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        updateLimits();
        dirtyLimits.mark();
        toolkit.scheduleUpdate(this);
    }

    private void lookChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyLook.mark();
        toolkit.scheduleUpdate(this);
    }

    private void styleChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyStyle.mark();
        toolkit.scheduleUpdate(this);
    }

    private void synchChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyValue.mark();
        toolkit.scheduleUpdate(this);
    }

    private void unitChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyUnit.mark();
        toolkit.scheduleUpdate(this);
    }

    /**
     * Updates, if required, the limits and zones.
     *
     * @return {@code true} is something changed and and UI update is required.
     */
    private boolean updateLimits ( ) {

        boolean somethingChanged = false;

        //  Model's values.
        double newMin = model_widget.propMinimum().getValue();
        double newMax = model_widget.propMaximum().getValue();
        double newLoLo = model_widget.propLevelLoLo().getValue();
        double newLow = model_widget.propLevelLow().getValue();
        double newHigh = model_widget.propLevelHigh().getValue();
        double newHiHi = model_widget.propLevelHiHi().getValue();

        if ( model_widget.propLimitsFromPV().getValue() ) {

            //  Try to get display range from PV.
            final Display display_info = ValueUtil.displayOf(model_widget.runtimePropValue().getValue());

            if ( display_info != null ) {
                newMin = display_info.getLowerCtrlLimit();
                newMax = display_info.getUpperCtrlLimit();
                newLoLo = display_info.getLowerAlarmLimit();
                newLow = display_info.getLowerWarningLimit();
                newHigh = display_info.getUpperWarningLimit();
                newHiHi = display_info.getUpperAlarmLimit();
            }

        }

        if ( !model_widget.propShowLoLo().getValue() ) {
            newLoLo = Double.NaN;
        }
        if ( !model_widget.propShowLow().getValue() ) {
            newLow = Double.NaN;
        }
        if ( !model_widget.propShowHigh().getValue() ) {
            newHigh = Double.NaN;
        }
        if ( !model_widget.propShowHiHi().getValue() ) {
            newHiHi = Double.NaN;
        }

        //  If invalid limits, fall back to 0..100 range.
        if ( !( Double.isNaN(newMin) || Double.isNaN(newMax) || newMin < newMax ) ) {
            newMin = 0.0;
            newMax = 100.0;
        }

        if ( Double.compare(min, newMin) != 0 ) {
            min = newMin;
            somethingChanged = true;
        }
        if ( Double.compare(max, newMax) != 0 ) {
            max = newMax;
            somethingChanged = true;
        }
        if ( Double.compare(lolo, newLoLo) != 0 ) {
            lolo = newLoLo;
            somethingChanged = true;
        }
        if ( Double.compare(low, newLow) != 0 ) {
            low = newLow;
            somethingChanged = true;
        }
        if ( Double.compare(high, newHigh) != 0 ) {
            high = newHigh;
            somethingChanged = true;
        }
        if ( Double.compare(hihi, newHiHi) != 0 ) {
            hihi = newHiHi;
            somethingChanged = true;
        }

        return somethingChanged;

    }

    private void valueChanged ( final WidgetProperty<? extends VType> property, final VType old_value, final VType new_value ) {

        if ( model_widget.propLimitsFromPV().getValue() ) {
            limitsChanged(null, null, null);
        }

        if ( model_widget.propPrecision().getValue() == -1 ) {
            contentChanged(null, null, null);
        }

        if ( model_widget.propUnitFromPV().getValue() ) {
            dirtyUnit.mark();
        }

        dirtyValue.mark();
        toolkit.scheduleUpdate(this);

    }

}
