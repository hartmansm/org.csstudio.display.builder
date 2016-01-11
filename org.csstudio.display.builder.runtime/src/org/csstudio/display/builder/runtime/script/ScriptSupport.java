/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.runtime.script;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.display.builder.model.properties.ScriptInfo;
import org.csstudio.display.builder.model.util.NamedDaemonPool;

/** Script (Jython, Javascript) Support
 *
 *  <p>Each instance of the support module maintains one interpreter instance.
 *  Script files are parsed/compiled (possibly slow) and can then be executed
 *  multiple times (hopefully faster).
 *
 *  <p>Scripts are executed on one thread per support/interpreter.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScriptSupport
{
    /** Pool for script related executors */
    static final ThreadFactory POOL = new NamedDaemonPool("ScriptSupport");

    private final JythonScriptSupport jython;
    private final JavaScriptSupport javascript;

    public ScriptSupport() throws Exception
    {
        jython = new JythonScriptSupport();
        javascript = new JavaScriptSupport();
    }

    /** Parse and compile script file
     *
     *  @param path Full path to script file
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String path) throws Exception
    {
        return compile(path, new FileInputStream(path));
    }

    /** Parse and compile script file
     *
     *  @param path Name of script (file name, URL)
     *  @param stream Stream for the script content
     *  @return {@link Script}
     *  @throws Exception on error
     */
    public Script compile(final String path, final InputStream stream) throws Exception
    {
        final InputStream script_stream = patchScript(path, stream);
        if (ScriptInfo.isJython(path))
            return jython.compile(path, script_stream);
        else if (ScriptInfo.isJavaScript(path))
            return javascript.compile(path, script_stream);
        throw new Exception("Cannot compile '" + path + "'");
    }

    /** Update legacy package names
     *  @param path Name of script (file name, URL)
     *  @param stream Stream for the script content
     *  @return Patched stream
     *  @throws Exception on error
     */
    private InputStream patchScript(final String path, final InputStream stream) throws Exception
    {
        boolean warned = false;

        final StringBuilder buf = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null)
        {
            if (line.contains("scriptUtil"))
            {
                if (! warned)
                {
                    Logger.getLogger(getClass().getName())
                          .log(Level.SEVERE,
                               "Script '" + path + "' accessed deprecated org.csstudio.opibuilder.scriptUtil, " +
                               "update to org.csstudio.display.builder.runtime.script.PVUtil");
                    warned = true;
                }
                line = line.replace("org.csstudio.opibuilder.scriptUtil", "org.csstudio.display.builder.runtime.script");
            }
            buf.append(line).append('\n');
        }
        stream.close();

        return new ByteArrayInputStream(buf.toString().getBytes());
    }

    /** Release resources (interpreter, ...) */
    public void close()
    {
        javascript.close();
        jython.close();
    }
}
