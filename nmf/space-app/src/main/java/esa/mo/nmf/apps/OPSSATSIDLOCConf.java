// (C) 2021 European Space Agency
// European Space Operations Centre
// Darmstadt, Germany

package esa.mo.nmf.apps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds OPSSATSIDLOC application configuration constants and helper methods.
 */
public class OPSSATSIDLOCConf {

  private static final Logger LOGGER = Logger.getLogger(OPSSATSIDLOCConf.class.getName());

  /**
   * Singleton instance.
   */
  private static OPSSATSIDLOCConf instance;

  /**
   * Path to the application configuration file.
   */
  private static final String PROPERTIES_FILE_PATH = "opssat-sidloc.properties";

  // ========== AVAILABLE PROPERTIES

  /**
   * Property value: unknown.
   */
  public static final String FREQUENCY = "esa.mo.nmf.apps.OPSSATSIDLOC.frequency";

   /**
   * Property value: unknown.
   */
  public static final String SAMP_RATE = "esa.mo.nmf.apps.OPSSATSIDLOC.samp_rate";
  
   /**
   * Property key: OBSW parameters for which the publishing will be enabled in NMF supervisor.
   */
  public static final String PARAMS_TO_ENABLE = "esa.mo.nmf.apps.OrbitAI.params_to_enable";
  
  /**
   * Configuration properties holder.
   */
  private Properties properties;

  /**
   * Hide constructor.
   */
  private OPSSATSIDLOCConf() {}

  /**
   * Returns the Configuration instance of the application.
   *
   * @return the configuration instance.
   */
  public static OPSSATSIDLOCConf getinstance() {
    if (instance == null) {
      instance = new OPSSATSIDLOCConf();
    }
    return instance;
  }

  /**
   * Loads the properties from the configuration file located at @v
   *
   */
  public void loadProperties() {
    try (InputStream input = new FileInputStream(PROPERTIES_FILE_PATH)) {
      properties = new Properties();
      properties.load(input);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error loading the application configuration properties", e);
    }

    LOGGER.log(Level.INFO,
        String.format("Loaded configuration properties from file %s", PROPERTIES_FILE_PATH));
  }

  /**
   * Searches for the property with the specified key in the application's properties.
   *
   * @param key The property key
   * @return The property or null if the property is not found
   */
  public String getProperty(String key) {
    String property = properties.getProperty(key);
    if (property == null) {
      LOGGER.log(Level.SEVERE,
          String.format("Couldn't find property with key %s, returning null", key));
    }
    return property;
  }
}
