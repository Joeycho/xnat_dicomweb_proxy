package org.nrg.xnat.dicomweb.utils;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Utility class for DICOMweb operations
 */
public class DicomWebUtils {

    /**
     * Convert DICOM Attributes to JSON string
     */
    public static String toJson(Attributes attrs) throws IOException {
        StringWriter sw = new StringWriter();
        try (JsonGenerator gen = Json.createGenerator(sw)) {
            JSONWriter writer = new JSONWriter(gen);
            writer.write(attrs);
        }
        return sw.toString();
    }

    /**
     * Read DICOM attributes from input stream
     */
    public static Attributes readDicom(InputStream is) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(is)) {
            return dis.readDataset(-1, -1);
        }
    }

    /**
     * Get content type for DICOM JSON
     */
    public static String getDicomJsonContentType() {
        return "application/dicom+json";
    }

    /**
     * Get content type for multipart related DICOM
     */
    public static String getMultipartContentType(String boundary) {
        return "multipart/related; type=\"application/dicom\"; boundary=" + boundary;
    }
}
