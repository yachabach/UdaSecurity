package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    //Instantiate the security service
    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    //Here we are going to send all possible arming statuses to the method.
    //We will have to check for the DISARMED status as that will not return
    //any value to the repository.
    @ParameterizedTest(name = "[{index}] Arming Status: {0} ")
    @DisplayName("handleSensorActivated sends only PENDING_ALARM")
    @EnumSource(ArmingStatus.class)
    void handleSensorActivated_sends_PENDING_ALARM (ArmingStatus armingStatus) {

        if (armingStatus != ArmingStatus.DISARMED) {
            when(securityRepository.getAlarmStatus())
            verify(securityRepository.setAlarmStatus(), AlarmStatus.PENDING_ALARM)

        }

    }
}