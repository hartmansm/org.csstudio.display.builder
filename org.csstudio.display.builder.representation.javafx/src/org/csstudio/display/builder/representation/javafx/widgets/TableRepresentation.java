/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.representation.javafx.JFXUtil;
import org.csstudio.javafx.StringTable;

import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/** Creates JavaFX item for model widget
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class TableRepresentation extends RegionBaseRepresentation<StringTable, TableWidget>
{
    private final DirtyFlag dirty_style = new DirtyFlag();
    private final DirtyFlag dirty_content = new DirtyFlag();
    private volatile String value_text = "<?>";

    @Override
    public StringTable createJFXNode() throws Exception
    {
        // In edit mode, table is passive.
        final StringTable table = new StringTable(! toolkit.isEditMode());
        if (toolkit.isEditMode())
        {   // Capture clicks and use to select widget in editor,
            // instead of interacting with the table
            table.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
            {
                event.consume();
                toolkit.fireClick(model_widget, event.isControlDown());
            });
        }
        return table;
    }

    @Override
    protected void registerListeners()
    {
        super.registerListeners();
        model_widget.positionWidth().addUntypedPropertyListener(this::styleChanged);
        model_widget.positionHeight().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayBackgroundColor().addUntypedPropertyListener(this::styleChanged);
        model_widget.displayToolbar().addUntypedPropertyListener(this::styleChanged);

//        model_widget.runtimeValue().addUntypedPropertyListener(this::contentChanged);
    }

    private void styleChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
        dirty_style.mark();
        toolkit.scheduleUpdate(this);
    }

//    /** @param value Current value of PV
//     *  @return Text to show, "<pv name>" if disconnected (no value)
//     */
//    private String computeText(final VType value)
//    {
//        if (value == null)
//            return "<" + model_widget.behaviorPVName().getValue() + ">";
//        return FormatOptionHandler.format(value,
//                                          model_widget.displayFormat().getValue(),
//                                          model_widget.displayPrecision().getValue(),
//                                          model_widget.displayShowUnits().getValue());
//    }

    private void contentChanged(final WidgetProperty<?> property, final Object old_value, final Object new_value)
    {
//        value_text = computeText(model_widget.runtimeValue().getValue());
        dirty_content.mark();
        toolkit.scheduleUpdate(this);
    }

    @Override
    public void updateChanges()
    {
        super.updateChanges();
        if (dirty_style.checkAndClear())
        {
            jfx_node.setPrefSize(model_widget.positionWidth().getValue(),
                                 model_widget.positionHeight().getValue());

//            Color color = JFXUtil.convert(model_widget.displayForegroundColor().getValue());
//            jfx_node.setTextFill(color);
            Color color = JFXUtil.convert(model_widget.displayBackgroundColor().getValue());
            jfx_node.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
//            jfx_node.setFont(JFXUtil.convert(model_widget.displayFont().getValue()));

            jfx_node.showToolbar(model_widget.displayToolbar().getValue());
        }
//        if (dirty_content.checkAndClear())
//            jfx_node.setText(value_text);
    }
}
