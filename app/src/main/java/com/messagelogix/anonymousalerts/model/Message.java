package com.messagelogix.anonymousalerts.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.spongycastle.crypto.agreement.srp.SRP6Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 6/22/2017.
 */
public class Message {
    @Expose
    private List<Message.MessageItem> data = new ArrayList<>();

    @Expose
    private Boolean success;

    /**
     * @return The data
     */
    public List<MessageItem> getData() {

        return data;
    }

    /**
     * @return The success
     */
    public Boolean getSuccess() {

        return success;
    }

    public static class MessageItem {


        @SerializedName("id")
        @Expose
        private String aaAlertId;

        @SerializedName("timestamp")
        @Expose
        private String date;

        @SerializedName("status")
        @Expose
        private String status;

        @SerializedName("confirm_code")
        @Expose
        private String code;

        @SerializedName("message")
        @Expose
        private String message;

        @SerializedName("push_notification_id")
        @Expose
        private String pushId;

        @SerializedName("submission_type_id")
        @Expose
        private String submissionTypeId;

        @SerializedName("unread_total")
        @Expose
        private String newMessageCount;

        //

        @SerializedName("pin_id")
        @Expose
        private String pinId;

        @SerializedName("acct_id")
        @Expose
        private String acctId;

        @SerializedName("contact_name")
        @Expose
        private String contactName;

        @SerializedName("contact_title")
        @Expose
        private Object contactTitle;

        @SerializedName("aalert_type_id")
        @Expose
        private String aalertTypeId;

        @SerializedName("message_type")
        @Expose
        private String messageType;

        @SerializedName("anonymous_email")
        @Expose
        private String anonymousEmail;

        @Expose
        private String priority;

        @SerializedName("sch_name")
        @Expose
        private String schName;

        @Expose
        private String location;

        @SerializedName("sender_email")
        @Expose
        private String senderEmail;

        @SerializedName("sender_name")
        @Expose
        private String senderName;

        @Expose
        private Object note;

        @SerializedName("sch_id")
        @Expose
        private String schId;

        @SerializedName("location_id")
        @Expose
        private String locationId;

        @SerializedName("contact_email")
        @Expose
        private String contactEmail;

        @Expose
        private String legit;

        @SerializedName("legit_id")
        @Expose
        private String legitId;

        @SerializedName("id_value")
        @Expose
        private String idValue;

        @SerializedName("id_value2")
        @Expose
        private String idValue2;

        @SerializedName("ip_address")
        @Expose
        private String ipAddress;

        @Expose
        private String Longitude;

        @Expose
        private String Latitude;

        @SerializedName("aa_contact_type_id")
        @Expose
        private String aaContactTypeId;

        @SerializedName("aa_contact_type")
        @Expose
        private String aaContactType;

        @SerializedName("submission_type")
        @Expose
        private String submissionType;

        @SerializedName("total_action_counter")
        @Expose
        private String totalActionCounter;

        @SerializedName("substantiated_id")
        @Expose
        private Object substantiatedId;

        @Expose
        private String substantiated;

        @SerializedName("group_id")
        @Expose
        private String groupId;

        @SerializedName("anonymous_cell")
        @Expose
        private String anonymousCell;

        @SerializedName("total_note_counter")
        @Expose
        private String totalNoteCounter;

        @SerializedName("action_by_adult")
        @Expose
        private String actionByAdult;

        @SerializedName("sender_cell")
        @Expose
        private String senderCell;

        @SerializedName("reply_to")
        @Expose
        private String replyTo;

        @SerializedName("reply_type")
        @Expose
        private String replyType;

        @SerializedName("media_file")
        @Expose
        private String mediaFile;

        @SerializedName("media_filename")
        @Expose
        private String mediaFilename;

        @SerializedName("media_filename_converted")
        @Expose
        private String mediaFilenameConverted;

        @SerializedName("media_type")
        @Expose
        private String mediaType;

        @Expose
        private String gps;

        @SerializedName("action_by_adult_id")
        @Expose
        private String actionByAdultId;

        /**
         *
         * @return
         */
        public String getStatus() {

            return status;
        }

        public String getAaAlertId() {

            return aaAlertId;
        }

