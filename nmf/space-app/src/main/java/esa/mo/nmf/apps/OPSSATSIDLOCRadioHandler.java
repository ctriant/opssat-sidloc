// (C) 2021 European Space Agency
// European Space Operations Centre
// Darmstadt, Germany
package esa.mo.nmf.apps;

import esa.mo.helpertools.connections.ConnectionConsumer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.structures.UInteger;
import esa.mo.nmf.NMFException;
import esa.mo.nmf.commonmoadapter.SimpleDataReceivedListener;
import java.util.Timer;
import java.util.TimerTask;
import org.ccsds.moims.mo.mal.MALDecoder;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.provider.MALInteraction;
import org.ccsds.moims.mo.mal.structures.Duration;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.platform.softwaredefinedradio.consumer.SoftwareDefinedRadioAdapter;
import org.ccsds.moims.mo.platform.softwaredefinedradio.structures.SDRConfiguration;

/**
 * Handles tasks related to data: fetch data from supervisor, save the data.
 *
 * @author Tanguy Soto
 */
public class OPSSATSIDLOCRadioHandler extends SoftwareDefinedRadioAdapter {

    private static final Logger LOGGER = Logger.getLogger(OPSSATSIDLOCRadioHandler.class.getName());

    /**
     * Relative path to the directory containing our data inside the toGround/
     * folder.
     */
    private static final String DATA_DIR = "data";

    /**
     * Time stamps format for data logs.
     */
    private static final SimpleDateFormat timestampFormat
            = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**
     * Photodiode elevation threshold for the HD Camera: FOV 18.63 deg (in lens
     * specs) and 21 deg (in ICD). Elevation threshold is 90 deg - (FOV +
     * margin) = 60 deg (1.0472 rad).
     */
    private static final float PD6_ELEVATION_THRESHOLD_HD_CAM = 1.0472f;

    /**
     * M&C interface of the application.
     */
    private final OPSSATSIDLOCMCAdapter adapter;

    /**
     * Parameters default value before first acquisition.
     */
    private static final String PARAMS_DEFAULT_VALUE = "x";

    /**
     * Lock for accessing our internal parameters
     */
    private final ReentrantLock parametersLock = new ReentrantLock();

    /**
     * The listener for parameters values coming from supervisor.
     */
    private SimpleDataReceivedListener parameterListener;

    /**
     * Initial SDR Configuration parameters
     */
    private static float SDR_SAMPLING_FREQUENCY = (float) 1.5;
    private static final float SDR_LPF_BW = (float) 0.75;
    private static final int SDR_RX_GAIN = 10;
    private static float SDR_CENTER_FREQUENCY = (float) 443.0;
    private static final Duration SDR_REPORTING_INTERVAL = new Duration(0.2);
    private SDRConfiguration config;

    private static List<String> parametersNames;
    

    public OPSSATSIDLOCRadioHandler(OPSSATSIDLOCMCAdapter adapter) {
        this.adapter = adapter;

        OPSSATSIDLOCConf.getinstance().loadProperties();
        SDR_SAMPLING_FREQUENCY = Float.parseFloat(OPSSATSIDLOCConf.getinstance().getProperty(OPSSATSIDLOCConf.SAMP_RATE));
        LOGGER.log(Level.INFO, String.format("SDR SAMP RATE. %f", SDR_SAMPLING_FREQUENCY));
        SDR_CENTER_FREQUENCY = Float.parseFloat(OPSSATSIDLOCConf.getinstance().getProperty(OPSSATSIDLOCConf.FREQUENCY));
        this.config = new SDRConfiguration(SDR_CENTER_FREQUENCY, SDR_RX_GAIN,
                SDR_LPF_BW, SDR_SAMPLING_FREQUENCY);
    }

    public UInteger recordSDRData() {
        LOGGER.log(Level.INFO, String.format("SDR SAMP RATE. %f", SDR_SAMPLING_FREQUENCY));
        try {
            org.ccsds.moims.mo.mal.transport.MALMessage msg = adapter.getConnector().getPlatformServices().getSoftwareDefinedRadioService().asyncEnableSDR(true, config, SDR_REPORTING_INTERVAL, this);
            return null; // Success!
        } catch (MALInteractionException | MALException | IOException | NMFException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return new UInteger(1);
        }
    }

