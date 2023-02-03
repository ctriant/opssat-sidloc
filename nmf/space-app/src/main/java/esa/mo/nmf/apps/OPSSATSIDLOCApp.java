/* ----------------------------------------------------------------------------
 * Copyright (C) 2021      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under European Space Agency Public License (ESA-PL) Weak Copyleft – v2.4
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.nmf.apps;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;
import java.io.File;

/**
 * Blank App
 */
public class OPSSATSIDLOCApp {

    private static final Logger LOGGER = Logger.getLogger(OPSSATSIDLOCApp.class.getName());
    /**
     * Path to the toGround/ directory of the application.
     */
    public static final String TO_GROUND_DIR = "toGround/";

    double samp_rate;

    /**
     * Monitoring & Control interface of the application.
     */
    private OPSSATSIDLOCMCAdapter adapter;

    public OPSSATSIDLOCApp() {
        // Initialize M&C interface
        adapter = new OPSSATSIDLOCMCAdapter();

        // Initialize application's NMF provider
        NanoSatMOConnectorImpl connector = new NanoSatMOConnectorImpl();
        connector.init(adapter);

        // Initialize application's NMF consumer (consuming the supervisor)
        SpaceMOApdapterImpl supervisorSMA
                = SpaceMOApdapterImpl.forNMFSupervisor(connector.readCentralDirectoryServiceURI());

        // // Once all initialized, pass them to the M&C interface that handles the application's logic
        adapter.setConnector(connector);
        adapter.setSupervisorSMA(supervisorSMA);

        LOGGER.log(Level.INFO, String.format("OPPSAT SIDLOC initialized."));
    }

    /**
     * Starts the application. This starts fetching data from the supervisor and
     * starts the training of models based on these data.
     *
     */
    public void start() {
        try {
            LOGGER.log(Level.INFO, "Starting OPSSAT SIDLOC");
            adapter.recordSDRData();
            Runtime.getRuntime().exec(new String[]{"./app", "out.txt"}, null, new File("."));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error executing experiment's binary.");
        }
    }

    /**
     * Main command line entry point.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception If there is an error
     */
    public static void main(final String[] args) throws Exception {
        OPSSATSIDLOCApp app = new OPSSATSIDLOCApp();
        app.start();
    }
}
