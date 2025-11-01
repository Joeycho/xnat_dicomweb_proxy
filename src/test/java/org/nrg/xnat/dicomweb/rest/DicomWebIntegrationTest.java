package org.nrg.xnat.dicomweb.rest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DICOMweb API endpoints
 *
 * Note: These tests are marked as @Ignore because they require a running XNAT instance
 * with test data. To run these tests:
 *
 * 1. Set up XNAT with test data
 * 2. Configure test project ID and test data UIDs
 * 3. Remove @Ignore annotation
 * 4. Run with proper Spring Boot test configuration
 */
@Ignore("Requires running XNAT instance with test data")
@RunWith(SpringRunner.class)
@SpringBootTest
public class DicomWebIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    // Configure these for your test environment
    private static final String TEST_PROJECT_ID = "TEST_PROJECT";
    private static final String TEST_STUDY_UID = "1.2.840.113619.2.55.3.1234567890.123";
    private static final String TEST_SERIES_UID = "1.2.840.113619.2.55.3.1234567890.456";
    private static final String TEST_INSTANCE_UID = "1.2.840.113619.2.55.3.1234567890.789";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSearchStudiesEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID + "/studies")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom+json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testSearchSeriesEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID + "/series")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom+json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testSearchInstancesEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID +
                           "/series/" + TEST_SERIES_UID + "/instances")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom+json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testRetrieveInstanceEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID +
                           "/series/" + TEST_SERIES_UID +
                           "/instances/" + TEST_INSTANCE_UID)
                .accept(MediaType.parseMediaType("application/dicom")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom"));
    }

    @Test
    public void testRetrieveInstanceMetadataEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID +
                           "/series/" + TEST_SERIES_UID +
                           "/instances/" + TEST_INSTANCE_UID + "/metadata")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom+json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testRetrieveSeriesEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID +
                           "/series/" + TEST_SERIES_UID)
                .accept(MediaType.parseMediaType("multipart/related")))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));
    }

    @Test
    public void testRetrieveStudyEndpoint() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID)
                .accept(MediaType.parseMediaType("multipart/related")))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));
    }

    @Test
    public void testSearchStudiesWithNonExistentProject() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/NONEXISTENT_PROJECT/studies")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/dicom+json"))
                .andExpect(content().string("[]"));
    }

    @Test
    public void testRetrieveNonExistentInstance() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID +
                           "/studies/" + TEST_STUDY_UID +
                           "/series/" + TEST_SERIES_UID +
                           "/instances/9.9.9.9.9.9.9.9")
                .accept(MediaType.parseMediaType("application/dicom")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCorsHeaders() throws Exception {
        mockMvc.perform(get("/xapi/dicomweb/projects/" + TEST_PROJECT_ID + "/studies")
                .header("Origin", "http://localhost:3000")
                .accept(MediaType.parseMediaType("application/dicom+json")))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
