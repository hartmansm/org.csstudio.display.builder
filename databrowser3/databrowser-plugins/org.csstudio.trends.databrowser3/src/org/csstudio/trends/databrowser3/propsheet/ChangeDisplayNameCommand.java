/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.propsheet;

import org.csstudio.display.builder.util.undo.UndoableAction;
import org.csstudio.display.builder.util.undo.UndoableActionManager;
import org.csstudio.trends.databrowser3.Messages;
import org.csstudio.trends.databrowser3.model.ModelItem;

/** Undo-able command to change item's display name
 *  @author Kay Kasemir
 */
public class ChangeDisplayNameCommand extends UndoableAction
{
    final private ModelItem item;
    final private String old_name, new_name;

    /** Register and perform the command
     *  @param operations_manager OperationsManager where command will be reg'ed
     *  @param item Model item to configure
     *  @param new_name New value
     */
    public ChangeDisplayNameCommand(final UndoableActionManager operations_manager,
            final ModelItem item, final String new_name)
    {
        super(Messages.TraceDisplayName);
        this.item = item;
        this.old_name = item.getDisplayName();
        this.new_name = new_name;
        operations_manager.execute(this);
    }

    /** {@inheritDoc} */
    @Override
    public void run()
    {
        item.setDisplayName(new_name);
    }

    /** {@inheritDoc} */
    @Override
    public void undo()
    {
        item.setDisplayName(old_name);
    }
}
