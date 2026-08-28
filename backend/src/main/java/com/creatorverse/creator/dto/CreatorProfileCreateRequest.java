package com.creatorverse.creator.dto;

public class CreatorProfileCreateRequest {
    private String niche;
    private String bio;

    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
