/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.editor;

import org.csstudio.display.builder.model.ContainerWidget;
import org.csstudio.display.builder.model.Widget;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/** Helpers for handling geometry
 *  @author Kay Kasemri
 */
public class GeometryTools
{
    /** Get offset of widget inside the display.
     *
     *  <p>Widgets that are inside a {@link ContainerWidget}
     *  are positioned relative to the container.
     *
     *  @param widget Model widget
     *  @return {@link Point2D} Offset of the widget relative to the display model
     */
    public static Point2D getDisplayOffset(Widget widget)
    {
        int dx = 0, dy = 0;

        while (widget.getParent().isPresent())
        {
            widget = widget.getParent().get();
            dx += widget.positionX().getValue();
            dy += widget.positionY().getValue();

            if (widget instanceof ContainerWidget)
            {
                final int[] insets = ((ContainerWidget)widget).runtimeInsets().getValue();
                dx += insets[0];
                dy += insets[1];
            }
        }

        return new Point2D(dx, dy);
    }

    /** Get bounds of widget, relative to container
     *  @param widget Model widget
     *  @return {@link Rectangle2D}
     */
    public static Rectangle2D getBounds(final Widget widget)
    {
        return new Rectangle2D(widget.positionX().getValue(),
                               widget.positionY().getValue(),
                               widget.positionWidth().getValue(),
                               widget.positionHeight().getValue());
    }

    /** Get bounds of widget relative to display model
     *  @param widget Model widget
     *  @return {@link Rectangle2D}
     */
    public static Rectangle2D getDisplayBounds(final Widget widget)
    {
        final Point2D offset = getDisplayOffset(widget);
        return new Rectangle2D(offset.getX() + widget.positionX().getValue(),
                               offset.getY() + widget.positionY().getValue(),
                               widget.positionWidth().getValue(),
                               widget.positionHeight().getValue());
    }


    /** Compute bounding rectangle
     *  @param one One rect, may be <code>null</code>
     *  @param other Other rect, may be <code>null</code>
     *  @return Bounding rectangle of one and other.
     *          <code>null</code> if both inputs are <code>null</code>.
     */
    public static Rectangle2D join(final Rectangle2D one, final Rectangle2D other)
    {
        if (one == null)
            return other;
        if (other == null)
            return one;
        final double x = Math.min(one.getMinX(), other.getMinX());
        final double y = Math.min(one.getMinY(), other.getMinY());
        final double x2 = Math.max(one.getMaxX(), other.getMaxX());
        final double y2 = Math.max(one.getMaxY(), other.getMaxY());
        return new Rectangle2D(x, y, x2-x, y2-y);
    }
}
