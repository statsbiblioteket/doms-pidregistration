package dk.statsbiblioteket.pidregistration.handlesystem;

import net.handle.hdllib.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.PrivateKey;

/**
 * Responsible for loading the private key
 */
public class PrivateKeyLoader {
    private static final Logger log = LoggerFactory.getLogger(PrivateKeyLoader.class);

    private static final String DEFAULT_PRIVATE_KEY_PATH = System.getProperty("user.home");

    private static final String PRIVATE_KEY_FILENAME = "admpriv.bin";

    private String privateKeyPassword;
    private File privateKeyFile;

    public PrivateKeyLoader(String privateKeyPassword) {
        this(privateKeyPassword, DEFAULT_PRIVATE_KEY_PATH);
    }

    public PrivateKeyLoader(String privateKeyPassword, String privateKeyPath) {
        this.privateKeyPassword = privateKeyPassword;
        this.privateKeyFile = new File(privateKeyPath, PRIVATE_KEY_FILENAME);
    }

    /**
     * Load the private key from file.
     *
     * @return The private key loaded from file.
     * @throws PrivateKeyException If something went wrong loading the private
     *                             key.
     */
    public PrivateKey load() throws PrivateKeyException {
        if (!privateKeyFile.exists()) {
            throw new PrivateKeyException(
                    "The admin private key file could not be found in '"
                            + privateKeyFile + "'.");
        }

        if (!privateKeyFile.canRead()) {
            throw new PrivateKeyException(
                    "The admin private key file cannot be read in '"
                            + privateKeyFile + "'.");
        }

        try {
            PrivateKey key = Util.getPrivateKeyFromFileWithPassphrase(privateKeyFile, privateKeyPassword);
            log.debug("Read handle private key from '" + privateKeyFile.getPath() + "'");
            return key;
        } catch (Exception e) {
            String message = "The admin private key  in '"
                    + privateKeyFile + "' could not be used, "
                    + " was the correct password used? " + e.getMessage();
            throw new PrivateKeyException(message, e);
        }
    }
}
