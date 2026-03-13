package io.github.lgwk42.notiondocs.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for notion-docs.
 * Configured via {@code notion-docs.*} in application.yml.
 */
@ConfigurationProperties(prefix = "notion-docs")
public class NotionDocsProperties {

    private boolean enabled = true;
    private String apiToken;
    private String parentPageId;
    private String databaseTitle = "API Documentation";
    private boolean syncOnStartup = true;
    private boolean archiveRemovedEndpoints = false;
    private List<String> includePackages = List.of();
    private List<String> excludePackages = List.of();
    private String defaultDomain = "";
    private List<String> defaultAuth = List.of();
    private String notionApiVersion = "2022-06-28";
    private Duration connectTimeout = Duration.ofSeconds(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getParentPageId() {
        return parentPageId;
    }

    public void setParentPageId(String parentPageId) {
        this.parentPageId = parentPageId;
    }

    public String getDatabaseTitle() {
        return databaseTitle;
    }

    public void setDatabaseTitle(String databaseTitle) {
        this.databaseTitle = databaseTitle;
    }

    public boolean isSyncOnStartup() {
        return syncOnStartup;
    }

    public void setSyncOnStartup(boolean syncOnStartup) {
        this.syncOnStartup = syncOnStartup;
    }

    public boolean isArchiveRemovedEndpoints() {
        return archiveRemovedEndpoints;
    }

    public void setArchiveRemovedEndpoints(boolean archiveRemovedEndpoints) {
        this.archiveRemovedEndpoints = archiveRemovedEndpoints;
    }

    public List<String> getIncludePackages() {
        return includePackages;
    }

    public void setIncludePackages(List<String> includePackages) {
        this.includePackages = includePackages;
    }

    public List<String> getExcludePackages() {
        return excludePackages;
    }

    public void setExcludePackages(List<String> excludePackages) {
        this.excludePackages = excludePackages;
    }

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

    public List<String> getDefaultAuth() {
        return defaultAuth;
    }

    public void setDefaultAuth(List<String> defaultAuth) {
        this.defaultAuth = defaultAuth;
    }

    public String getNotionApiVersion() {
        return notionApiVersion;
    }

    public void setNotionApiVersion(String notionApiVersion) {
        this.notionApiVersion = notionApiVersion;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Validates that required properties are set.
     *
     * @throws IllegalStateException if required properties are missing
     */
    public void validate() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException(
                    "notion-docs.api-token is required. Set your Notion Integration token.");
        }
        if (parentPageId == null || parentPageId.isBlank()) {
            throw new IllegalStateException(
                    "notion-docs.parent-page-id is required. Set the parent Notion page ID.");
        }
    }
}
