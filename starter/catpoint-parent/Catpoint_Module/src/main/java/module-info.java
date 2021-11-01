module Catpoint.Module {
    requires com.udacity.catpoint.image;
    requires miglayout;
    requires java.desktop;
    requires com.google.gson;
    requires com.google.common;
    requires java.prefs;
    opens com.udacity.catpoint.data to com.google.gson;
    //opens com.udacity.catpoint.service.SecurityService;

}