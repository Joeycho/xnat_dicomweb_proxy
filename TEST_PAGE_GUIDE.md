# DICOMweb Test Page Guide

## Overview

The plugin includes a built-in test page for verifying the DICOMweb API endpoints are working correctly. This provides a visual, interactive way to test the plugin without needing curl, Postman, or other external tools.

## Access the Test Page

Once the plugin is installed in XNAT, access the test page at:

```
http://your-xnat-server/xapi/dicomweb/test
```

Or with HTTPS:

```
https://your-xnat-server/xapi/dicomweb/test
```

**Note:** You must be logged into XNAT to access this page. It uses your current XNAT session for authentication.

## Features

### 🎨 Modern UI
- Clean, responsive design
- Gradient background
- Easy-to-read results
- Color-coded status indicators

### 🔍 Test All Endpoints
- **QIDO-RS** (Query): Search studies, series, and instances
- **WADO-RS** (Retrieve): Get metadata and download DICOM files
- Real-time response display
- Execution time tracking

### 📊 Response Details
- HTTP status code (with color badges)
- Content-Type header
- Response time in milliseconds
- Full response body (pretty-printed JSON)
- Copy to clipboard functionality

## How to Use

### 1. Basic Setup

1. **Log into XNAT** in your browser
2. **Navigate to** `http://your-xnat-server/xapi/dicomweb/test`
3. **Enter your Project ID** in the configuration section

### 2. Test Search Studies (QIDO-RS)

This is the simplest test to start with:

1. Enter a valid XNAT Project ID (e.g., `MY_PROJECT`)
2. Click **"Search Studies"**
3. Results will show all imaging sessions in the project

**Expected Response:**
```json
[
  {
    "0020000D": {
      "vr": "UI",
      "Value": ["1.2.840.113619.2.55.3.123456789"]
    },
    "00100020": {
      "vr": "LO",
      "Value": ["PATIENT001"]
    },
    ...
  }
]
```

### 3. Test Search Series

To test series search:

1. From the study search results, copy a `StudyInstanceUID` value
2. Paste it into the **"Study Instance UID"** field
3. Click **"Search Series"**

**Expected Response:**
```json
[
  {
    "0020000E": {
      "vr": "UI",
      "Value": ["1.2.840.113619.2.55.3.987654321"]
    },
    "00080060": {
      "vr": "CS",
      "Value": ["CT"]
    },
    ...
  }
]
```

### 4. Test Search Instances

To test instance search:

1. From series results, copy a `SeriesInstanceUID`
2. Enter both Study and Series UIDs
3. Click **"Search Instances"**

**Expected Response:**
```json
[
  {
    "00080018": {
      "vr": "UI",
      "Value": ["1.2.840.113619.2.55.3.111111111"]
    },
    "00080016": {
      "vr": "UI",
      "Value": ["1.2.840.10008.5.1.4.1.1.2"]
    },
    ...
  }
]
```

### 5. Test Metadata Retrieval

To get instance metadata:

1. Enter Study, Series, and Instance UIDs
2. Click **"Get Instance Metadata"**
3. View full DICOM attributes in JSON format

### 6. Download DICOM File

To download an actual DICOM file:

1. Enter Study, Series, and Instance UIDs
2. Click **"Download Instance (DICOM)"**
3. Browser will prompt to download the `.dcm` file

## Understanding Results

### ✅ Success (Green Badge)

- **Status Code**: 200
- **Meaning**: Request succeeded
- **Response**: Contains DICOM data

### ❌ Error (Red Badge)

Common error codes:

- **401 Unauthorized**: Not logged into XNAT
- **403 Forbidden**: No permission for project
- **404 Not Found**: Project, study, or UID doesn't exist
- **500 Internal Server Error**: Plugin or XNAT error

### Response Metadata

The test page displays:

- **Status**: HTTP status code with color indicator
- **Content-Type**: Response format (usually `application/dicom+json`)
- **Time**: Request execution time in milliseconds

## Testing Workflow

### Recommended Testing Sequence:

1. **Start Simple**: Test "Search Studies" first
   - Verifies plugin is loaded
   - Checks project access
   - Shows available data

2. **Drill Down**: Use UIDs from results to test deeper levels
   - Copy StudyInstanceUID → Test series search
   - Copy SeriesInstanceUID → Test instance search

3. **Test Retrieval**: Download actual DICOM data
   - Test metadata endpoints
   - Download instance file

4. **Verify Data**: Check downloaded DICOM file
   ```bash
   dcmdump downloaded-file.dcm
   ```

## Troubleshooting

### Page Won't Load

**Issue**: Test page shows 404 or doesn't load

**Solutions**:
- Verify plugin is installed: Check `$XNAT_HOME/plugins/`
- Check XNAT logs: `tail -f $XNAT_HOME/logs/catalina.out`
- Ensure URL is correct: `/xapi/dicomweb/test` (not `/dicomweb/test`)

