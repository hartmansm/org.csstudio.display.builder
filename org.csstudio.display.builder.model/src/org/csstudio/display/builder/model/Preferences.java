/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model;

import org.csstudio.display.builder.model.macros.MacroXMLUtil;
import org.csstudio.display.builder.model.macros.Macros;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Preference settings
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    public static final String CACHE_TIMEOUT = "cache_timeout";
    public static final String CLASS_FILES = "class_files";
    public static final String COLOR_FILES = "color_files";
    public static final String FONT_FILES = "font_files";
    public static final String READ_TIMEOUT = "read_timeout";
    public static final String LEGACY_FONT_CALIBRATION = "legacy_font_calibration";
    public static final String MACROS = "macros";
    public static final String MAX_REPARSE_ITERATIONS = "max_reparse_iterations";
    public static final String SUPPORT_STANDALONE_WINDOW = "support_standalone_window";

    public static String[] getClassFiles()
    {
        return getPreference(CLASS_FILES,
                             "platform:/plugin/org.csstudio.display.builder.model/examples/classes.bcf")
                            .split(" *; *");
    }

    public static String[] getColorFiles()
    {
        return getPreference(COLOR_FILES,
                             "platform:/plugin/org.csstudio.display.builder.model/examples/color.def")
                            .split(" *; *");
    }

    public static String[] getFontFiles()
    {
        return getPreference(FONT_FILES,
                             "platform:/plugin/org.csstudio.display.builder.model/examples/font.def")
                            .split(" *; *");
    }

    /** @return Read timeout [ms] */
    public static int getReadTimeout()
    {
        int timeout = 10000;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            timeout = prefs.getInt(ModelPlugin.ID, READ_TIMEOUT, timeout, null);
        return timeout;
    }

    /** @return Cache timeout [sec] */
    public static int getCacheTimeout()
    {
        int timeout = 60;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            timeout = prefs.getInt(ModelPlugin.ID, CACHE_TIMEOUT, timeout, null);
        return timeout;
    }

    /** @return Legacy font size calibration */
    public static double getLegacyFontCalibration()
    {
        double factor = 0.75;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            factor = prefs.getDouble(ModelPlugin.ID, LEGACY_FONT_CALIBRATION, factor, null);
        return factor;
    }

    /** @return Global macros set in preferences, or <code>null</code> */
    public static Macros getMacros()
    {
        // Fall-back value used in MacroHierarchyUnitTest
        String macro_def = "<EXAMPLE_MACRO>Value from Preferences</EXAMPLE_MACRO><TEST>true</TEST>";
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            macro_def = prefs.getString(ModelPlugin.ID, MACROS, macro_def, null);
        if (macro_def.isEmpty())
            return null;
        try
        {
            return MacroXMLUtil.readMacros(macro_def);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /** @return Maximum number of re-parse operations */
    public static int getMaxReparse()
    {
        int max_reparse = 5000;
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            max_reparse = prefs.getInt(ModelPlugin.ID, MAX_REPARSE_ITERATIONS, max_reparse, null);
        return max_reparse;
    }

    /** @return Do support standalone window option? */
    public static boolean isStandaloneWindowSupported()
    {
        return Boolean.parseBoolean(getPreference(SUPPORT_STANDALONE_WINDOW, "true"));
    }

    /** @param key Preference key
     *  @param default_value Default value
     *  @return Preference text or default value
     */
    public static String getPreference(final String key, final String default_value)
    {
        final IPreferencesService prefs = Platform.getPreferencesService();
        if (prefs != null)
            return prefs.getString(ModelPlugin.ID, key, default_value, null);
        return default_value;
    }
}
