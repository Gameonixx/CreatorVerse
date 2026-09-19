package com.creatorverse.brand.dto;

import org.hibernate.validator.constraints.URL;

public class BrandProfileCreateRequest {
    private String companyName;
    private String description;
    private String industry;
    
    @URL(message = "Invalid URL format")
    private String websiteUrl;
    
    private String logoUrl;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { 
        if (websiteUrl != null && !websiteUrl.trim().isEmpty()) {
            websiteUrl = websiteUrl.trim();
            if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                websiteUrl = "https://" + websiteUrl;
            }
        }
        this.websiteUrl = websiteUrl; 
    }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
