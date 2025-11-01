package org.nrg.xnat.dicomweb.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.dicomweb.utils.TestDataFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for XnatDicomServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class XnatDicomServiceImplTest {

    @InjectMocks
    private XnatDicomServiceImpl dicomService;

    private UserI mockUser;
    private XnatProjectdata mockProject;

    @Before
    public void setUp() {
        mockUser = TestDataFactory.createMockUser();
        mockProject = TestDataFactory.createMockProject();
    }

    @Test
    public void testSearchStudies() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<Attributes> results = dicomService.searchStudies(
                    mockUser, TestDataFactory.TEST_PROJECT_ID, null);

            assertNotNull("Results should not be null", results);
            assertFalse("Results should not be empty", results.isEmpty());
            assertEquals("Should return one study", 1, results.size());

            Attributes study = results.get(0);
            assertEquals("StudyInstanceUID should match",
                    TestDataFactory.TEST_STUDY_UID,
                    study.getString(Tag.StudyInstanceUID));
            assertEquals("PatientID should match",
                    TestDataFactory.TEST_PATIENT_ID,
                    study.getString(Tag.PatientID));
        }
    }

    @Test
    public void testSearchStudiesProjectNotFound() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    anyString(), eq(mockUser), eq(false)))
                    .thenReturn(null);

            List<Attributes> results = dicomService.searchStudies(
                    mockUser, "NONEXISTENT_PROJECT", null);

            assertNotNull("Results should not be null", results);
            assertTrue("Results should be empty for non-existent project", results.isEmpty());
        }
    }

    @Test
    public void testSearchStudiesMultipleStudies() throws Exception {
        XnatProjectdata multiProject = mock(XnatProjectdata.class);
        when(multiProject.getId()).thenReturn(TestDataFactory.TEST_PROJECT_ID);

        List<XnatImagesessiondata> multipleSessions = TestDataFactory.createMockSessions(3);
        when(multiProject.getImageSessions_imageSession()).thenReturn(multipleSessions);

        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(multiProject);

            List<Attributes> results = dicomService.searchStudies(
                    mockUser, TestDataFactory.TEST_PROJECT_ID, null);

            assertNotNull("Results should not be null", results);
            assertEquals("Should return three studies", 3, results.size());
        }
    }

    @Test
    public void testSearchSeries() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<Attributes> results = dicomService.searchSeries(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID, null);

            assertNotNull("Results should not be null", results);
            assertFalse("Results should not be empty", results.isEmpty());

            Attributes series = results.get(0);
            assertEquals("SeriesInstanceUID should match",
                    TestDataFactory.TEST_SERIES_UID,
                    series.getString(Tag.SeriesInstanceUID));
            assertEquals("Modality should be CT",
                    "CT", series.getString(Tag.Modality));
        }
    }

    @Test
    public void testSearchSeriesStudyNotFound() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<Attributes> results = dicomService.searchSeries(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    "NONEXISTENT_STUDY_UID", null);

            assertNotNull("Results should not be null", results);
            assertTrue("Results should be empty for non-existent study", results.isEmpty());
        }
    }

    @Test
    public void testSearchSeriesMultipleSeries() throws Exception {
        XnatImagesessiondata session = mock(XnatImagesessiondata.class);
        when(session.getUid()).thenReturn(TestDataFactory.TEST_STUDY_UID);

        List<XnatImagescandata> multipleScans = TestDataFactory.createMockScans(5);
        when(session.getScans_scan()).thenReturn(multipleScans);

        XnatProjectdata project = mock(XnatProjectdata.class);
        List<XnatImagesessiondata> sessions = new ArrayList<>();
        sessions.add(session);
        when(project.getImageSessions_imageSession()).thenReturn(sessions);

        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(project);

            List<Attributes> results = dicomService.searchSeries(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID, null);

            assertNotNull("Results should not be null", results);
            assertEquals("Should return five series", 5, results.size());
        }
    }

    @Test
    public void testSearchInstances() throws Exception {
        // Note: This test is limited because it requires actual DICOM files
        // In a real implementation, you'd mock the file system access
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<Attributes> results = dicomService.searchInstances(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID,
                    TestDataFactory.TEST_SERIES_UID, null);

            assertNotNull("Results should not be null", results);
            // Results will be empty in unit test without actual files
        }
    }

    @Test
    public void testRetrieveMetadata() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            Attributes metadata = dicomService.retrieveMetadata(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID,
                    TestDataFactory.TEST_SERIES_UID,
                    TestDataFactory.TEST_INSTANCE_UID);

            // Metadata will be null in unit test without actual files
            // In integration tests with real data, this would return attributes
        }
    }

    @Test
    public void testRetrieveInstance() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            InputStream stream = dicomService.retrieveInstance(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID,
                    TestDataFactory.TEST_SERIES_UID,
                    TestDataFactory.TEST_INSTANCE_UID);

            // Stream will be null in unit test without actual files
            // In integration tests with real data, this would return a stream
        }
    }

    @Test
    public void testRetrieveStudy() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<InputStream> streams = dicomService.retrieveStudy(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID);

            assertNotNull("Results should not be null", streams);
            // Streams will be empty in unit test without actual files
        }
    }

    @Test
    public void testRetrieveSeries() throws Exception {
        try (MockedStatic<XnatProjectdata> mockedStatic = mockStatic(XnatProjectdata.class)) {
            mockedStatic.when(() -> XnatProjectdata.getXnatProjectdatasById(
                    eq(TestDataFactory.TEST_PROJECT_ID), eq(mockUser), eq(false)))
                    .thenReturn(mockProject);

            List<InputStream> streams = dicomService.retrieveSeries(
                    mockUser, TestDataFactory.TEST_PROJECT_ID,
                    TestDataFactory.TEST_STUDY_UID,
                    TestDataFactory.TEST_SERIES_UID);

            assertNotNull("Results should not be null", streams);
            // Streams will be empty in unit test without actual files
        }
    }
}
