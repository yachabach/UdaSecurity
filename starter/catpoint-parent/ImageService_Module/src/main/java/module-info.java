module com.udacity.catpoint.image {

    //This is the only one I knew to specify.  All others came from
    //Intellij suggestions.
    exports com.udacity.catpoint.image.service;

    //requires java.desktop was suggested by intellij.  It is used in
    //FakeImageService to expose the BufferedImage class
    requires java.desktop;

    //All of the following were also suggested by intellij.  They are used
    //in the AwsImageService.java class.
    requires org.slf4j;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
}