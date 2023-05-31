module SecServiceModule{
    exports application;
    exports data;
    requires java.desktop;
    requires miglayout;
    opens data to com.google.gson;
    requires ImageService;
    requires com.google.gson;
    requires java.prefs;
    requires guava;
//    opens com.udacity.catpoint.data to SecurityServiceModule;
//    opens Interface to SecurityServiceModule;



}