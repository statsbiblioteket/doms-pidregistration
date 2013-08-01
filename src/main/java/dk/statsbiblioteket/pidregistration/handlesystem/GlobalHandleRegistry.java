package dk.statsbiblioteket.pidregistration.handlesystem;

import dk.statsbiblioteket.pidregistration.PIDHandle;
import dk.statsbiblioteket.pidregistration.configuration.PropertyBasedRegistrarConfiguration;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractRequest;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.Charset;
import java.security.PrivateKey;

public class GlobalHandleRegistry {
    private static final Log log = LogFactory.getLog(GlobalHandleRegistry.class);

    private static final String ADMIN_ID_PREFIX = "0.NA/";
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF8");
    private static final int ADMIN_ID_INDEX = 300;
    private static final int ADMIN_RECORD_INDEX = 200;
    private static final int URL_RECORD_INDEX = 1;
    private final PropertyBasedRegistrarConfiguration config;

    private RequestBuilder requestBuilder;

    public GlobalHandleRegistry(PropertyBasedRegistrarConfiguration config) {
        this.config = config;
        String adminId = ADMIN_ID_PREFIX + config.getHandlePrefix();

        PublicKeyAuthenticationInfo pubKeyAuthInfo = new PublicKeyAuthenticationInfo(
                adminId.getBytes(DEFAULT_ENCODING), ADMIN_ID_INDEX, loadPrivateKey());

        requestBuilder = new RequestBuilder(adminId,
                                            ADMIN_ID_INDEX,
                                            ADMIN_RECORD_INDEX,
                                            URL_RECORD_INDEX,
                                            DEFAULT_ENCODING,
                                            pubKeyAuthInfo);

    }

    private PrivateKey loadPrivateKey() throws PrivateKeyException {
        String path = config.getPrivateKeyPath();
        String password = config.getPrivateKeyPassword();
        PrivateKeyLoader privateKeyLoader =
                path == null ? new PrivateKeyLoader(password) : new PrivateKeyLoader(password, path);
        return privateKeyLoader.load();
    }

    public void registerPid(PIDHandle handle, String url)
            throws RegisteringPidFailedException {
        log.debug("Registering handle '" + handle + "' for url '" + url + "'");
        HandleValue[] values = lookupHandle(handle);

        if (values == null) {
            log.debug("Handle '" + handle + "' was previously unknown. Adding with url '" + url + "'");
            createPidWithUrl(handle, url);
            return;
        }

        String urlAtServer = findFirstWithTypeUrl(values);
        if (urlAtServer == null) {
            log.debug("Handle '" + handle + "' already registered, but with no url. Adding '" + url + "'");
            addUrlToPid(handle, url);
            return;
        }

        if (!urlAtServer.equalsIgnoreCase(url)) {
            log.debug("Handle '" + handle + "' already registered with different" + " url '" + urlAtServer + "'. Replacing with '" + url + "'");
            replaceUrlOfPid(handle, url);
            return;
        }

        log.debug("Handle '" + handle + "' already registered with url '" + url + "'. Doing nothing.");
    }

    private HandleValue[] lookupHandle(PIDHandle handle) {
        try {
            return new HandleResolver().resolveHandle(handle.asString());
        } catch (HandleException e) {
            if (e.getCode() == HandleException.HANDLE_DOES_NOT_EXIST) {
                return null;
            } else {
                throw new RegisteringPidFailedException(
                        "Did not succeed in resolving handle, existing or not.",
                        e);
            }
        }
    }

    private String findFirstWithTypeUrl(HandleValue[] handleValues) {
        for (HandleValue value : handleValues) {
            String type = value.getTypeAsString().toUpperCase();
            int index = value.getIndex();
            if (index == URL_RECORD_INDEX && type.equals("URL")) {
                return value.getDataAsString();
            }
        }
        return null;
    }

    private void createPidWithUrl(PIDHandle handle, String url)
            throws RegisteringPidFailedException {
        AbstractRequest request = requestBuilder.buildCreateHandleRequest(handle, url);
        processRequest(request);
    }

    private void processRequest(AbstractRequest request) {
        try {
            HandleResolver resolver = new HandleResolver();
            AbstractResponse response = resolver.processRequest(request);
            if (response.responseCode != AbstractMessage.RC_SUCCESS) {
                throw new RegisteringPidFailedException(
                        "Failed trying to register a handle at the server, response was" + response);
            }
        } catch (HandleException e) {
            throw new RegisteringPidFailedException(
                    "Could not process the request to register a handle at the server.",
                    e);
        }
    }

    private void addUrlToPid(PIDHandle handle, String url)
            throws RegisteringPidFailedException {
        AbstractRequest request = requestBuilder.buildAddValueRequest(handle, url);
        processRequest(request);
    }

    private void replaceUrlOfPid(PIDHandle handle, String url)
            throws RegisteringPidFailedException {
        AbstractRequest request = requestBuilder.buildModifyValueRequest(handle, url);
        processRequest(request);
    }
}