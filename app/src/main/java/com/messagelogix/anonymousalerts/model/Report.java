package com.messagelogix.anonymousalerts.model;

/**
 * Created by Ahmed Daou on 10/6/2015.
 * This is a model for the report , it holds all the selected values
 */
public class Report {
    private String message = "";
    private String priorityId = "0";
    private String contactId = "0";
    private String locationId = "0";
    private String buildingId = "0";
    private String incidentId = "0";
    private String submitterId = "0";
    private String buildingTypeId = "0";
    private String regionId = "0";
    private String categoryId = "0";

    public Report() {
        this.message = "";
        this.priorityId = "0";
        this.contactId = "0";
        this.locationId = "0";
        this.buildingId = "0";
        this.incidentId = "0";
        this.submitterId = "0";
        this.buildingTypeId = "0";
        this.regionId = "0";
        this.categoryId = "0";
    }

    public String getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(String priorityId) {
        this.priorityId = priorityId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getSubmitterId() {
        return submitterId;
    }


    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBuildingTypeId() {

        return buildingTypeId;
    }

    public void setBuildingTypeId(String buildingTypeId) {

        this.buildingTypeId = buildingTypeId;
    }

    public String getRegionId() {

        return regionId;
    }

    public void setRegionId(String regionId) {

        this.regionId = regionId;
    }
}
