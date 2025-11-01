package org.nrg.xnat.dicomweb.service;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xft.security.UserI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.CriteriaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * XNAT 1.9.x implementation of DICOM service
 */
@Service
public class XnatDicomServiceImpl implements XnatDicomService {

    private static final Logger logger = LoggerFactory.getLogger(XnatDicomServiceImpl.class);

    @Override
    public List<Attributes> searchStudies(UserI user, String projectId, Attributes queryAttributes) {
        List<Attributes> results = new ArrayList<>();

        try {
            // Get project and check permissions
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
            if (project == null) {
                logger.warn("Project not found or user does not have access: {}", projectId);
                return results;
            }

            // Search for image sessions in this project using CriteriaCollection
            CriteriaCollection cc = new CriteriaCollection("AND");
            cc.addClause("xnat:imageSessionData/project", projectId);

            ArrayList sessions = XnatImagesessiondata.getXnatImagesessiondatasByField(
                "xnat:imageSessionData/project", projectId, user, false);

            logger.debug("Found {} sessions in project {}",
                        sessions != null ? sessions.size() : 0, projectId);

            if (sessions != null) {
                for (Object sessionObj : sessions) {
                    try {
                        if (sessionObj instanceof XnatImagesessiondata) {
                            XnatImagesessiondata session = (XnatImagesessiondata) sessionObj;

                            // Only include sessions with StudyInstanceUID
                            String studyUID = session.getUid();
                            if (studyUID != null && !studyUID.isEmpty()) {
                                Attributes attrs = createStudyAttributes(session);
                                results.add(attrs);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error processing session", e);
                    }
                }
            }

            logger.info("Study search for project {} returned {} studies", projectId, results.size());

        } catch (Exception e) {
            logger.error("Error searching studies in project: " + projectId, e);
        }

        return results;
    }

    @Override
    public List<Attributes> searchSeries(UserI user, String projectId, String studyInstanceUID, Attributes queryAttributes) {
        List<Attributes> results = new ArrayList<>();

        try {
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
            if (project == null) {
                return results;
            }

            // Find the session with matching StudyInstanceUID
            XnatImagesessiondata targetSession = findSessionByUID(user, projectId, studyInstanceUID);

            if (targetSession == null) {
                logger.warn("Study not found: {}", studyInstanceUID);
                return results;
            }

            // Get all scans (series) in the session
            List scans = targetSession.getScans_scan();

            logger.debug("Found {} scans in session {}", scans.size(), targetSession.getId());

            for (Object scanObj : scans) {
                XnatImagescandata scan = (XnatImagescandata) scanObj;
                Attributes attrs = createSeriesAttributes(scan, studyInstanceUID);
                results.add(attrs);
            }

            logger.info("Series search for study {} returned {} series", studyInstanceUID, results.size());

        } catch (Exception e) {
            logger.error("Error searching series in study: " + studyInstanceUID, e);
        }

        return results;
    }

    @Override
    public List<Attributes> searchInstances(UserI user, String projectId, String studyInstanceUID,
                                           String seriesInstanceUID, Attributes queryAttributes) {
        List<Attributes> results = new ArrayList<>();

        try {
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
            if (project == null) {
                return results;
            }

            // Find the scan by SeriesInstanceUID
            XnatImagesessiondata session = findSessionByUID(user, projectId, studyInstanceUID);
            if (session == null) {
                logger.warn("Study not found: {}", studyInstanceUID);
                return results;
            }

            XnatImagescandata targetScan = null;
            List scans = session.getScans_scan();

            for (Object scanObj : scans) {
                XnatImagescandata scan = (XnatImagescandata) scanObj;
                if (seriesInstanceUID.equals(scan.getUid())) {
                    targetScan = scan;
                    break;
                }
            }

            if (targetScan == null) {
                logger.warn("Series not found: {}", seriesInstanceUID);
                return results;
            }

            // Get DICOM files for this scan
            results = readDicomFilesFromScan(targetScan);

            logger.info("Instance search for series {} returned {} instances", seriesInstanceUID, results.size());

        } catch (Exception e) {
            logger.error("Error searching instances in series: " + seriesInstanceUID, e);
        }

        return results;
    }

    @Override
    public InputStream retrieveInstance(UserI user, String projectId, String studyInstanceUID,
                                       String seriesInstanceUID, String sopInstanceUID) {
        try {
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
            if (project == null) {
                return null;
            }

            // Find the session and scan
            XnatImagesessiondata session = findSessionByUID(user, projectId, studyInstanceUID);
            if (session == null) {
                return null;
            }

            XnatImagescandata targetScan = null;
            List scans = session.getScans_scan();

            for (Object scanObj : scans) {
                XnatImagescandata scan = (XnatImagescandata) scanObj;
                if (seriesInstanceUID.equals(scan.getUid())) {
                    targetScan = scan;
                    break;
                }
            }

            if (targetScan == null) {
                return null;
            }

            // Find the specific DICOM file
            File dicomFile = findDicomFileInScan(targetScan, sopInstanceUID);

            if (dicomFile != null) {
                logger.info("Retrieved instance: {}", sopInstanceUID);
                return new FileInputStream(dicomFile);
            }

        } catch (Exception e) {
            logger.error("Error retrieving instance: " + sopInstanceUID, e);
        }

        return null;
    }

    @Override
    public Attributes retrieveMetadata(UserI user, String projectId, String studyInstanceUID,
                                      String seriesInstanceUID, String sopInstanceUID) {
        List<Attributes> instances = searchInstances(user, projectId, studyInstanceUID, seriesInstanceUID, null);

        for (Attributes attrs : instances) {
            if (sopInstanceUID.equals(attrs.getString(Tag.SOPInstanceUID))) {
                return attrs;
            }
        }

        return null;
    }

    @Override
    public List<InputStream> retrieveStudy(UserI user, String projectId, String studyInstanceUID) {
        List<InputStream> streams = new ArrayList<>();

        try {
            List<Attributes> series = searchSeries(user, projectId, studyInstanceUID, null);

            for (Attributes seriesAttrs : series) {
                String seriesUID = seriesAttrs.getString(Tag.SeriesInstanceUID);
                streams.addAll(retrieveSeries(user, projectId, studyInstanceUID, seriesUID));
            }

        } catch (Exception e) {
            logger.error("Error retrieving study: " + studyInstanceUID, e);
        }

        return streams;
    }

    @Override
    public List<InputStream> retrieveSeries(UserI user, String projectId, String studyInstanceUID, String seriesInstanceUID) {
        List<InputStream> streams = new ArrayList<>();

        try {
            List<Attributes> instances = searchInstances(user, projectId, studyInstanceUID, seriesInstanceUID, null);

            for (Attributes attrs : instances) {
                String sopUID = attrs.getString(Tag.SOPInstanceUID);
                InputStream stream = retrieveInstance(user, projectId, studyInstanceUID, seriesInstanceUID, sopUID);
                if (stream != null) {
                    streams.add(stream);
                }
            }

        } catch (Exception e) {
            logger.error("Error retrieving series: " + seriesInstanceUID, e);
        }

        return streams;
    }

    @Override
    public byte[] retrieveRenderedInstance(UserI user, String projectId, String studyInstanceUID,
                                          String seriesInstanceUID, String sopInstanceUID) {
        try {
            XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
            if (project == null) {
                return null;
            }

            // Find the session and scan
            XnatImagesessiondata session = findSessionByUID(user, projectId, studyInstanceUID);
            if (session == null) {
                return null;
            }

            XnatImagescandata targetScan = null;
            List scans = session.getScans_scan();

            for (Object scanObj : scans) {
                XnatImagescandata scan = (XnatImagescandata) scanObj;
                if (seriesInstanceUID.equals(scan.getUid())) {
                    targetScan = scan;
                    break;
                }
            }

            if (targetScan == null) {
                return null;
            }

            // Find the specific DICOM file
            File dicomFile = findDicomFileInScan(targetScan, sopInstanceUID);

            if (dicomFile != null) {
                logger.info("Rendering instance: {}", sopInstanceUID);
                return renderDicomToJpeg(dicomFile);
            }

        } catch (Exception e) {
            logger.error("Error rendering instance: " + sopInstanceUID, e);
        }

        return null;
    }

    // Helper methods

    /**
     * Find session by StudyInstanceUID
     */
    private XnatImagesessiondata findSessionByUID(UserI user, String projectId, String studyUID) {
        try {
            // Search by UID field
            ArrayList sessions = XnatImagesessiondata.getXnatImagesessiondatasByField(
                "xnat:imageSessionData/UID", studyUID, user, false);

            if (sessions != null && !sessions.isEmpty()) {
                for (Object sessionObj : sessions) {
                    if (sessionObj instanceof XnatImagesessiondata) {
                        XnatImagesessiondata session = (XnatImagesessiondata) sessionObj;
                        // Verify it's in the correct project
                        if (projectId.equals(session.getProject())) {
                            return session;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error finding session by UID: " + studyUID, e);
        }

        return null;
    }

    /**
     * Create study-level DICOM attributes from session
     */
    private Attributes createStudyAttributes(XnatImagesessiondata session) {
        Attributes attrs = new Attributes();

        try {
            // Required return attributes for QIDO-RS Study query
            String studyUID = session.getUid();
            if (studyUID != null && !studyUID.isEmpty()) {
                attrs.setString(Tag.StudyInstanceUID, VR.UI, studyUID);
            }

            String subjectId = session.getSubjectId();
            attrs.setString(Tag.PatientName, VR.PN, subjectId != null ? subjectId : "UNKNOWN");
            attrs.setString(Tag.PatientID, VR.LO, subjectId != null ? subjectId : "UNKNOWN");

            // Format date
            Object sessionDateObj = session.getDate();
            if (sessionDateObj != null) {
                String dateStr = sessionDateObj.toString().replaceAll("-", "");
                attrs.setString(Tag.StudyDate, VR.DA, dateStr);
            } else {
                attrs.setString(Tag.StudyDate, VR.DA, "");
            }

            String label = session.getLabel();
            attrs.setString(Tag.StudyDescription, VR.LO, label != null ? label : "");
            attrs.setString(Tag.AccessionNumber, VR.SH, label != null ? label : "");

            String id = session.getId();
            attrs.setString(Tag.StudyID, VR.SH, id != null ? id : "");

            // Add modalities in study
            List scans = session.getScans_scan();
            if (scans != null && !scans.isEmpty()) {
                List<String> modalities = new ArrayList<>();
                for (Object scanObj : scans) {
                    XnatImagescandata scan = (XnatImagescandata) scanObj;
                    String modality = scan.getModality();
                    if (modality != null && !modality.isEmpty() && !modalities.contains(modality)) {
                        modalities.add(modality);
                    }
                }
                if (!modalities.isEmpty()) {
                    attrs.setString(Tag.ModalitiesInStudy, VR.CS, String.join("\\", modalities));
                }
            }

        } catch (Exception e) {
            logger.error("Error creating study attributes", e);
        }

        return attrs;
    }

    /**
     * Create series-level DICOM attributes from scan
     */
    private Attributes createSeriesAttributes(XnatImagescandata scan, String studyUID) {
        Attributes attrs = new Attributes();

        try {
            // Series-level attributes
            String seriesUID = scan.getUid();
            if (seriesUID != null && !seriesUID.isEmpty()) {
                attrs.setString(Tag.SeriesInstanceUID, VR.UI, seriesUID);
            }

            String modality = scan.getModality();
            attrs.setString(Tag.Modality, VR.CS, modality != null ? modality : "OT");

            String scanId = scan.getId();
            attrs.setString(Tag.SeriesNumber, VR.IS, scanId != null ? scanId : "1");

            String description = scan.getSeriesDescription();
            attrs.setString(Tag.SeriesDescription, VR.LO, description != null ? description : "");

            // Add study-level attributes
            attrs.setString(Tag.StudyInstanceUID, VR.UI, studyUID);

        } catch (Exception e) {
            logger.error("Error creating series attributes", e);
        }

        return attrs;
    }

    /**
     * Read DICOM files from scan resources
     */
    private List<Attributes> readDicomFilesFromScan(XnatImagescandata scan) {
        List<Attributes> results = new ArrayList<>();

        try {
            // Get resources/files from the scan
            List resources = scan.getFile();

            if (resources != null) {
                for (Object resourceObj : resources) {
                    if (resourceObj instanceof XnatAbstractresource) {
                        XnatAbstractresource resource = (XnatAbstractresource) resourceObj;

                        // Check if this is a DICOM resource
                        String label = resource.getLabel();
                        if ("DICOM".equalsIgnoreCase(label)) {
                            // Get catalog directory from resource
                            String catalogPath = getResourcePath(resource, scan);

                            if (catalogPath != null) {
                                File catalogDir = new File(catalogPath);

                                if (catalogDir.exists() && catalogDir.isDirectory()) {
                                    File[] files = catalogDir.listFiles((dir, name) ->
                                        name.toLowerCase().endsWith(".dcm") ||
                                        name.toLowerCase().endsWith(".dicom"));

                                    if (files != null) {
                                        for (File dicomFile : files) {
                                            try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
                                                Attributes attrs = dis.readDataset(-1, -1);
                                                results.add(attrs);
                                            } catch (Exception e) {
                                                logger.warn("Error reading DICOM file: " + dicomFile.getName(), e);
                                            }
                                        }
                                    }
                                } else {
                                    logger.debug("Catalog directory does not exist: {}", catalogPath);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error reading DICOM files from scan", e);
        }

        return results;
    }

    /**
     * Find specific DICOM file by SOPInstanceUID
     */
    private File findDicomFileInScan(XnatImagescandata scan, String sopInstanceUID) {
        try {
            List resources = scan.getFile();

            if (resources != null) {
                for (Object resourceObj : resources) {
                    if (resourceObj instanceof XnatAbstractresource) {
                        XnatAbstractresource resource = (XnatAbstractresource) resourceObj;

                        String label = resource.getLabel();
                        if ("DICOM".equalsIgnoreCase(label)) {
                            String catalogPath = getResourcePath(resource, scan);

                            if (catalogPath != null) {
                                File catalogDir = new File(catalogPath);

                                if (catalogDir.exists() && catalogDir.isDirectory()) {
                                    File[] files = catalogDir.listFiles((dir, name) ->
                                        name.toLowerCase().endsWith(".dcm") ||
                                        name.toLowerCase().endsWith(".dicom"));

                                    if (files != null) {
                                        for (File dicomFile : files) {
                                            try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
                                                Attributes attrs = dis.readDataset(-1, -1);
                                                String fileSOPUID = attrs.getString(Tag.SOPInstanceUID);

                                                if (sopInstanceUID.equals(fileSOPUID)) {
                                                    return dicomFile;
                                                }
                                            } catch (Exception e) {
                                                logger.debug("Error reading DICOM file: " + dicomFile.getName(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error finding DICOM file in scan", e);
        }

        return null;
    }

    /**
     * Get file system path for a resource
     *
     * NOTE: This uses XNAT default path conventions. You may need to adjust
     * the base archive path based on your XNAT installation.
     */
    private String getResourcePath(XnatAbstractresource resource, XnatImagescandata scan) {
        try {
            // Construct path based on XNAT conventions
            // Adjust baseArchive if your XNAT uses a different path
            String baseArchive = System.getProperty("xnat.archive", "/data/xnat/archive");

            XnatImagesessiondata session = (XnatImagesessiondata) scan.getImageSessionData();
            if (session != null) {
                String projectId = session.getProject();
                String sessionLabel = session.getLabel();

                // Standard XNAT archive structure
                String path = baseArchive + "/" + projectId + "/arc001/" +
                             sessionLabel + "/SCANS/" + scan.getId() + "/" + resource.getLabel();

                return path;
            }
        } catch (Exception e) {
            logger.debug("Error getting resource path", e);
        }

        return null;
    }

    /**
     * Render a DICOM file to JPEG format
     */
    private byte[] renderDicomToJpeg(File dicomFile) {
        try {
            // Use ImageIO with DICOM plugin to read the image
            ImageInputStream iis = ImageIO.createImageInputStream(dicomFile);
            if (iis == null) {
                logger.error("Could not create ImageInputStream for DICOM file");
                return null;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
            if (!readers.hasNext()) {
                logger.error("No DICOM ImageReader found");
                iis.close();
                return null;
            }

            ImageReader reader = readers.next();
            reader.setInput(iis, false);

            DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();

            // Read the first frame (middle frame would be better but requires more logic)
            BufferedImage bufferedImage = reader.read(0, param);

            reader.dispose();
            iis.close();

            if (bufferedImage == null) {
                logger.error("Could not read image from DICOM file");
                return null;
            }

            // Convert to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "JPEG", baos);

            logger.debug("Successfully rendered DICOM to JPEG, size: {} bytes", baos.size());

            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error rendering DICOM to JPEG", e);
            return null;
        }
    }
}
