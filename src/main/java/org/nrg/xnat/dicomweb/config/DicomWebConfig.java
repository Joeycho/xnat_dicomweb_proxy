package org.nrg.xnat.dicomweb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.xnat.dicomweb"})
public class DicomWebConfig {
    // CORS configuration is handled by XNAT's built-in CORS support
    // or can be configured via XNAT's security settings
}
