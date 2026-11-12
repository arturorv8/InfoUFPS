package org.infoufps.application.venture;

public class TestVentureSettings implements VentureSettings {

    private int maxVentures = 3;
    private int dailyLimit = 5;
    private int reportThreshold = 3;

    @Override
    public int maxVenturesPerUser() {
        return maxVentures;
    }

    @Override
    public int dailyCreationLimit() {
        return dailyLimit;
    }

    @Override
    public int reportThresholdForReview() {
        return reportThreshold;
    }

    @Override
    public String frontendBaseUrl() {
        return "http://localhost:5173";
    }

    @Override
    public String frontendVenturePath() {
        return "/emprendimientos/{id}";
    }

    @Override
    public String apiBaseUrl() {
        return "http://localhost:8080";
    }

    @Override
    public String shareLogoUrl() {
        return "http://localhost:5173/logo.png";
    }
}

