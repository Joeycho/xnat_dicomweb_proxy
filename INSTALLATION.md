# Installation and Deployment Guide

## Overview

This guide provides step-by-step instructions for installing and deploying the XNAT DICOMweb Proxy Plugin.

## Prerequisites

- XNAT 1.7.5+ installed and running
- Admin access to XNAT server
- Java 8 or higher
- Gradle 5.6+ (for building from source)

## Important Note

**The service implementation (`XnatDicomServiceImpl.java`) is currently a stub** that needs to be completed based on your specific XNAT version. The XNAT data model API varies between versions, requiring version-specific implementations.

## Building the Plugin

### From Source

1. Clone or navigate to the plugin directory:
   ```bash
   cd /path/to/xnat-dicomweb-proxy
   ```

2. Build the plugin:
   ```bash
   ./gradlew clean build
   ```

3. The JAR file will be created at:
   ```
   build/libs/xnat-dicomweb-proxy-1.0.0.jar
   ```

### Using the Build Script

```bash
./build_plugin.sh
```

## Installation

### Option 1: Manual Installation

1. Copy the JAR file to XNAT's plugins directory:
   ```bash
   cp build/libs/xnat-dicomweb-proxy-1.0.0.jar $XNAT_HOME/plugins/
   ```

2. Restart XNAT:
   ```bash
   sudo systemctl restart tomcat9  # or your XNAT service
   ```

### Option 2: Using XNAT Plugin Manager

1. Log into XNAT as an administrator
2. Navigate to: **Administer → Plugin Settings**
3. Click "Install Plugin"
4. Upload `xnat-dicomweb-proxy-1.0.0.jar`
5. XNAT will automatically restart to load the plugin

## Completing the Implementation

Before the plugin will work with actual data, you need to complete the `XnatDicomServiceImpl.java` implementation:

1. Identify your XNAT version:
   ```bash
   grep "XNAT_VERSION" $XNAT_HOME/config/xnat-conf.properties
   ```

2. Review the XNAT data model API for your version:
   - XNAT 1.7.x: https://wiki.xnat.org/xnat-api/xnat-1-7-rest-api-directory
   - XNAT 1.8.x: https://wiki.xnat.org/xnat-api/xnat-1-8-rest-api-directory

3. Edit `src/main/java/org/nrg/xnat/dicomweb/service/XnatDicomServiceImpl.java`

4. Implement the TODOs using the appropriate XNAT API calls:
   - `searchStudies()` - Access imaging sessions
   - `searchSeries()` - Access scans within sessions
   - `searchInstances()` - Access DICOM files in resources
   - `retrieveInstance()` - Stream DICOM files

5. Rebuild and redeploy

## Verification

### 1. Check Plugin Loaded

View XNAT logs:
```bash
tail -f $XNAT_HOME/logs/catalina.out | grep dicomweb
```

Look for:
```
INFO  org.nrg.framework.services.impl.XnatPluginService - Loading plugin: dicomwebproxy
INFO  org.nrg.framework.services.impl.XnatPluginService - Loaded plugin: DICOMweb Proxy Plugin
```

### 2. Test API Endpoints

```bash
# Test endpoint accessibility
curl -u username:password \
  http://your-xnat-server/xapi/dicomweb/projects/PROJECT_ID/studies

# Should return:
# [] (empty array if no studies or implementation incomplete)
# or study data if implementation is complete
```

### 3. Check Swagger Documentation

Navigate to:
```
http://your-xnat-server/xapi/swagger-ui.html
```

Look for "DICOMweb QIDO-RS API" and "DICOMweb WADO-RS API" sections.

## Configuration

### CORS Configuration

For production deployments with web viewers, configure CORS in XNAT:

1. Edit `$XNAT_HOME/config/xnat-conf.properties`

2. Add CORS settings:
   ```properties
   security.cors.allowed-origins=https://ohif.example.com,https://volview.example.com
   security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
   security.cors.allowed-headers=*
   security.cors.max-age=3600
   ```

3. Restart XNAT

### Project Access

Ensure users have appropriate permissions:

1. **Read access**: Required for QIDO-RS (search)
2. **Download access**: Required for WADO-RS (retrieve)

## Connecting DICOM Viewers

### OHIF Viewer

1. Create OHIF configuration file (`app-config.js`):
   ```javascript
   window.config = {
     routerBasename: '/',
     servers: {
       dicomWeb: [
         {
           name: 'XNAT',
           wadoUriRoot: 'https://xnat.example.com/xapi/dicomweb/projects/PROJECT_ID',
           qidoRoot: 'https://xnat.example.com/xapi/dicomweb/projects/PROJECT_ID',
           wadoRoot: 'https://xnat.example.com/xapi/dicomweb/projects/PROJECT_ID',
           qidoSupportsIncludeField: false,
           imageRendering: 'wadors',
           thumbnailRendering: 'wadors',
         },
       ],
     },
   };
   ```

