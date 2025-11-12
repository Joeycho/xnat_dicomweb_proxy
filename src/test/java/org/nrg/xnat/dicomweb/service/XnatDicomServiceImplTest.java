package org.nrg.xnat.dicomweb.service;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Focused tests for helper methods in {@link XnatDicomServiceImpl}.
 */
public class XnatDicomServiceImplTest {

    private XnatDicomServiceImpl service;
    private Method matchesDescriptor;
    private Method buildFallbackArchivePath;
    private Method joinPaths;
    private Method parseFrameNumbers;

    @Before
    public void setUp() throws Exception {
        service = new XnatDicomServiceImpl();

        matchesDescriptor = XnatDicomServiceImpl.class.getDeclaredMethod("matchesDicomDescriptor", String.class);
        matchesDescriptor.setAccessible(true);

        buildFallbackArchivePath = XnatDicomServiceImpl.class.getDeclaredMethod(
                "buildFallbackArchivePath", String.class, String.class, String.class);
        buildFallbackArchivePath.setAccessible(true);

        joinPaths = XnatDicomServiceImpl.class.getDeclaredMethod(
                "joinPaths", String.class, String[].class);
        joinPaths.setAccessible(true);

        parseFrameNumbers = XnatDicomServiceImpl.class.getDeclaredMethod(
                "parseFrameNumbers", String.class);
        parseFrameNumbers.setAccessible(true);
    }

    @Test
    public void matchesDicomDescriptorRecognizesSecondaryLabels() throws Exception {
        boolean result = (boolean) matchesDescriptor.invoke(service, "Secondary Review");
        assertTrue("Descriptors containing 'secondary' should be treated as DICOM", result);
    }

    @Test
    public void buildFallbackArchivePathDefaultsToArc001() throws Exception {
        String path = (String) buildFallbackArchivePath.invoke(service,
                "/data/xnat/archive/", "ProjectA", "Session01");
        assertEquals("/data/xnat/archive/ProjectA/arc001/Session01", path);
    }

    @Test
    public void joinPathsAvoidsDuplicateSeparators() throws InvocationTargetException, IllegalAccessException {
        String result = (String) joinPaths.invoke(service, "/root/", new String[]{"/nested", "child"});
        assertEquals("/root/nested/child", result.replace('\\', '/'));
    }

    @Test
    public void parseFrameNumbers_SingleFrame() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "1");
        assertEquals("Should parse single frame number", 1, result.size());
        assertEquals("Frame number should be 1", Integer.valueOf(1), result.get(0));
    }

    @Test
    public void parseFrameNumbers_MultipleFrames() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "1,2,3");
        assertEquals("Should parse three frame numbers", 3, result.size());
        assertEquals("First frame should be 1", Integer.valueOf(1), result.get(0));
        assertEquals("Second frame should be 2", Integer.valueOf(2), result.get(1));
        assertEquals("Third frame should be 3", Integer.valueOf(3), result.get(2));
    }

    @Test
    public void parseFrameNumbers_WithSpaces() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "1, 2, 3");
        assertEquals("Should parse frame numbers with spaces", 3, result.size());
        assertEquals("First frame should be 1", Integer.valueOf(1), result.get(0));
        assertEquals("Second frame should be 2", Integer.valueOf(2), result.get(1));
        assertEquals("Third frame should be 3", Integer.valueOf(3), result.get(2));
    }

    @Test
    public void parseFrameNumbers_InvalidNumbers() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "1,abc,3");
        assertEquals("Should skip invalid numbers", 2, result.size());
        assertEquals("First valid frame should be 1", Integer.valueOf(1), result.get(0));
        assertEquals("Second valid frame should be 3", Integer.valueOf(3), result.get(1));
    }

    @Test
    public void parseFrameNumbers_ZeroAndNegative() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "0,-1,1,2");
        assertEquals("Should skip zero and negative numbers", 2, result.size());
        assertEquals("First valid frame should be 1", Integer.valueOf(1), result.get(0));
        assertEquals("Second valid frame should be 2", Integer.valueOf(2), result.get(1));
    }

    @Test
    public void parseFrameNumbers_EmptyString() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "");
        assertEquals("Should return empty list for empty string", 0, result.size());
    }

    @Test
    public void parseFrameNumbers_Null() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, (String) null);
        assertEquals("Should return empty list for null", 0, result.size());
    }

    @Test
    public void parseFrameNumbers_NonSequential() throws Exception {
        List<Integer> result = (List<Integer>) parseFrameNumbers.invoke(service, "5,1,10,3");
        assertEquals("Should parse non-sequential frames", 4, result.size());
        assertEquals("Should preserve order", Integer.valueOf(5), result.get(0));
        assertEquals("Should preserve order", Integer.valueOf(1), result.get(1));
        assertEquals("Should preserve order", Integer.valueOf(10), result.get(2));
        assertEquals("Should preserve order", Integer.valueOf(3), result.get(3));
    }
}
