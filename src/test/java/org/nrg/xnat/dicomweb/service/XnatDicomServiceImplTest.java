package org.nrg.xnat.dicomweb.service;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
}
