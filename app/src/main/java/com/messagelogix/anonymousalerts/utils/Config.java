package com.messagelogix.anonymousalerts.utils;


public class Config {

    //API Constant
    public static final String API_URL = "https://k12connections.com/bluekitten/user/v1/index.php";
    public static final String API_UPLOAD_MEDIA_URL = "https://www.anonymousalerts.com/bluekitten/user/v1/index.php";
    public static final String API_LOGO_URL = "https://anonymousalerts.com/API/user/v1/view/logo/";

    public static final String API_KEY = "TkuCD!91_8m6w9v4.O4DK5b60{]z8C3q%g9[zaWnqCe)2k5zhxS!r)p71jAXqTc";
    public static final String APP_ID = "873452dbe7fb1874f80eab6b488f718c";
    public static final String SUCCESS = "success";
    public static final String DATA = "data";

    //Device
    public static final String ANDROID_DEVICE_TYPE_ID = "2";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String IS_LOGGED_IN = "LoginSession";
    public static final String LAST_VERSION_CODE = "LAST_VERSION_CODE";
    public static final String UNIQUE_ID = "unique_id";
    public static final String ANDROID_ID = "androidID";
    public static String SENDER_ID = "522649466422";

    //Account
    public static final String ACCOUNT_ID = "acct_id";
    public static final String HAS_VIDEO_FEATURE = "iphone_aa_videouploader";
    public static final String HAS_PHOTO_FEATURE = "iphone_aa_imageuploader";
    public static final String HAS_PRIORITY = "aa_portal_priority";
    public static final String HAS_LOCATION = "aa_portal_location";
    public static final String HAS_SUBMITTER = "aa_portal_submitter";
    public static final String HAS_SMS = "aa_portal_sms";
    public static final String HAS_BUILDING_TYPE = "aa_building_type_listing";
    public static final String HAS_REGIONS = "aa_regions";
    public static final String LOCATOR_TIMEOUT = "locator_timeout";
    public static final String HAS_INCIDENT_CATEGORIES = "hasIncidentCategories";

    //Menu
    public static final String HAS_SMART_BUTTON = "locator";
    public static final String HAS_INCIDENT_BUTTON = "anonymous_active";
    public static final String HAS_GLOSSARY_BUTTON = "aa_glossary";
    public static final String HAS_HELP_BUTTON = "aa_help";
    public static final String HAS_NOTIFICATION_BUTTON = "push_to_app";
    public static final String HAS_EMERGENCY_BUTTON = "aa_991_hotline";

    //Customization
    public static final String LOGO_NAME = "logo_name";
    public static final String DISPLAY_NAME = "display_name";
    public static final String STEP1_INSTRUCTION = "step1_instructions";
    public static final String STEP1_COMPLETE_FORM = "step1_completeform";
    public static final String STEP2_CONTACT = "step2_contact";
    public static final String STEP2_FILL_OUT_FORM = "step2_filloutform";
    public static final String STEP3_MESSAGE_SENT = "step3_messagesent";
    public static final String STEP3_EMERGENCY_NUMBER = "step3_emergencynumber";
    public static final String STEP3_OTHER_NUMBERS = "step3_othernumbers";
    public static final String BUILDING_NAMING = "step1_building";
    //------HTML------
    public static final String STEP1_COMPLETE_FORM_HTML = "step1_completeform_html";
    public static final String STEP2_CONTACT_HTML = "step2_contact_html_new";
    public static final String STEP2_FILL_OUT_FORM_HTML = "step2_filloutform_html_new";
    public static final String STEP3_MESSAGE_SENT_HTML = "step3_messagesent_html";
    public static final String STEP3_EMERGENCY_NUMBER_HTML = "step3_emergencynumber_html";
    public static final String STEP3_OTHER_NUMBERS_HTML = "step3_othernumbers_html";
    //Emergency Dialer
    public static final String HAS_SUICIDE_HOTLINE = "aa_suicide_hotline";
    public static final String SUICIDE_HOT_LINE = "aa_suicide_hotlinenumber";
    public static final String HOT_LINE_LABEL = "aa_suicide_hotline_label";

    //Locator
    public static final String USER_FULL_NAME = "name";
    public static final String USER_BUILDING_ID = "buildingId";
    public static final String USER_BUILDING_NAME = "buildingName";
    public static final String USER_PROFILE_PICTURE = "profile_picture";
    public static final String NEXT_TIME_ALLOWED = "nextAllowedTime";


    //[FEATURE] AA LITE
    public static final String HAS_LITE = "aa_quick_alerts";
    public static final String HAS_NONANONYMOUS_LITEREPORTS = "aa_replies_nonanonymous";
    public static final String HAS_SUBMITTER_FOR_LITE = "aa_portal_submitter";

    //[FEATURE] ANONYMOUS CHECKBOX TOGGLE
    public static final String HAS_ANON_TOGGLE = "aa1_slider_option";

    //[FEATURE] MESSAGE CENTER
    public static final String HAS_MESSAGE_CENTER = "aa_message_center";

    //[FEATURE] LOCATION VALIDATION TOGGLE
    public static final String HAS_LOCATION_VALIDATION = "aa_location_required";

    /**
     * Please replace this with a valid API key which is enabled for the
     * YouTube Data API v3 service. Go to the
     * <a href="https://console.developers.google.com/">Google Developers Console</a>
     * to register a new developer key.
     */
    public static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyAI034-nJVY-x0AXiFOXUD6k0yMZ4J6Ggw";

    //[FEATURE] Cutoff Time
    public static final String HAS_CUTOFF = "aa_hours_cutoff";
    public static final String IS_UNLOCKED = "monitoring_hours_window";
    public static final String HOURS_OF_OPERATION = "hours_of_operation";

    //[FEATURE] Pause
    public static final String HAS_PAUSE = "aa_pause_submission";
    public static final String PAUSE_TIMEOUT = "aa_pause_timeout";
    public static final String NEXT_TIME_ALLOWED_AA = "nextAllowedTimeAA";

    //[FEATURE] Add Victim/Accused
    public static final String HAS_ADD_VIC = "aa_victim_accused";

    //[FEATURE]
    public static final String AA_APP_ENABLED = "aa_app_enabled";

    public static final String LANGUAGE = "en";
}
