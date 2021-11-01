package com.udacity.catpoint.service;

import com.udacity.catpoint.application.StatusListener;
import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.image.service.FakeImageService;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private final FakeImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();

    //Added for Test 10
    private boolean currentImageHasCat = false;

    public SecurityService(SecurityRepository securityRepository, FakeImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            //reset all sensors
            securityRepository.resetAllSensors(false);

            if (currentImageHasCat){
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        //Remember if the image has a cat or not.
        currentImageHasCat=cat;
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the
     * SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been
     * activated by the user.
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has
     * been deactivated by the user
     */

    private void handleSensorDeactivated() {

        if ((securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM)
                && (!securityRepository.isAnySensorActive()))
            setAlarmStatus(AlarmStatus.NO_ALARM);

        /*switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM ->  setAlarmStatus(AlarmStatus.NO_ALARM);
            case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }*/
    }

    /**
     * Change the activation status for the specified sensor and update alarm
     * status if necessary.  This function is called when a user toggles
     * the Activate/Deactivate button on the Sensor Management panel.  It
     * has nothing to do with a sensor activated by a cat.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {

        /*
        Update alarm status by comparing current state to previously saved
        sensor state.  sensor.getActive() returns the state of the sensor after
        the last change.  The current near-real-time state of the sensor
        is in the variable "active" passed to this method.
         */
        if(!sensor.getActive() && active) {
            sensor.setActive(true);   //Status chosen by user
            handleSensorActivated();    //by user
        } else if (sensor.getActive() && !active) {
            sensor.setActive(false);    //Need this to handle no active sensors
            handleSensorDeactivated();  //by user
        }

        securityRepository.updateSensor(sensor);    //...in the JSON file
    }

    /**
     * Send an image to the SecurityService for processing. The
     * securityService will use it's provided ImageService to analyze the
     * image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
