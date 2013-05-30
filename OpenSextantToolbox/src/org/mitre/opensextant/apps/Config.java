/**
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 *
 * (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
 */
package org.mitre.opensextant.apps;

import gate.Gate;
import java.io.File;
import org.mitre.opensextant.processing.ProcessingException;

/**
 * Copyright 2009-2013 The MITRE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * **************************************************************************
 * NOTICE This software was produced for the U. S. Government under Contract No.
 * W15P7T-12-C-F600, and is subject to the Rights in Noncommercial Computer
 * Software and Noncommercial Computer Software Documentation Clause
 * 252.227-7014 (JUN 1995)
 * 
* (c) 2012 The MITRE Corporation. All Rights Reserved.
 * **************************************************************************
 *
 */
/**
 *
 * @author Marc C. Ubaldino, MITRE <ubaldino at mitre dot org>
 */
public class Config {

    public static String OPENSEXTANT_HOME = System.getProperty("opensextant.home");
    public static String SOLR_HOME = null;
    public static String GATE_HOME = null;
    public static String GATE_PLUGINS = null;
    public static String GATE_USER = null;
    public static String GATE_SESSION = null;
    public static String DEFAULT_GAPP = "OpenSextant_Solr.gapp";
    public static String SELECTED_GAPP = null;
    public static String DEFAULT_TEMP = "/tmp";
    /**
     *
     */
    public static String RUNTIME_GAPP_PATH = null;

    public Config(boolean notdefault) throws ProcessingException {
        throw new ProcessingException("If you want to override the non-default configuration,"
                + " \n 1. look at Config(), "
                + " \n 2. subclass Config(boolean), "
                + " \n 3. override this ctor "
                + " \n 4. make sure you close by calling initializePlatform"
                + "\n\n You can have only one 'GATE' configuration per JVM -- it is global in nature");
    }

    /**
     * Default configuration given OPSXT_HOME. SOLR_HOME behavior:
     * <pre>
     * * if JVM arg solr.solr.home is set, assume caller wants to use that path
     * * if SOLR_HOME var is set by caller, use it.
     * * if SOLR_HOME is null, prepare a relative path $opensextant.home/../opensextant-solr/  as the path,
     *       set the resulting path as the JVM arg,
     * 
     * In all cases the JVM arg solr.solr.home should be set to a valid path.
     * </pre>
     */
    public Config() throws ProcessingException {
        /**
         * As of Feb 2013, this is what worked:
         * <jvmArg value="-Dsolr.solr.home=${solr_home}"/>
         * <!-- GATE user config, plugins, etc. -->
         * <jvmArg value="-Dgate.home=${gate_home}"/>
         * <jvmArg value="-Dgate.user.config=${gate_home}/user-gate.xml"/>
         * <jvmArg value="-Dgate.plugins.home=${gate_home}/plugins"/>
         */
        if (SOLR_HOME == null) {
            SOLR_HOME = System.getProperty("solr.solr.home");
            if (SOLR_HOME == null) {
                SOLR_HOME = OPENSEXTANT_HOME + File.separator + ".." + File.separator + "opensextant-solr";
                try {
                    SOLR_HOME = new File(SOLR_HOME).getCanonicalPath();
                    System.setProperty("solr.solr.home", SOLR_HOME);
                } catch (Exception ioerr) {
                    throw new ProcessingException("Solr Home is erroneous", ioerr);
                }
            }
        } else {
            System.setProperty("solr.solr.home", SOLR_HOME);
        }

        if (GATE_HOME == null) {
            GATE_HOME = OPENSEXTANT_HOME + File.separator + "gate";
        }

        GATE_PLUGINS = GATE_HOME + File.separator + "plugins";

        //  We set some silly null user session info here. This prevents GATE from reading your ~/.gate/ settings.
        // 
        GATE_USER = GATE_HOME + File.separator + "user-gate.xml";
        GATE_SESSION = "x";

        initializePlatform();

        SELECTED_GAPP = DEFAULT_GAPP;
        RUNTIME_GAPP_PATH = GATE_HOME + File.separator + SELECTED_GAPP /*DEFAULT_GAPP*/;
    }

    /**
     * Default configuration given OPSXT_HOME , overridden by using a different
     * GAPP file; a different GAPP (GATE app pipeline) is effectively the only
     * thing here that would change
     */
    public Config(String myGapp) throws ProcessingException {
        this();
        initializePlatform();

        SELECTED_GAPP = myGapp;
        File gateHome = new File(GATE_HOME);
        RUNTIME_GAPP_PATH = gateHome.getAbsolutePath() + File.separator + SELECTED_GAPP /* User defined GAPP */;
    }
    public static boolean platform_initialized = false;

    /**
     * Initialize your JVM once with this call. subsequent calls should be
     * shunted as the platform_initialized flag should prevent Gate from being
     * re-init'd. Other global resources should also be added here if they are
     * one-time init.
     *
     */
    public static synchronized void initializePlatform() throws ProcessingException {
        if (platform_initialized) {
            //log.info("Ignoring second request to initialize the OpenSextant platform");
            return;
        }

        File gateHome = new File(GATE_HOME);
        if (!gateHome.canRead()) {
            throw new ProcessingException("GATE HOME does not exist");
        }

        File pluginsHome = new File(GATE_PLUGINS);
        if (!pluginsHome.canRead()) {
            throw new ProcessingException("GATE plugins does not exist");
        }

        gate.Gate.setGateHome(gateHome);
        gate.Gate.setPluginsHome(pluginsHome);
        gate.Gate.setUserConfigFile(new File(GATE_USER));
        gate.Gate.setUserSessionFile(null);

        try {
            // initialize GATE
            // log.info("Initing GATE");
            Gate.init();
            platform_initialized = true;

        } catch (Exception gerr) {
            throw new ProcessingException("GATE would not start", gerr);
        }
    }
}