    /**
     * Queries OBSW parameters values from the supervisor and set our internal
     * parameters with those values.
     *
     * @param subscribe True if we want supervisor to push new parameters data,
     * false
     * @return null if it was successful. If not null, then the returned value
     * holds the error number
     */
//    public synchronized UInteger toggleSupervisorParametersSubscription(boolean subscribe) {
//        parametersNames = getParametersToEnable();
//        if (parametersNames == null) {
//            parametersNames = new ArrayList<>();
//            return new UInteger(1);
//        }
//        if (parametersNames.size() <= 0) {
//            return new UInteger(2);
//        }
//
//        // Always toggle the parameters generation in supervisor
//        try {
//            adapter.getSupervisorSMA().toggleParametersGeneration(parametersNames, subscribe);
//        } catch (NMFException e0) {
//            LOGGER.log(Level.SEVERE, "Error toggling supervisor parameters generation", e0);
//            return new UInteger(3);
//        }
//
//        if (subscribe) {
//
//            // Only if first time, create and register the parameters listener
//            if (this.parameterListener == null) {
//                this.parameterListener = new SimpleDataReceivedListener() {
//                    @Override
//                    public void onDataReceived(String parameterName, Serializable data) {
//                        if (data == null) {
//                            LOGGER.log(Level.WARNING,
//                                    String.format("Received null value for parameter %s", parameterName));
//                            return;
//                        }
//
//                        String dataS = data.toString();
//                        LOGGER.log(Level.FINE, String.format(
//                                "Received value %s from supervisor for parameter %s", dataS, parameterName));
//
//                        setParameter(parameterName, dataS);
//                    }
//                };
//
//                adapter.getSupervisorSMA().addDataReceivedListener(this.parameterListener);
//                LOGGER.log(Level.INFO, "Started fetching parameters from supervisor");
//            }
//        }
//
//        return null;
//    }
    /**
     * Parses the parameters to enable properties and returns the list of OBSW
     * parameter we want to enable the generation in the supervisor.
     *
     * @return The list of parameter identifiers or null if an error occurred.
     */
//    private List<String> getParametersToEnable() {
//        String content = OPSSATSIDLOCConf.getinstance().getProperty(OPSSATSIDLOCConf.PARAMS_TO_ENABLE);
//        if (content == null) {
//            LOGGER.log(Level.SEVERE, "Error while loading parameters to enable in supervisor");
//            return null;
//        }
//
//        // parse the line
//        List<String> paramNames = new ArrayList<String>();
//        for (String paramName : content.split(",")) {
//            paramNames.add(paramName);
//        }
//
//        if (paramNames.size() <= 0) {
//            LOGGER.log(Level.WARNING,
//                    String.format("Found no parameters to enable in property content %s", content));
//        }
//        return paramNames;
//    }
//    /**
//     * Sets a parameter with the given value.
//     *
//     * @param parameterName The parameter name
//     * @param value The new value for the parameter
//     */
//    private void setParameter(String parameterName, String value) {
//        parametersLock.lock();
//        parameters.put(parameterName, value);
//        parametersLock.unlock();
//    }
//
//    /**
//     * Returns latest parameters values fetched from supervisor at the time of
//     * call.
//     *
//     * @return The map of parameters names and their values
//     */
//    private Map<String, String> getStampedParametersValues() {
//        Map<String, String> parametersValues = new HashMap<>();
//        parametersLock.lock();
//        for (String parameterName : parametersNames) {
//            parameters.computeIfAbsent(parameterName, k -> PARAMS_DEFAULT_VALUE);
//            parametersValues.put(parameterName, parameters.get(parameterName));
//        }
//        parametersLock.unlock();
//
//        return parametersValues;
//    }
    /**
     * Returns a formatted time stamp for the time of the call.
     *
     * @return the time stamp formatted as a String
     */
    public static String formatTimestamp(long timestamp) {
        return timestampFormat.format(new Date(timestamp));
    }

    /**
     * Returns a time stamp for the time of the call.
     *
     * @return the time stamp in milliseconds
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
}
