/*******************************************************************************
 * Copyright (c) 2015 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamWriter;

import org.csstudio.display.builder.model.persist.ModelWriter;
import org.csstudio.display.builder.model.persist.XMLUtil;
import org.w3c.dom.Element;

/** Structured widget property, contains several basic widget properties.
 *
 *  <p>The individual elements can be set unless they are read-only.
 *  The structure will be read-only if all its elements are read-only.
 *
 *  <p>The overall structure cannot be redefined,
 *  i.e. it is not permitted to set a new 'value'
 *  to the structure.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class StructuredWidgetProperty extends WidgetProperty<List<WidgetProperty<?>>>
{
    /** Descriptor of a structured property */
    public static class Descriptor extends WidgetPropertyDescriptor<List<WidgetProperty<?>>>
    {
        public Descriptor(final WidgetPropertyCategory category,
                          final String name, final String description)
        {
            super(category, name, description);
        }

        @Override
        public StructuredWidgetProperty createProperty(
                final Widget widget, final List<WidgetProperty<?>> elements)
        {
            return new StructuredWidgetProperty(this, widget, elements);
        }
    };

    protected StructuredWidgetProperty(final Descriptor descriptor,
            final Widget widget, final List<WidgetProperty<?>> elements)
    {
        super(descriptor, widget, elements);
    }

    /** @return <code>true</code> if all elements have default value */
    @Override
    public boolean isDefaultValue()
    {
        for (WidgetProperty<?> element : value)
            if (! element.isDefaultValue())
                return false;
        return true;
    }

    /** @return <code>true</code> if all elements are read-only */
    @Override
    public boolean isReadonly()
    {
        for (WidgetProperty<?> element : value)
            if (! element.isReadonly())
                return false;
        return true;
    }

    /** Access element as known type
     *
     *  @param index Element index, 0 .. (<code>getValue().size()</code>-1)
     *  @return Widget property cast to receiving type
     */
    // Not perfect: Caller needs to know the type.
    // Still, at least there's a runtime error when attempting to cast to the wrong type,
    // and since the structure cannot change, this is almost as good as a compile time check.
    @SuppressWarnings("unchecked")
    public <TYPE> WidgetProperty<TYPE> getElement(final int index)
    {
        return (WidgetProperty<TYPE>) value.get(index);
    }

    /** Access element as known type
     *
     *  @param element_name Element name
     *  @return Widget property cast to receiving type
     *  @throws IllegalArgumentException if element name is not found in structure
     */
    @SuppressWarnings("unchecked")
    public <TYPE> WidgetProperty<TYPE> getElement(final String element_name)
    {
        for (WidgetProperty<?> element : value)
            if (element.getName().equals(element_name))
                return (WidgetProperty<TYPE>) element;
        throw new IllegalArgumentException("Structure has no element named " + element_name);
    }

    @Override
    public void setValue(final List<WidgetProperty<?>> value)
    {
        throw new IllegalAccessError("Elements of structure " + getName() + " cannot be re-defined");
    }

    @Override
    public void setValueFromObject(final Object value) throws Exception
    {
        throw new Exception("Elements of structure " + getName() + " cannot be re-defined");
    }

    @Override
    public void writeToXML(final XMLStreamWriter writer) throws Exception
    {
        for (WidgetProperty<?> element : value)
            ModelWriter.writeProperty(writer, element);
    }

    @Override
    public void readFromXML(final Element property_xml) throws Exception
    {
        for (WidgetProperty<?> element : value)
        {
            final Element xml = XMLUtil.getChildElement(property_xml, element.getName());
            if (xml == null)
                continue;
            try
            {
                element.readFromXML(xml);
            }
            catch (Exception ex)
            {
                Logger.getLogger(getClass().getName())
                      .log(Level.WARNING, "Error reading " + getName() + " element " + element.getName(), ex);
            }
        }
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder("'" + getName() + "' = { ");
        boolean first = true;
        for (WidgetProperty<?> element : value)
        {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(element);
        }
        buf.append(" }");
        return buf.toString();
    }
}