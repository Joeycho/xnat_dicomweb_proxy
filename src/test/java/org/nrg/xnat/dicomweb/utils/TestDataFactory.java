package org.nrg.xnat.dicomweb.utils;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.nrg.xdat.om.*;
import org.nrg.xft.security.UserI;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory class for creating test data
 */
public class TestDataFactory {

    public static final String TEST_PROJECT_ID = "TEST_PROJECT";
    public static final String TEST_STUDY_UID = "1.2.840.113619.2.55.3.1234567890.123";
    public static final String TEST_SERIES_UID = "1.2.840.113619.2.55.3.1234567890.456";
    public static final String TEST_INSTANCE_UID = "1.2.840.113619.2.55.3.1234567890.789";
    public static final String TEST_PATIENT_ID = "PATIENT001";
    public static final String TEST_SESSION_LABEL = "TEST_SESSION";

    /**
     * Create a mock UserI
     */
    public static UserI createMockUser() {
        UserI user = mock(UserI.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getLogin()).thenReturn("testuser");
        return user;
    }

    /**
     * Create a mock XNAT project
     */
    public static XnatProjectdata createMockProject() {
        XnatProjectdata project = mock(XnatProjectdata.class);
        when(project.getId()).thenReturn(TEST_PROJECT_ID);
        when(project.getName()).thenReturn("Test Project");

        List<XnatImagesessiondata> sessions = new ArrayList<>();
        sessions.add(createMockSession());

        when(project.getImageSessions_imageSession()).thenReturn(sessions);

        return project;
    }

    /**
     * Create a mock imaging session (study)
     */
    public static XnatImagesessiondata createMockSession() {
        XnatImagesessiondata session = mock(XnatImagesessiondata.class);
        when(session.getUid()).thenReturn(TEST_STUDY_UID);
        when(session.getSubjectId()).thenReturn(TEST_PATIENT_ID);
        when(session.getLabel()).thenReturn(TEST_SESSION_LABEL);
        when(session.getId()).thenReturn("SESSION001");
        when(session.getDate()).thenReturn(new Date());

        List<XnatImagescandata> scans = new ArrayList<>();
        scans.add(createMockScan());

        when(session.getScans_scan()).thenReturn(scans);

        return session;
    }

    /**
     * Create a mock scan (series)
     */
    public static XnatImagescandata createMockScan() {
        XnatImagescandata scan = mock(XnatImagescandata.class);
        when(scan.getUid()).thenReturn(TEST_SERIES_UID);
        when(scan.getId()).thenReturn("1");
        when(scan.getModality()).thenReturn("CT");
        when(scan.getSeriesDescription()).thenReturn("Test Series");

        List<XnatAbstractresource> resources = new ArrayList<>();
        resources.add(createMockResource());

        when(scan.getFile()).thenReturn(resources);

        return scan;
    }

    /**
     * Create a mock resource
     */
    public static XnatAbstractresource createMockResource() {
        XnatAbstractresource resource = mock(XnatAbstractresource.class);
        when(resource.getLabel()).thenReturn("DICOM");
        when(resource.getFormat()).thenReturn("DICOM");
        when(resource.getUri()).thenReturn("/tmp/test/dicom");

        return resource;
    }

    /**
     * Create DICOM attributes for a study
     */
    public static Attributes createStudyAttributes() {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.StudyInstanceUID, VR.UI, TEST_STUDY_UID);
        attrs.setString(Tag.PatientName, VR.PN, TEST_PATIENT_ID);
        attrs.setString(Tag.PatientID, VR.LO, TEST_PATIENT_ID);
        attrs.setString(Tag.StudyDate, VR.DA, "20240101");
        attrs.setString(Tag.StudyDescription, VR.LO, TEST_SESSION_LABEL);
        attrs.setString(Tag.AccessionNumber, VR.SH, TEST_SESSION_LABEL);
        attrs.setString(Tag.StudyID, VR.SH, "SESSION001");
        attrs.setString(Tag.ModalitiesInStudy, VR.CS, "CT");
        return attrs;
    }

    /**
     * Create DICOM attributes for a series
     */
    public static Attributes createSeriesAttributes() {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.StudyInstanceUID, VR.UI, TEST_STUDY_UID);
        attrs.setString(Tag.SeriesInstanceUID, VR.UI, TEST_SERIES_UID);
        attrs.setString(Tag.Modality, VR.CS, "CT");
        attrs.setString(Tag.SeriesNumber, VR.IS, "1");
        attrs.setString(Tag.SeriesDescription, VR.LO, "Test Series");
        return attrs;
    }

    /**
     * Create DICOM attributes for an instance
     */
    public static Attributes createInstanceAttributes() {
        Attributes attrs = createSeriesAttributes();
        attrs.setString(Tag.SOPInstanceUID, VR.UI, TEST_INSTANCE_UID);
        attrs.setString(Tag.SOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        attrs.setString(Tag.InstanceNumber, VR.IS, "1");
        return attrs;
    }

    /**
     * Create a mock DICOM input stream
     */
    public static InputStream createMockDicomStream() {
        // Simple mock - in real tests you'd create actual DICOM data
        return new ByteArrayInputStream(new byte[]{0, 0, 0, 0});
    }

    /**
     * Create multiple mock sessions
     */
    public static List<XnatImagesessiondata> createMockSessions(int count) {
        List<XnatImagesessiondata> sessions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            XnatImagesessiondata session = mock(XnatImagesessiondata.class);
            when(session.getUid()).thenReturn(TEST_STUDY_UID + "." + i);
            when(session.getSubjectId()).thenReturn(TEST_PATIENT_ID + i);
            when(session.getLabel()).thenReturn(TEST_SESSION_LABEL + i);
            when(session.getId()).thenReturn("SESSION" + String.format("%03d", i));
            when(session.getDate()).thenReturn(new Date());

            List<XnatImagescandata> scans = new ArrayList<>();
            scans.add(createMockScan());
            when(session.getScans_scan()).thenReturn(scans);

            sessions.add(session);
        }

        return sessions;
    }

    /**
     * Create multiple mock scans
     */
    public static List<XnatImagescandata> createMockScans(int count) {
        List<XnatImagescandata> scans = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            XnatImagescandata scan = mock(XnatImagescandata.class);
            when(scan.getUid()).thenReturn(TEST_SERIES_UID + "." + i);
            when(scan.getId()).thenReturn(String.valueOf(i + 1));
            when(scan.getModality()).thenReturn("CT");
            when(scan.getSeriesDescription()).thenReturn("Test Series " + i);

            List<XnatAbstractresource> resources = new ArrayList<>();
            resources.add(createMockResource());
            when(scan.getFile()).thenReturn(resources);

            scans.add(scan);
        }

        return scans;
    }
}