### Authentication Error

**Issue**: 401 Unauthorized

**Solutions**:
- Log into XNAT in the same browser
- Check session hasn't expired
- Try refreshing XNAT and the test page

### Empty Results

**Issue**: Search returns `[]` (empty array)

**Possible Causes**:
- Project has no imaging sessions
- Sessions missing StudyInstanceUID
- User lacks project read permissions

**Verify**:
```sql
-- Check sessions in database
SELECT id, uid, project FROM xnat_imagesessiondata
WHERE project = 'YOUR_PROJECT';
```

### 404 Not Found for Series/Instances

**Issue**: Studies work but series/instances don't

**Possible Causes**:
- Invalid UID format
- UID doesn't exist in project
- Typo when copying UID

**Check**:
- Copy UIDs carefully from response
- Verify UIDs in XNAT database
- Check XNAT logs for errors

### Network Errors

**Issue**: "Network Error" message

**Solutions**:
- Check browser console (F12) for details
- Verify XNAT is running
- Check network connectivity
- Review browser CORS settings (if applicable)

## Advanced Testing

### Using Browser DevTools

1. Open DevTools (F12)
2. Go to Network tab
3. Perform test
4. Inspect request/response details

**Useful for:**
- Viewing full HTTP headers
- Checking request payload
- Debugging errors
- Analyzing performance

### Testing with Different Projects

1. Test with multiple projects
2. Verify access control works
3. Compare response times

### Testing Different Modalities

1. Search studies in projects with different modalities (CT, MR, PET)
2. Verify ModalitiesInStudy is correct
3. Test modality-specific attributes

### Performance Testing

1. Test with large projects (100+ studies)
2. Note response times
3. Identify slow queries
4. Consider optimization if needed

## Automation

While this is a manual test page, you can automate testing using the same endpoints:

```bash
# Script to test all endpoints
PROJECT_ID="MY_PROJECT"
BASE_URL="http://localhost:8080/xapi/dicomweb"

# Test search studies
curl -u user:pass "$BASE_URL/projects/$PROJECT_ID/studies"

# Extract UID and test series
# ... etc
```

## Comparing with External Viewers

After verifying via test page, compare with:

### OHIF Viewer
- Should see same studies
- Study list should match
- Images should load

### VolView
- Should connect successfully
- Same study count
- Metadata should match

## Best Practices

1. **Always start with study search** - Basic connectivity test
2. **Copy UIDs carefully** - One wrong character breaks everything
3. **Check XNAT logs** - Errors logged even if not shown in UI
4. **Test incrementally** - Don't jump to complex queries
5. **Use real project data** - Test with actual clinical data if possible

## Test Page vs External Tools

| Feature | Test Page | curl/Postman | OHIF/VolView |
|---------|-----------|--------------|--------------|
| Easy to use | ✅ Yes | ⚠️ Medium | ✅ Yes |
| Built-in | ✅ Yes | ❌ No | ❌ No |
| Authentication | ✅ Automatic | ⚠️ Manual | ⚠️ Configure |
| Visual results | ✅ Yes | ❌ No | ✅ Yes |
| Download DICOM | ✅ Yes | ✅ Yes | ✅ Yes |
| View images | ❌ No | ❌ No | ✅ Yes |

## Security Notes

- Test page requires XNAT authentication
- Uses same-origin credentials
- No authentication bypass
- Respects all XNAT permissions
- Marked as `openUrls` only for the test page path

## Limitations

- Cannot upload DICOM (STOW-RS not implemented)
- Cannot render images (use OHIF/VolView for that)
- Single project at a time
- Manual UID entry required

## Example Test Session

```
1. Navigate to: http://xnat/xapi/dicomweb/test
2. Enter Project: "LUNG_CT_PROJECT"
3. Click "Search Studies" → Shows 15 studies ✅
4. Copy first StudyInstanceUID: "1.2.840.113619..."
5. Paste into Study UID field
6. Click "Search Series" → Shows 4 series ✅
7. Copy first SeriesInstanceUID: "1.2.840.113619..."
8. Click "Search Instances" → Shows 120 instances ✅
9. Copy first SOPInstanceUID: "1.2.840.113619..."
10. Click "Get Instance Metadata" → Shows full DICOM tags ✅
11. Click "Download Instance" → Downloads file successfully ✅

Result: All endpoints working! ✅
```

## Reporting Issues

If you find issues with the test page:

1. Check browser console for JavaScript errors
2. Review XNAT logs for backend errors
3. Note exact error messages
4. Document steps to reproduce
5. Include XNAT and plugin versions

## Summary

The test page provides:
- ✅ Quick verification plugin is working
- ✅ Visual feedback on all endpoints
- ✅ No external tools needed
- ✅ Built into the plugin
- ✅ Uses your XNAT authentication

**Quick Start**: Just navigate to `/xapi/dicomweb/test` and start testing!
