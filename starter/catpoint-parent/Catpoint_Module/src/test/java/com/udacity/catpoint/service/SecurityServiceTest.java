package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    //The class we are testing
    SecurityService securityService;

    //We are mocking the SecurityRepository and the image service
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private FakeImageService imageService;
    @Mock
    private Sensor sensor;

    //Instantiate the security service
    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    /*
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
    @DisplayName("changeSensorActivationStatus sends only PENDING_ALARM")
    @EnumSource(ArmingStatus.class)
    void changeSensorActivationStatus_sends_PENDING_ALARM (ArmingStatus armingStatus) {

        if (armingStatus != ArmingStatus.DISARMED) {

            Mockito.when(sensor.getActive()).thenReturn(false);
            Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
            Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

            securityService.changeSensorActivationStatus(sensor, true);
            verify(securityRepository).setAlarmStatus(eq(AlarmStatus.PENDING_ALARM));
        }
    }

    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("changeSensorActivationStatus sets off ALARM")
    @EnumSource(ArmingStatus.class)
    void changeSensorActivationStatus_sends_ALARM (ArmingStatus armingStatus) {

        if (armingStatus != ArmingStatus.DISARMED) {

            Mockito.when(sensor.getActive()).thenReturn(false);
            Mockito.when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
            Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

            securityService.changeSensorActivationStatus(sensor, true);
            verify(securityRepository).setAlarmStatus(eq(AlarmStatus.ALARM));
        }
    }
}