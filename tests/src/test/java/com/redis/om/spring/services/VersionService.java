package com.redis.om.spring.services;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Service for managing application version information.
 * This is a test fixture for demonstrating version-based index naming.
 */
@Component
public class VersionService {

    private final String version;

    public VersionService(Environment environment) {
        this.version = environment.getProperty("app.version", "1.0.0");
    }

    /**
     * Get the full version string.
     *
     * @return the full version (e.g., "2.0.1")
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the major version number.
     *
     * @return the major version
     */
    public String getMajorVersion() {
        String[] parts = version.split("\\.");
        return parts.length > 0 ? parts[0] : "1";
    }

    /**
     * Get the minor version number.
     *
     * @return the minor version
     */
    public String getMinorVersion() {
        String[] parts = version.split("\\.");
        return parts.length > 1 ? parts[1] : "0";
    }

    /**
     * Get the patch version number.
     *
     * @return the patch version
     */
    public String getPatchVersion() {
        String[] parts = version.split("\\.");
        return parts.length > 2 ? parts[2] : "0";
    }

    /**
     * Check if this is a major version (x.0.0).
     *
     * @return true if this is a major version
     */
    public boolean isMajorVersion() {
        return "0".equals(getMinorVersion()) && "0".equals(getPatchVersion());
    }

    /**
     * Get the next version string based on increment type.
     *
     * @param incrementType the type of increment (major, minor, patch)
     * @return the next version string
     */
    public String getNextVersion(String incrementType) {
        int major = Integer.parseInt(getMajorVersion());
        int minor = Integer.parseInt(getMinorVersion());
        int patch = Integer.parseInt(getPatchVersion());

        switch (incrementType.toLowerCase()) {
            case "major":
                return (major + 1) + ".0.0";
            case "minor":
                return major + "." + (minor + 1) + ".0";
            case "patch":
            default:
                return major + "." + minor + "." + (patch + 1);
        }
    }
}