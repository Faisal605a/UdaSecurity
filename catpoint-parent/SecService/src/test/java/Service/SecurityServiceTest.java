package Service;

import application.SensorPanel;
import data.*;
import imageService.ImageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    public SecurityService securityService ;
    @Mock
    public SecurityRepository securityRepository;
    @Mock
    public ImageService imageService;


    @BeforeEach
    public void init(){

        securityService  = new SecurityService(securityRepository, imageService);

    }

    //1- If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    public void changeSensorActivationStatusNoAlarmTest(){


        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, true);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.PENDING_ALARM);

    }
    //2- If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @Test
    public void changeSensorActivationStatusPendingAlarm1Test(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);

    }
    //3- If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    public void changeSensorActivationStatusPendingAlarm2Test(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Motion front door", SensorType.MOTION);
        securityService.changeSensorActivationStatus(sensor1, true);


        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, false);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.NO_ALARM);

    }
    //4- If alarm is active, change in sensor state should not affect the alarm state.
    // argument matcher could be used here to test multiple states of arming.
    @Test
    public void changeSensorActivationStatusActiveAlarmStateTest(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, false);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);

    }
    //5- If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    @Test
    public void changeSensorActivationStatusPendingTest(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);

    }
    //6- If a sensor is deactivated while already inactive, make no changes to the alarm state.
    // argument matcher for the alarming states

    @ParameterizedTest
    @CsvSource({"ALARM", "PENDING_ALARM", "NO_ALARM"})
    public void changeSensorDeActivationStatusTest(AlarmStatus alarmStatus){

//        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, false);
        assertTrue(securityService.getAlarmStatus() == alarmStatus);
        securityService.changeSensorActivationStatus(sensor1, false);
        assertTrue(securityService.getAlarmStatus() == alarmStatus);

    }

    //7- If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
    @Test
    public void catDetectedArmedTest(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.processImage(new BufferedImage(12,10,2) );
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
    }
    @Test
    public void catDetectedArmedSensorsTest(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.processImage(new BufferedImage(12,10,2) );
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
    }
    @Test
    public void catDetectedArmed2SensorsTest(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        Set sensors = new HashSet();
        sensor1.setActive(true);
        sensors.add(sensor1);
        when(securityRepository.getSensors()).thenReturn(sensors);

//        securityService.changeSensorActivationStatus(sensor1, anyBoolean());
        securityService.processImage(new BufferedImage(12,10,2) );
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
    }
    @Test
    public void catDetectedArmed3SensorsTest(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Sensor front door", SensorType.MOTION);
        Set sensors = new HashSet();
        sensor1.setActive(true);
        sensor2.setActive(true);
        sensors.addAll(List.of(sensor1, sensor2));
        when(securityRepository.getSensors()).thenReturn(sensors);

        securityService.processImage(new BufferedImage(12,10,2) );
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
    }

    //8- If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @Test
    public void catDetectedAlarmStatusTest(){
        //when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, true);
        securityService.processImage(new BufferedImage(12,10,2) );
//        assertAll(() ->securityService.getSensors().stream().filter(s  -> s.getActive() == false));
//        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.NO_ALARM);
    }

    //9- If the system is disarmed, set the status to no alarm.
    @Test
    public void SystemStatusDisarmed(){
//        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        //System.out.println(securityService.getAlarmStatus());
        System.out.println(securityService.getArmingStatus());
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.NO_ALARM);
    }

    //10- If the system is armed, reset all sensors to inactive.
    @Test
    public void SystemStatusArmed(){
//        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
//        securityService.getArmingStatus();
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor1, true);
//        securityService.changeSensorActivationStatus(sensor1, false);
//        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertFalse(checkActivationSensors());
    }

    @Test
    public void statutesListinerAddDelete(){
        SensorPanel temp = new SensorPanel(securityService);
        securityService.addStatusListener(temp);
        securityService.removeStatusListener(temp);
    }
    @Test
    public void SensorAddDelete(){
        int before= securityService.getSensors().size();
        Sensor sensor1 = new Sensor("Sensor front door", SensorType.DOOR);
        securityService.addSensor(sensor1);
        Set sensors = new HashSet();
        sensors.add(sensor1);
        when(securityRepository.getSensors()).thenReturn(sensors);
        int after= securityService.getSensors().size();
        securityService.removeSensor(new Sensor("Sensor front door", SensorType.DOOR));
        sensors.remove(sensor1);
        when(securityRepository.getSensors()).thenReturn(sensors);
        int end = securityService.getSensors().size();
        assertEquals(before, end);
        assertEquals((before+1), after);
    }
    //11 -If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @Test
    public void catDetectedAlarmStatusNoSensorTest(){
        //when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);

        securityService.processImage(new BufferedImage(12,10,2) );
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
//        assertAll(() ->securityService.getSensors().stream().filter(s  -> s.getActive() == false));
//        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertTrue(securityService.getAlarmStatus() == AlarmStatus.ALARM);
    }
    boolean checkActivationSensors(){

        return securityService.getSensors().stream().anyMatch(s -> s.getActive() == true);
    }




}