        public String getDate() {

            return date;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String getNewMessageCount() {

            return newMessageCount;
        }

        public String getPushId() {

            return pushId;
        }

        public String getSubmissionType() {

            return submissionType;
        }

        public String getSubmissionTypeId() {

            return submissionTypeId;
        }

        public Object getContactTitle() {

            return contactTitle;
        }

        public Object getNote() {

            return note;
        }

        public String getAalertTypeId() {

            return aalertTypeId;
        }

        public String getAcctId() {

            return acctId;
        }

        public String getAnonymousEmail() {

            return anonymousEmail;
        }

        public String getContactEmail() {

            return contactEmail;
        }

        public String getContactName() {

            return contactName;
        }

        public String getLocation() {

            return location;
        }

        public String getLocationId() {

            return locationId;
        }

        public String getMessageType() {

            return messageType;
        }

        public String getPinId() {

            return pinId;
        }

        public String getIdValue() {

            return idValue;
        }

        public String getIdValue2() {

            return idValue2;
        }

        public String getIpAddress() {

            return ipAddress;
        }

        public String getLatitude() {

            return Latitude;
        }

        public String getLegit() {

            return legit;
        }

        public String getLegitId() {

            return legitId;
        }

        public String getLongitude() {

            return Longitude;
        }

        public String getPriority() {

            return priority;
        }

        public String getAaContactTypeId() {

            return aaContactTypeId;
        }

        public String getSchId() {

            return schId;
        }

        public String getSchName() {

            return schName;
        }

        public String getAaContactType() {

            return aaContactType;
        }

        public String getSenderEmail() {

            return senderEmail;
        }

        public String getSenderName() {

            return senderName;
        }

        public Object getSubstantiatedId() {

            return substantiatedId;
        }

        public String getActionByAdult() {

            return actionByAdult;
        }

        public String getActionByAdultId() {

            return actionByAdultId;
        }

        public String getAnonymousCell() {

            return anonymousCell;
        }

        public String getGps() {

            return gps;
        }

        public String getGroupId() {

            return groupId;
        }

        public String getMediaFile() {

            return mediaFile;
        }

        public String getMediaFilename() {

            return mediaFilename;
        }

        public String getMediaFilenameConverted() {

            return mediaFilenameConverted;
        }

        public String getMediaType() {

            return mediaType;
        }

        public String getReplyTo() {

            return replyTo;
        }

        public String getReplyType() {

            return replyType;
        }

        public String getSenderCell() {

            return senderCell;
        }

        public String getSubstantiated() {

            return substantiated;
        }

        public String getTotalActionCounter() {

            return totalActionCounter;
        }

        public String getTotalNoteCounter() {

            return totalNoteCounter;
        }


        /**
         * set
         */
        public void setDate(String date) {

            this.date = date;
        }

        public void setAaAlertId(String aaAlertId) {

            this.aaAlertId = aaAlertId;
        }


        public void setStatus(String status) {

            this.status = status;
        }

        public void setCode(String code) {

            this.code = code;
        }

        public void setMessage(String message) {

            this.message = message;
        }

        public void setNewMessageCount(String newMessageCount) {

            this.newMessageCount = newMessageCount;
        }

        public void setPushId(String pushId) {

            this.pushId = pushId;
        }

        public void setSubmissionType(String submissionType) {

            this.submissionType = submissionType;
        }

        public void setAalertTypeId(String aalertTypeId) {

            this.aalertTypeId = aalertTypeId;
        }

        public void setAcctId(String acctId) {

            this.acctId = acctId;
        }

        public void setAaContactType(String aaContactType) {

            this.aaContactType = aaContactType;
        }

        public void setAnonymousEmail(String anonymousEmail) {

            this.anonymousEmail = anonymousEmail;
        }

        public void setContactName(String contactName) {

            this.contactName = contactName;
        }

        public void setAaContactTypeId(String aaContactTypeId) {

            this.aaContactTypeId = aaContactTypeId;
        }

        public void setContactTitle(Object contactTitle) {

            this.contactTitle = contactTitle;
        }

        public void setLocation(String location) {

            this.location = location;
        }

        public void setMessageType(String messageType) {

            this.messageType = messageType;
        }

        public void setLocationId(String locationId) {

            this.locationId = locationId;
        }

        public void setNote(Object note) {

            this.note = note;
        }

        public void setPinId(String pinId) {

            this.pinId = pinId;
        }

        public void setContactEmail(String contactEmail) {

            this.contactEmail = contactEmail;
        }

        public void setIdValue(String idValue) {

            this.idValue = idValue;
        }

        public void setIdValue2(String idValue2) {

            this.idValue2 = idValue2;
        }

        public void setIpAddress(String ipAddress) {

            this.ipAddress = ipAddress;
        }

        public void setLegit(String legit) {

            this.legit = legit;
        }

        public void setLegitId(String legitId) {

            this.legitId = legitId;
        }

        public void setLongitude(String longitude) {

            Longitude = longitude;
        }

        public void setPriority(String priority) {

            this.priority = priority;
        }

        public void setLatitude(String latitude) {

            Latitude = latitude;
        }

        public void setSchId(String schId) {

            this.schId = schId;
        }

        public void setActionByAdult(String actionByAdult) {

            this.actionByAdult = actionByAdult;
        }

        public void setSchName(String schName) {

            this.schName = schName;
        }

        public void setActionByAdultId(String actionByAdultId) {

            this.actionByAdultId = actionByAdultId;
        }

        public void setSenderEmail(String senderEmail) {

            this.senderEmail = senderEmail;
        }

        public void setSenderName(String senderName) {

            this.senderName = senderName;
        }

        public void setAnonymousCell(String anonymousCell) {

            this.anonymousCell = anonymousCell;
        }

        public void setGps(String gps) {

            this.gps = gps;
        }

        public void setGroupId(String groupId) {

            this.groupId = groupId;
        }

        public void setMediaFile(String mediaFile) {

            this.mediaFile = mediaFile;
        }

        public void setMediaFilename(String mediaFilename) {

            this.mediaFilename = mediaFilename;
        }

        public void setMediaFilenameConverted(String mediaFilenameConverted) {

            this.mediaFilenameConverted = mediaFilenameConverted;
        }

        public void setMediaType(String mediaType) {

            this.mediaType = mediaType;
        }

        public void setReplyTo(String replyTo) {

            this.replyTo = replyTo;
        }

        public void setReplyType(String replyType) {

            this.replyType = replyType;
        }

        public void setSenderCell(String senderCell) {

            this.senderCell = senderCell;
        }

        public void setSubstantiated(String substantiated) {

            this.substantiated = substantiated;
        }

        public void setSubstantiatedId(Object substantiatedId) {

            this.substantiatedId = substantiatedId;
        }

        public void setTotalActionCounter(String totalActionCounter) {

            this.totalActionCounter = totalActionCounter;
        }

        public void setTotalNoteCounter(String totalNoteCounter) {

            this.totalNoteCounter = totalNoteCounter;
        }

        public void setSubmissionTypeId(String submissionTypeId) {

            this.submissionTypeId = submissionTypeId;
        }
    }
}
