package com.messagelogix.anonymousalerts.model;

/**
 * Created by Program on 7/14/2015.
 */
public class LocatorMessage {
    private Data[] data;

    private boolean success;

    public Data[] getData() {
        return data;
    }

    public void setData(Data[] data) {
        this.data = data;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ClassPojo [data = " + data + ", success = " + success + "]";
    }

    public class Data {
        private String message;

        private String id;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "ClassPojo [message = " + message + ", id = " + id + "]";
        }
    }
}
