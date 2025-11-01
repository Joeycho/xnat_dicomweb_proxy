package org.nrg.xnat.dicomweb.plugin;

import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(
    value = "dicomwebproxy",
    name = "DICOMweb Proxy Plugin",
    description = "Exposes XNAT projects as DICOMweb endpoints for OHIF and VolView",
    entityPackages = "org.nrg.xnat.dicomweb",
    openUrls = {"/xapi/dicomweb/test"}
)
@ComponentScan({"org.nrg.xnat.dicomweb"})
public class DicomWebProxyPlugin {
}
