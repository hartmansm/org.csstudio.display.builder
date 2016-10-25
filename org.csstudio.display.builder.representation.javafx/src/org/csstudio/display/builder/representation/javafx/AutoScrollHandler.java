/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved
 * The contents of this file are subject to the terms of
 * the GNU General Public License Version 3 only ("GPL"
 * Version 3, or the "License"). You can obtain a copy of
 * the License at https://www.gnu.org/licenses/gpl-3.0.html
 * You may use, distribute and modify this code under the
 * terms of the GPL Version 3 license. See the License for
 * the specific language governing permissions and
 * limitations under the License.
 * When distributing the software, include this License
 * Header Notice in each file. If applicable, add the
 * following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying
 * information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.csstudio.display.builder.representation.javafx;


import java.awt.MouseInfo;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;


/**
 * Provide the support for auto-scroll in {@link ScrollPane}, when a drag
 * operation
 * is going outside the pane borders.
 * <P>
 * The {@link ScrollPane} is listened for {@link DragEvent#DRAG_EXITED} and
 * {@link MouseEvent#MOUSE_EXITED} events that will start a {@link Timeline}
 * scrolling the pane every 250ms of an amount proportional to the distance
 * of the cursor from the pane borders.
 * <P>
 * {@link DragEvent#DRAG_ENTERED} and {@link MouseEvent#MOUSE_ENTERED} events,
 * and mouse up condition are monitored to stop the {@link Timeline}.
 *
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Oct 2016
 */
class AutoScrollHandler {

    private enum Edge {
        LEFT, RIGHT, TOP, BOTTOM
    }

    private AtomicReference<Timeline> autoScrollTimeline = new AtomicReference<Timeline>();
    private final ScrollPane scrollPane;

    /**
     * @param scrollPane The {@link ScrollPane} instance for which auto-scroll must be managed.
     */
    AutoScrollHandler ( ScrollPane scrollPane ) {

        this.scrollPane = scrollPane;

        scrollPane.setOnDragEntered(this::handleOnDragEntered);
        scrollPane.setOnDragExited(this::handleOnDragExited);
        scrollPane.setOnMouseEntered(this::handleOnMouseEntered);
        scrollPane.setOnMouseExited(this::handleOnMouseExited);

    }

    /**
     * Creates a new {@link Timeline} and start it. The timeline is started to scroll
     * in the direction specified by the given {@code edge} parameter.
     *
     * @param edge The scrolling side.
     * @return A newly created (and started) {@link Timeline} object.
     */
    private Timeline createAndStartTimeline ( final Edge edge ) {

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(250), event -> scroll(edge)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return timeline;

    }

    /**
     * Return the edge of the {@link ScrollPane} where the cursor exited.
     *
     * @param point The current cursor location relative to the {@link ScrollPane}.
     * @return A {@link Edge} object, {code null} if the cursor is actually inside the {@link ScrollPane}.
     */
    private Edge getEdge ( Point2D point ) {
        return getEdge(point.getX(), point.getY());
    }

    /**
     * Return the edge of the {@link ScrollPane} where the cursor exited.
     *
     * @param x The current cursor x position relative to the {@link ScrollPane}.
     * @param y The current cursor y position relative to the {@link ScrollPane}.
     * @return A {@link Edge} object, {code null} if the cursor is actually inside the {@link ScrollPane}.
     */
    private Edge getEdge ( final double x, final double y ) {

        Bounds bounds = scrollPane.getBoundsInLocal();

        if ( x <= bounds.getMinX() ) {
            return Edge.LEFT;
        } else if ( x >= bounds.getMaxX() ) {
            return Edge.RIGHT;
        } else if ( y <= bounds.getMinY() ) {
            return Edge.TOP;
        } else if ( y >= bounds.getMaxY() ) {
            return Edge.BOTTOM;
        } else {
            // Inside
            return null;
        }

    }

    private void handleOnDragEntered ( DragEvent event ) {

        Timeline timeline = autoScrollTimeline.getAndSet(null);

        if ( timeline != null ) {
            timeline.stop();
        }

    }

    private void handleOnDragExited ( DragEvent event ) {
        autoScrollTimeline.compareAndSet(null, createAndStartTimeline(getEdge(scrollPane.sceneToLocal(event.getSceneX(), event.getSceneY()))));
    }

    private void handleOnMouseEntered ( MouseEvent event ) {

        Timeline timeline = autoScrollTimeline.getAndSet(null);

        if ( timeline != null ) {
            timeline.stop();
        }

    }

    private void handleOnMouseExited ( MouseEvent event ) {
        if ( event.isPrimaryButtonDown() ) {
            autoScrollTimeline.compareAndSet(null, createAndStartTimeline(getEdge(scrollPane.sceneToLocal(event.getSceneX(), event.getSceneY()))));
        }
    }

    /**
     * Scrolls the {@link ScrollPane} along the given {@code edge}.
     *
     * @param edge The scrolling side.
     */
    private void scroll ( Edge edge ) {

        if ( edge == null ) {
            return;
        }

        Point screenLocation = MouseInfo.getPointerInfo().getLocation();
        Point2D localLocation = scrollPane.screenToLocal(screenLocation.getX(), screenLocation.getY());

        switch ( edge ) {
            case LEFT:
                scrollPane.setHvalue(scrollPane.getHvalue() - 1);
                break;
            case RIGHT:
                scrollPane.setHvalue(scrollPane.getHvalue() + 1);
                break;
        }



    }

}
