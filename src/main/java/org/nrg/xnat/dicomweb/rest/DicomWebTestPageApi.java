package org.nrg.xnat.dicomweb.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves the DICOMweb test page
 */
@XapiRestController
@Api("DICOMweb Test Page")
public class DicomWebTestPageApi extends AbstractXapiRestController {

    private static final Logger logger = LoggerFactory.getLogger(DicomWebTestPageApi.class);

    @Autowired
    public DicomWebTestPageApi(final UserManagementServiceI userManagementService,
                               final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    /**
     * Serve the test page
     */
    @XapiRequestMapping(
            value = "/dicomweb/test",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE
    )
    @ApiOperation(value = "DICOMweb Test Page", response = String.class)
    public String getTestPage() {
        try {
            InputStream is = getClass().getResourceAsStream("/META-INF/resources/dicomweb-test.html");
            if (is != null) {
                return new String(readAllBytes(is), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Error loading test page", e);
        }

        return createInlineTestPage();
    }

    /**
     * Read all bytes from input stream (Java 8 compatible)
     */
    private byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        while ((bytesRead = is.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        return output.toByteArray();
    }

    /**
     * Create inline test page if resource file not found
     */
    private String createInlineTestPage() {
        return "<!DOCTYPE html>" +
                "<html><head><title>DICOMweb Test Page</title></head>" +
                "<body><h1>DICOMweb Test Page</h1>" +
                "<p>Test page resource not found. Using inline fallback.</p>" +
                "<p>Please check that dicomweb-test.html is in resources/META-INF/resources/</p>" +
                "</body></html>";
    }
}
