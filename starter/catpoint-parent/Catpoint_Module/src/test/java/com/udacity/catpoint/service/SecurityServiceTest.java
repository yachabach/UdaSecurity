package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.eq;


import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityServiceTest {

    //The class we are testing
    SecurityService securityService;

    //We are mocking the SecurityRepository, image service, and sensor
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private FakeImageService imageService;
    @Mock
    private Sensor sensor;

    //We are going to Fake a Set of sensors

    //Instantiate the security service
    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    /*  Test 1
    1.	If alarm is armed and a sensor becomes activated, put the system into
    pending alarm status.

    Here we are going to send all possible arming statuses to the method.
    We will have to check for the DISARMED status as that will not return
    PENDING_ALARM to the repository.

    For us to only get a PENDING_ALARM,  handleSensorActivated has to see that
    the Armed Status in the repository is not DISARMED and that the Alarm
    Status in the repository is NO_ALARM.
     */
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("Test 1: DISARMED-Ignore activation; ARMED & Activated sets PENDING_ALARM")
    @EnumSource(ArmingStatus.class)
    void changeSensorActivationStatus_sends_PENDING_ALARM(ArmingStatus armingStatus) {

        if (armingStatus == ArmingStatus.DISARMED) {
            //Nothing should happen when disarmed
            verify(securityRepository, never()).setAlarmStatus(any());
        } else {
            Mockito.when(sensor.getActive()).thenReturn(false);
            Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
            Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

            securityService.changeSensorActivationStatus(sensor, true);
            verify(securityRepository).setAlarmStatus(eq(AlarmStatus.PENDING_ALARM));
        }

    }

    /*Test 2 - If alarm is armed *and* a sensor becomes activated *and* the system is
    already pending alarm, set off the alarm.
     */
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("Test 2: DISARMED-Ignore activation; ARMED and Activated sets PENDING_ALARM")
    @EnumSource(ArmingStatus.class)
    void changeSensorActivationStatus_sends_ALARM(ArmingStatus armingStatus) {

        if (armingStatus == ArmingStatus.DISARMED) {
            //Nothing should happen when disarmed
            verify(securityRepository, never()).setAlarmStatus(any());
        } else {
            Mockito.when(sensor.getActive()).thenReturn(false);
            Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
            Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

            securityService.changeSensorActivationStatus(sensor, true);
            verify(securityRepository).setAlarmStatus(eq(AlarmStatus.ALARM));
        }
    }

    /*  Test 3
    If pending alarm *and* all sensors are inactive, return to no alarm state.

    To be in a PENDING_ALARM state a sensor must have been active at some point.
    So we will pass "true" when the method asks for the status of the sensor
    from the repository.
     */
    @Test
    @DisplayName("Test 3: All inactive and PENDING_ALARM sets NO_ALARM")
    void inactive_sensors_and_PENDING_ALARM_sends_NO_ALARM() {

        //Sensor had to have been active at some point for the alarm status
        //to be PENDING_ALARM
        Mockito.when(sensor.getActive()).thenReturn(true);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        //All sensors are now inactive
        Mockito.when(securityRepository.isAnySensorActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, false);

        //Verify we send the correct parameter to setAlarmStatus
        verify(securityRepository).setAlarmStatus(eq(AlarmStatus.NO_ALARM));
    }

    /*  Test 4
    If alarm is active, change in sensor state should not affect the alarm state.
     */
    @Test
    @DisplayName("Test 4: ALARM and deactivated sensor leaves ALARM")
    void inactive_sensors_and_ALARM_no_state_change() {

        //Sensor had to have been active at some point for the alarm status
        //to be ALARM
        Mockito.when(sensor.getActive()).thenReturn(true);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        //Mockito.when(securityRepository.isAnySensorActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, false);

        //Verify we never setAlarmStatus
        verify(securityRepository, times(0)).setAlarmStatus(any());
    }

    /*
    Test 5: If a sensor is activated *while* already active *and* the system is in pending state,
    change it to alarm state.
     */
    @Test
    @DisplayName("Test 5: Additional Sensor activated with PENDING_ALARM makes ALARM")
    void Active_sensors_Activated_makes_ALARM() {

        //Sensor had to have been active at some point for the alarm status
        //to be ALARM
        Mockito.when(sensor.getActive()).thenReturn(false);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        //Mockito.when(securityRepository.isAnySensorActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, true);

        //Verify we never setAlarmStatus
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    /*
    Test 6: 1. If a sensor is deactivated *while* already inactive, make no
    changes to the alarm state.
     */
    @Test
    @DisplayName("Test 6: Sensor deactivated with ALARM leaves ALARM")
    void deactivated_sensor_leaves_ALARM() {

        //Sensor had to have been active at some point for the alarm status
        //to be ALARM
        Mockito.when(sensor.getActive()).thenReturn(false);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        //Mockito.when(securityRepository.isAnySensorActive()).thenReturn(false);
        securityService.changeSensorActivationStatus(sensor, true);

        //Verify we never setAlarmStatus
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    /*
    Test 7: If the camera image contains a cat *while* the system is armed-home,
    put the system into alarm status.
     */
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("Test 7: DISARMED or ARMED_AWAY sets NO_ALARM; ARMED_HOME sets ALARM")
    @EnumSource(ArmingStatus.class)
    void camera_detects_cat_with_ARMED_HOME(ArmingStatus armingStatus) {

        //Need a BufferedImage object to send to processImage; image finds a cat
        BufferedImage img = new BufferedImage(300,225,BufferedImage.TYPE_INT_BGR);
        Mockito.when(imageService.imageContainsCat(img, 50f)).thenReturn(true);

        //Set the arming status
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        securityService.processImage(img);
        switch (armingStatus) {
            case ARMED_HOME->
                    verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
            case DISARMED, ARMED_AWAY ->
                verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    /*
    Test 8: If the camera image does not contain a cat, change the status to no alarm *as long as*
    the sensors are not active.
     */
    @Test
    @DisplayName("Test 8: No cat in image sets NO_ALARM")
    void no_cat_detected_set_NO_ALARM() {

        //Need a BufferedImage object to send to processImage; image finds a cat
        BufferedImage img = new BufferedImage(300,225,BufferedImage.TYPE_INT_BGR);
        Mockito.when(imageService.imageContainsCat(img, 50f)).thenReturn(false);

        //Check for a cat - response will be false
        securityService.processImage(img);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /*
    Test 9: If the system is disarmed, set the status to no alarm.
     */
    @Test
    @DisplayName("Test 9: Setting DISARMED sets NO_ALARM")
    void setting_DISARMED_sets_NO_ALARM(){

        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /*
    Test 10: If the system is armed, reset all sensors to inactive.
     */
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("Test 10: DISARMED - do nothing; ARMED_AWAY or ARMED_HOME-reset_all_sensors")
    @EnumSource(ArmingStatus.class)
    void system_armed_reset_sensors(ArmingStatus armingStatus) {

        securityService.setArmingStatus(armingStatus);
        switch (armingStatus){
            case DISARMED ->
                verify(securityRepository,never()).resetAllSensors(anyBoolean());
            case ARMED_AWAY, ARMED_HOME ->
                verify(securityRepository).resetAllSensors(false);
        }
    }

    /*
    Test 11: If the system is armed-home *while* the camera shows a cat, set
    the alarm status to alarm.
     */
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("Test 11: DISARMED - do nothing; ARMED_AWAY or ARMED_HOME with cat - ALARM")
    @EnumSource(ArmingStatus.class)
    void system_armed_and_cat_sets_ALARM(ArmingStatus armingStatus) {

        //Need a BufferedImage object to send to processImage; image finds a cat
        BufferedImage img = new BufferedImage(300, 225, BufferedImage.TYPE_INT_BGR);
        Mockito.when(imageService.imageContainsCat(img, 50f)).thenReturn(true);

        //Process the image to make sure currentImageHasCat=true
        //This will also set NO_ALARM in DISARMED condition
        securityService.processImage(img);

        //Now set the arming status to anything but DISARMED
        if (armingStatus != ArmingStatus.DISARMED) {
            securityService.setArmingStatus(armingStatus);
            verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

}