2. Deploy OHIF and access with XNAT credentials

### VolView

1. Launch VolView desktop application
2. Go to **File → Remote**
3. Enter DICOMweb URL:
   ```
   https://xnat.example.com/xapi/dicomweb/projects/PROJECT_ID
   ```
4. Authenticate with XNAT credentials
5. Browse and load studies

## Troubleshooting

### Plugin Not Loading

**Symptoms**: No log messages about dicomwebproxy

**Solutions**:
- Check JAR file permissions: `chmod 644 $XNAT_HOME/plugins/xnat-dicomweb-proxy-1.0.0.jar`
- Verify XNAT has write access to plugins directory
- Check for errors in `catalina.out`
- Ensure no conflicting plugins

### 404 Errors on API Endpoints

**Symptoms**: `/xapi/dicomweb/...` returns 404

**Solutions**:
- Verify plugin loaded successfully (check logs)
- Clear XNAT cache: Delete `$XNAT_HOME/work/Catalina/` and restart
- Check URL includes `/xapi` prefix
- Verify project ID is correct

### Authentication Failures

**Symptoms**: 401 Unauthorized errors

**Solutions**:
- Verify credentials are correct
- Check user has project access
- For web viewers, ensure CORS is configured
- Check XNAT session timeout settings

### Empty Results

**Symptoms**: API returns `[]` for endpoints

**Solutions**:
- **Most likely**: Implementation is not complete (still using stubs)
- Verify project contains imaging data
- Check user has read permissions
- Review XNAT logs for errors
- Verify DICOM data has required UIDs

### CORS Errors

**Symptoms**: Browser console shows CORS policy errors

**Solutions**:
- Add web viewer origin to XNAT CORS configuration
- Ensure `/dicomweb/**` is in plugin's `openUrls`
- Check preflight OPTIONS requests are allowed
- Verify CORS headers in response

## Uninstallation

1. Remove JAR file:
   ```bash
   rm $XNAT_HOME/plugins/xnat-dicomweb-proxy-1.0.0.jar
   ```

2. Restart XNAT:
   ```bash
   sudo systemctl restart tomcat9
   ```

3. (Optional) Clear plugin cache:
   ```bash
   rm -rf $XNAT_HOME/work/Catalina/
   ```

## Upgrading

1. Stop XNAT
2. Remove old JAR file
3. Copy new JAR file to plugins directory
4. Start XNAT
5. Verify new version in logs

## Security Considerations

### Production Deployment

1. **HTTPS Only**: Never use HTTP in production
2. **Restrict CORS**: Limit `allowed-origins` to specific domains
3. **Authentication**: Rely on XNAT's authentication (no bypass)
4. **Authorization**: Plugin respects XNAT project permissions
5. **Input Validation**: Plugin validates all URL parameters
6. **Rate Limiting**: Consider implementing rate limiting for public-facing deployments

### Firewall Rules

If XNAT is behind a firewall, ensure these ports are open:
- 443 (HTTPS) for API access
- Any custom XNAT ports

## Performance Tuning

### For Large Datasets

1. **Increase JVM heap**:
   ```bash
   # In $CATALINA_HOME/bin/setenv.sh
   CATALINA_OPTS="-Xms2048m -Xmx4096m"
   ```

2. **Enable caching** (requires implementing cache in plugin code)

3. **Optimize database**: Ensure XNAT database is properly indexed

4. **Use CDN**: For static DICOM data, consider CDN for better performance

## Support

For issues specific to this plugin:
- Check logs: `$XNAT_HOME/logs/catalina.out`
- Review documentation: `README.md`, `ARCHITECTURE.md`, `IMPLEMENTATION.md`
- Report issues to plugin maintainer

For XNAT-related issues:
- XNAT Documentation: https://wiki.xnat.org
- XNAT Forum: https://groups.google.com/forum/#!forum/xnat_discussion

## Next Steps

After installation:

1. Complete the `XnatDicomServiceImpl` implementation
2. Test with actual XNAT data
3. Configure CORS for web viewers
4. Connect OHIF or VolView
5. Train users on accessing DICOM data via viewers

## Additional Resources

- **README.md**: User-facing documentation
- **ARCHITECTURE.md**: Technical architecture overview
- **IMPLEMENTATION.md**: Developer implementation details
- **TESTING.md**: Testing guide
- **DICOM Standard**: http://dicom.nema.org/medical/dicom/current/output/html/part18.html
