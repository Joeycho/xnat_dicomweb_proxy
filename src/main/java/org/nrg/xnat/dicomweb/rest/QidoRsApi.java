package org.nrg.xnat.dicomweb.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.dcm4che3.data.Attributes;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.dicomweb.service.XnatDicomService;
import org.nrg.xnat.dicomweb.utils.DicomWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * QIDO-RS (Query based on ID for DICOM Objects over RESTful Services)
 * Implements DICOMweb search endpoints
 */
@XapiRestController
@Api("DICOMweb QIDO-RS API")
public class QidoRsApi extends AbstractXapiRestController {

    private static final Logger logger = LoggerFactory.getLogger(QidoRsApi.class);

    private final XnatDicomService dicomService;

    @Autowired
    public QidoRsApi(final XnatDicomService dicomService,
                     final UserManagementServiceI userManagementService,
                     final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.dicomService = dicomService;
    }

    /**
     * Search for studies in a project
     * GET /dicomweb/projects/{projectId}/studies
     */
    @XapiRequestMapping(
            value = "/dicomweb/projects/{projectId}/studies",
            method = RequestMethod.GET,
            produces = "application/dicom+json"
    )
    @ApiOperation(value = "Search for studies in a project (QIDO-RS)", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Studies found"),
            @ApiResponse(code = 401, message = "Must be authenticated"),
            @ApiResponse(code = 404, message = "Project not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    public ResponseEntity<String> searchStudies(@PathVariable String projectId) {
        try {
            UserI user = getSessionUser();
            List<Attributes> studies = dicomService.searchStudies(user, projectId, null);

            // Convert to JSON array
            String json = "[" + studies.stream()
                    .map(attrs -> {
                        try {
                            return DicomWebUtils.toJson(attrs);
                        } catch (Exception e) {
                            logger.error("Error converting study to JSON", e);
                            return "{}";
                        }
                    })
                    .collect(Collectors.joining(",")) + "]";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(DicomWebUtils.getDicomJsonContentType()))
                    .body(json);

        } catch (Exception e) {
            logger.error("Error searching studies in project: " + projectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search for series in a study
     * GET /dicomweb/projects/{projectId}/studies/{studyUID}/series
     */
    @XapiRequestMapping(
            value = "/dicomweb/projects/{projectId}/studies/{studyUID}/series",
            method = RequestMethod.GET,
            produces = "application/dicom+json"
    )
    @ApiOperation(value = "Search for series in a study (QIDO-RS)", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Series found"),
            @ApiResponse(code = 401, message = "Must be authenticated"),
            @ApiResponse(code = 404, message = "Study not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    public ResponseEntity<String> searchSeries(@PathVariable String projectId,
                                               @PathVariable String studyUID) {
        try {
            UserI user = getSessionUser();
            List<Attributes> series = dicomService.searchSeries(user, projectId, studyUID, null);

            String json = "[" + series.stream()
                    .map(attrs -> {
                        try {
                            return DicomWebUtils.toJson(attrs);
                        } catch (Exception e) {
                            logger.error("Error converting series to JSON", e);
                            return "{}";
                        }
                    })
                    .collect(Collectors.joining(",")) + "]";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(DicomWebUtils.getDicomJsonContentType()))
                    .body(json);

        } catch (Exception e) {
            logger.error("Error searching series in study: " + studyUID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search for instances in a series
     * GET /dicomweb/projects/{projectId}/studies/{studyUID}/series/{seriesUID}/instances
     */
    @XapiRequestMapping(
            value = "/dicomweb/projects/{projectId}/studies/{studyUID}/series/{seriesUID}/instances",
            method = RequestMethod.GET,
            produces = "application/dicom+json"
    )
    @ApiOperation(value = "Search for instances in a series (QIDO-RS)", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Instances found"),
            @ApiResponse(code = 401, message = "Must be authenticated"),
            @ApiResponse(code = 404, message = "Series not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    public ResponseEntity<String> searchInstances(@PathVariable String projectId,
                                                  @PathVariable String studyUID,
                                                  @PathVariable String seriesUID) {
        try {
            UserI user = getSessionUser();
            List<Attributes> instances = dicomService.searchInstances(user, projectId, studyUID, seriesUID, null);

            String json = "[" + instances.stream()
                    .map(attrs -> {
                        try {
                            return DicomWebUtils.toJson(attrs);
                        } catch (Exception e) {
                            logger.error("Error converting instance to JSON", e);
                            return "{}";
                        }
                    })
                    .collect(Collectors.joining(",")) + "]";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(DicomWebUtils.getDicomJsonContentType()))
                    .body(json);

        } catch (Exception e) {
            logger.error("Error searching instances in series: " + seriesUID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
