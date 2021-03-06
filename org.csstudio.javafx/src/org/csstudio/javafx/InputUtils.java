/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.javafx;

import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * ENTER and RETURN are handled differently, mostly when JavaFX is
 * embedded in Eclipse RCP. This utility class provides a method to
 * add a proper filter to a JavaFX {@link Node} to fix the problem.
 * <p>
 * On MacOS X with Java 8, when the application is run outside
 * Eclipse RCP (i.e. pure JavaFX application), the inner handler
 * will never fire new events, because ENTER and RETURN are considered
 * equals. Instead, when the application is an Eclipse RCB-based one,
 * then a proper {@link KeyCode#ENTER} is generated when the numpad
 * ENTER key is pressed.
 * </p>
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 1 Jun 2018
 */
public class InputUtils {

    private InputUtils ( ) {
    }

    public static <T extends Node> T wrap ( T component ) {

        component.addEventFilter(KeyEvent.ANY, new EnterHandler());

        return component;

    }

    private static class EnterHandler implements EventHandler<KeyEvent> {

        private KeyCode pressedCode = null;
        private KeyCode typedCode = null;
        private String character = null;

        @Override
        public void handle ( KeyEvent event ) {

            EventType<KeyEvent> eventType = event.getEventType();

            if ( eventType == KeyEvent.KEY_PRESSED ) {
                pressedCode = event.getCode();
                return;
            } else if ( eventType == KeyEvent.KEY_TYPED ) {
                typedCode = event.getCode();
                character = event.getCharacter();
                return;
            } else if ( eventType == KeyEvent.KEY_RELEASED ) {

                KeyCode releasedCode = event.getCode();

                if ( releasedCode == KeyCode.UNDEFINED && typedCode == KeyCode.UNDEFINED && pressedCode == KeyCode.UNDEFINED && "\r".equals(character) ) {

                    Object source = event.getSource();
                    EventTarget target = event.getTarget();
                    String txt = event.getText();
                    boolean sDown = event.isShiftDown();
                    boolean cDown = event.isControlDown();
                    boolean aDown = event.isAltDown();
                    boolean mDown = event.isMetaDown();

                    ((Node) source).fireEvent(new KeyEvent(source, target, KeyEvent.KEY_PRESSED,  "\u0000",  txt, KeyCode.ENTER, sDown, cDown, aDown, mDown));
                    ((Node) source).fireEvent(new KeyEvent(source, target, KeyEvent.KEY_TYPED,    character, txt, KeyCode.ENTER, sDown, cDown, aDown, mDown));
                    ((Node) source).fireEvent(new KeyEvent(source, target, KeyEvent.KEY_RELEASED, "\u0000",  txt, KeyCode.ENTER, sDown, cDown, aDown, mDown));

                }

                pressedCode = null;
                typedCode = null;
                character = null;

            }

        }

    }

}
