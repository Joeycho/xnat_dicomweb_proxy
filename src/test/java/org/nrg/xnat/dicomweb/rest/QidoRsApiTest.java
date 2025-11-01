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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for QidoRsApi
 */
@RunWith(MockitoJUnitRunner.class)
public class QidoRsApiTest {

    @Mock
    private XnatDicomService dicomService;

    @Mock
    private UserManagementServiceI userManagementService;

    @Mock
    private RoleHolder roleHolder;

    @InjectMocks
    private QidoRsApi qidoRsApi;

    private UserI mockUser;

    @Before
    public void setUp() {
        mockUser = TestDataFactory.createMockUser();
    }

    @Test
    public void testSearchStudiesSuccess() throws Exception {
        // Setup
        List<Attributes> studies = new ArrayList<>();
        studies.add(TestDataFactory.createStudyAttributes());

        when(dicomService.searchStudies(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID), isNull()))
                .thenReturn(studies);

        // Mock getSessionUser - using reflection or setting up properly
        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchStudies(TestDataFactory.TEST_PROJECT_ID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertTrue("Body should be JSON array", response.getBody().startsWith("["));
        assertTrue("Body should contain StudyInstanceUID",
                   response.getBody().contains("0020000D"));

        verify(dicomService).searchStudies(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID), isNull());
    }

    @Test
    public void testSearchStudiesEmpty() throws Exception {
        // Setup
        List<Attributes> emptyStudies = new ArrayList<>();

        when(dicomService.searchStudies(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID), isNull()))
                .thenReturn(emptyStudies);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchStudies(TestDataFactory.TEST_PROJECT_ID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("Body should be empty JSON array", "[]", response.getBody());
    }

    @Test
    public void testSearchStudiesException() throws Exception {
        // Setup
        when(dicomService.searchStudies(any(UserI.class), anyString(), isNull()))
                .thenThrow(new RuntimeException("Test exception"));

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchStudies(TestDataFactory.TEST_PROJECT_ID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be INTERNAL_SERVER_ERROR",
                     HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testSearchSeriesSuccess() throws Exception {
        // Setup
        List<Attributes> series = new ArrayList<>();
        series.add(TestDataFactory.createSeriesAttributes());

        when(dicomService.searchSeries(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), isNull()))
                .thenReturn(series);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchSeries(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertTrue("Body should be JSON array", response.getBody().startsWith("["));
        assertTrue("Body should contain SeriesInstanceUID",
                   response.getBody().contains("0020000E"));

        verify(dicomService).searchSeries(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), isNull());
    }

    @Test
    public void testSearchSeriesEmpty() throws Exception {
        // Setup
        List<Attributes> emptySeries = new ArrayList<>();

        when(dicomService.searchSeries(any(UserI.class), anyString(), anyString(), isNull()))
                .thenReturn(emptySeries);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchSeries(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("Body should be empty JSON array", "[]", response.getBody());
    }

    @Test
    public void testSearchInstancesSuccess() throws Exception {
        // Setup
        List<Attributes> instances = new ArrayList<>();
        instances.add(TestDataFactory.createInstanceAttributes());

        when(dicomService.searchInstances(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID), isNull()))
                .thenReturn(instances);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchInstances(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Body should not be null", response.getBody());
        assertTrue("Body should be JSON array", response.getBody().startsWith("["));
        assertTrue("Body should contain SOPInstanceUID",
                   response.getBody().contains("00080018"));

        verify(dicomService).searchInstances(any(UserI.class), eq(TestDataFactory.TEST_PROJECT_ID),
                eq(TestDataFactory.TEST_STUDY_UID), eq(TestDataFactory.TEST_SERIES_UID), isNull());
    }

    @Test
    public void testSearchInstancesEmpty() throws Exception {
        // Setup
        List<Attributes> emptyInstances = new ArrayList<>();

        when(dicomService.searchInstances(any(UserI.class), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(emptyInstances);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchInstances(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("Body should be empty JSON array", "[]", response.getBody());
    }

    @Test
    public void testSearchInstancesException() throws Exception {
        // Setup
        when(dicomService.searchInstances(any(UserI.class), anyString(), anyString(), anyString(), isNull()))
                .thenThrow(new RuntimeException("Test exception"));

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchInstances(
                TestDataFactory.TEST_PROJECT_ID,
                TestDataFactory.TEST_STUDY_UID,
                TestDataFactory.TEST_SERIES_UID);

        // Verify
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be INTERNAL_SERVER_ERROR",
                     HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testContentTypeIsApplicationDicomJson() throws Exception {
        // Setup
        List<Attributes> studies = new ArrayList<>();
        studies.add(TestDataFactory.createStudyAttributes());

        when(dicomService.searchStudies(any(UserI.class), anyString(), isNull()))
                .thenReturn(studies);

        QidoRsApi spyApi = spy(qidoRsApi);
        doReturn(mockUser).when(spyApi).getSessionUser();

        // Execute
        ResponseEntity<String> response = spyApi.searchStudies(TestDataFactory.TEST_PROJECT_ID);

        // Verify
        assertNotNull("Content type should not be null", response.getHeaders().getContentType());
        assertEquals("Content type should be application/dicom+json",
                     "application/dicom+json",
                     response.getHeaders().getContentType().toString());
    }
}
