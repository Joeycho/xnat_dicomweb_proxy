package org.nrg.xnat.dicomweb.rest;

import org.dcm4che3.data.Attributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.dicomweb.service.XnatDicomService;
import org.nrg.xnat.dicomweb.utils.TestDataFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for WadoRsApi
 */
@RunWith(MockitoJUnitRunner.class)
public class WadoRsApiTest {

    @Mock
    private XnatDicomService dicomService;

    @Mock
    private UserManagementServiceI userManagementService;

    @Mock
    private RoleHolder roleHolder;

    @InjectMocks
    private WadoRsApi wadoRsApi;

    private UserI mockUser;

    @Before
    public void setUp() {
        mockUser = TestDataFactory.createMockUser();
    }

    @Test
    public void testRetrieveInstanceSuccess() throws Exception {
        // Setup
        InputStream mockStream = TestDataFactory.createMockDicomStream();

        when(dicomService.retrieveInstance(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID),
                eq(TestDataFactory.TEST_INSTANCE_UID)))
                .thenReturn(mockStream);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveInstance(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                TestDataFactory.TEST_INSTANCE_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());

        verify(dicomService).retrieveInstance(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID),
                eq(TestDataFactory.TEST_INSTANCE_UID));
    }

    @Test
    public void testRetrieveInstanceNotFound() throws Exception {
        // Setup
        when(dicomService.retrieveInstance(any(UserI.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveInstance(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                "NONEXISTENT_INSTANCE");

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRetrieveInstanceException() throws Exception {
        // Setup
        when(dicomService.retrieveInstance(any(UserI.class), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveInstance(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                TestDataFactory.TEST_INSTANCE_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be INTERNAL_SERVER_ERROR",
                     HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testRetrieveInstanceMetadataSuccess() throws Exception {
        // Setup
        Attributes attrs = TestDataFactory.createInstanceAttributes();

        when(dicomService.retrieveMetadata(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID),
                eq(TestDataFactory.TEST_INSTANCE_UID)))
                .thenReturn(attrs);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.retrieveInstanceMetadata(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                TestDataFactory.TEST_INSTANCE_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertTrue("Body should be JSON array", response.getBody().startsWith("["));

        verify(dicomService).retrieveMetadata(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID),
                eq(TestDataFactory.TEST_INSTANCE_UID));
    }

    @Test
    public void testRetrieveInstanceMetadataNotFound() throws Exception {
        // Setup
        when(dicomService.retrieveMetadata(any(UserI.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.retrieveInstanceMetadata(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                "NONEXISTENT_INSTANCE");

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRetrieveSeriesSuccess() throws Exception {
        // Setup
        List<InputStream> streams = new ArrayList<>();
        streams.add(TestDataFactory.createMockDicomStream());

        when(dicomService.retrieveSeries(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID)))
                .thenReturn(streams);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveSeries(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertNotNull("Content type should not be null", response.getHeaders().getContentType());
        assertTrue("Content type should be multipart/related",
                   response.getHeaders().getContentType().toString().contains("multipart/related"));

        verify(dicomService).retrieveSeries(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID));
    }

    @Test
    public void testRetrieveSeriesNotFound() throws Exception {
        // Setup
        when(dicomService.retrieveSeries(any(UserI.class), anyString(), anyString(), anyString()))
                .thenReturn(new ArrayList<>());

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveSeries(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                "NONEXISTENT_SERIES");

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRetrieveStudySuccess() throws Exception {
        // Setup
        List<InputStream> streams = new ArrayList<>();
        streams.add(TestDataFactory.createMockDicomStream());

        when(dicomService.retrieveStudy(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID)))
                .thenReturn(streams);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveStudy(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());

        verify(dicomService).retrieveStudy(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID));
    }

    @Test
    public void testRetrieveSeriesMetadataSuccess() throws Exception {
        // Setup
        List<Attributes> instances = new ArrayList<>();
        instances.add(TestDataFactory.createInstanceAttributes());

        when(dicomService.searchInstances(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID), isNull()))
                .thenReturn(instances);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.retrieveSeriesMetadata(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertTrue("Body should be JSON array", response.getBody().startsWith("["));

        verify(dicomService).searchInstances(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID), isNull());
    }

    @Test
    public void testRetrieveSeriesMetadataNotFound() throws Exception {
        // Setup
        when(dicomService.searchInstances(any(UserI.class), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(new ArrayList<>());

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.retrieveSeriesMetadata(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                "NONEXISTENT_SERIES");

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testContentTypeIsApplicationDicom() throws Exception {
        // Setup
        InputStream mockStream = TestDataFactory.createMockDicomStream();

        when(dicomService.retrieveInstance(any(UserI.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockStream);

        WadoRsApi spyApi = spy(wadoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<InputStreamResource> response = spyApi.retrieveInstance(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID,
                TestDataFactory.TEST_INSTANCE_UID);

        // Verify
        assertNotNull("Content type should not be null", response.getHeaders().getContentType());
        assertEquals("Content type should be application/dicom",
                     "application/dicom",
                     response.getHeaders().getContentType().toString());
    }
}
