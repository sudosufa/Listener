package app.decoder.devices;

import app.dao.TrackingDAO;
import app.decoder.basic.DecoderDevice;
import app.model.DeviceTracking;
import app.tools.ByteWrapper;
import app.tools.CodecStore;
import app.tools.Console;
import app.tools.avldata.AvlData;
import app.tools.vondors.CodecException;
import app.tools.vondors.IOElement;
import config.ListDevices;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TeltonikaDecoder extends DecoderDevice {
    @Override
    public String getImei() throws IOException {
        return dataInputStream.readUTF();
    }

    @Override
    public void handshake() throws IOException {
        dataOutputStream.writeBoolean(true);
    }

    @Override
    public List<DeviceTracking> decode() throws IOException, CodecException {
        List<DeviceTracking> deviceTrackings = new ArrayList<>();
        byte[] packet = ByteWrapper.unwrapFromStream(dataInputStream);
        if (packet == null) {
            Console.println("Err : packet data is  null");
            return null;
        }
        AvlData decoder = CodecStore.getSuitableCodec(packet);
        if (decoder == null) {
            Console.println("Err : SuitableCodec not fond");
            return null;
        }
        AvlData[] avlDatas = decoder.decode(packet);
        for (AvlData avlData : avlDatas) {
            int dis_priority = avlData.getPriority();
            double dis_longitude = ((double) avlData.getGpsElement().getLongitude()) / 10000000;
            double dis_latitude = ((double) avlData.getGpsElement().getLatitude()) / 10000000;
            double dis_speed = avlData.getGpsElement().getSpeed();
            double dis_angle = avlData.getGpsElement().getAngle();
            double dis_altitude = avlData.getGpsElement().getAltitude();
            Integer dis_satellites = (int) (avlData.getGpsElement().getSatellites());
            String dis_tracking_time = new Timestamp(avlData.getTimestamp()).toString();
            DeviceTracking tracking = newTracking();
            tracking.setTracking_time(dis_tracking_time);
            tracking.setBasicData(device.getImei(), device.getId(), device.getId_vehicule(), dis_priority);
            tracking.setGPSTrackingData(dis_longitude, dis_latitude, dis_speed, dis_angle, dis_altitude, dis_satellites);
            TrackingDAO pp = new TrackingDAO();
            Console.println("................................", Console.Colors.getGREEN());
            Console.println(String.valueOf(device.getId_vehicule()), Console.Colors.getGREEN());
            Console.println(String.valueOf(device.getId()), Console.Colors.getGREEN());
            Console.println("................................", Console.Colors.getGREEN());
            Console.println(String.valueOf(device.getId()), Console.Colors.getGREEN());
            fillData(tracking, avlData.getInputOutputElement(), ""+pp.getParamsVehicule(device.getId()).get(0),""+pp.getParamsVehicule(device.getId()).get(1),""+pp.getParamsVehicule(device.getId()).get(2),""+pp.getParamsVehicule(device.getId()).get(3),""+pp.getParamsVehicule(device.getId()).get(4));
            deviceTrackings.add(tracking);
        }
        return deviceTrackings;
    }

    @Override
    public void feedBackOk(int nbrTracking) throws IOException {
        dataOutputStream.writeInt(nbrTracking);
    }

    private void fillData(DeviceTracking tracking, IOElement ioElement,String params1,String volMaxReservoir1,String volMaxReservoir2,String params2,String params3) {

        Console.println(""+ioElement, Console.Colors.getGREEN());

        double vol_tank_1=0;
        double fuel_level=0;
        double ai1=0;
        double ai2=0;
        double LV_fuel_consumed= 0;
        double odoMetreValue=0;
        double totalMileageValue=0;


        for (int key : ioElement.getAvailableProperties()) {

            double value = ioElement.getValue(key);
            tracking.setAvlData(key, value);

            if(key == 84){
                fuel_level = value;



            } else if (key == 89 ){
                vol_tank_1 = value;
            }
            else if (key == 9 ){
                ai1 = value;
            }
            else if (key == 10 ){
                ai2 = value;
            }
            else if (key == 83 ){
                LV_fuel_consumed = value;
            }
            else if(key == 199){
                odoMetreValue=value;
            } else if (key == 87){
                totalMileageValue = value;
            }



            if(params1.equals("can_x1_litre"))
            {
                Console.println(params1, Console.Colors.getGREEN());
                Console.println(Long.toString((long) fuel_level), Console.Colors.getGREEN());
                tracking.setReservoir1(fuel_level*0.1);
            }
            else if(new String(params1).equals("can_x1_%"))
            {
                tracking.setReservoir1(vol_tank_1*Double.parseDouble(volMaxReservoir1));
            }
            else if(new String(params1).equals("can_litre+sonde_litre"))
            {
                tracking.setReservoir1(fuel_level*0.1);
                tracking.setReservoir2(Double.parseDouble("INKO1"));
            }
            else if(new String(params1).equals("can_litre+sonde_volt"))
            {
                tracking.setReservoir1(fuel_level*0.1);
                float reservoir = (float) (((ai1 - device.volReservoir2Min) / (device.volReservoir2Max - device.volReservoir2Min)) * 100);
                tracking.setReservoir2(reservoir*Double.parseDouble(volMaxReservoir2));
            }
            else if(new String(params1).equals("can_%+sonde_litre"))
            {
                tracking.setReservoir1(vol_tank_1*Double.parseDouble(volMaxReservoir1));
                tracking.setReservoir2(Double.parseDouble("INKO1"));

            }
            else if(new String(params1).equals("can_%+sonde_volt"))
            {
                tracking.setReservoir1(vol_tank_1*Double.parseDouble(volMaxReservoir1));
                float reservoir = (float) (((ai1 - device.volReservoir2Min) / (device.volReservoir2Max - device.volReservoir2Min)) * 100);
                tracking.setReservoir2(reservoir*Double.parseDouble(volMaxReservoir2));

            }
            else if(new String(params1).equals("sonde_x1_litre"))
            {
                tracking.setReservoir1(Double.parseDouble("INKO1"));


            }
            else if(new String(params1).equals("sonde_x1_volt"))
            {
                float reservoir = (float) (((ai1 - device.volReservoir1Min) / (device.volReservoir1Max - device.volReservoir1Min)) * 100);
                tracking.setReservoir1(reservoir*Double.parseDouble(volMaxReservoir1));


            }
            else if(new String(params1).equals("sonde_x2_litre"))
            {
                tracking.setReservoir1(Double.parseDouble("INKO1"));
                tracking.setReservoir2(Double.parseDouble("INKO2"));


            }
            else if(new String(params1).equals("sonde_x2_volt"))
            {
                float reservoir = (float) (((ai1 - device.volReservoir1Min) / (device.volReservoir1Max - device.volReservoir1Min)) * 100);
                tracking.setReservoir1(reservoir*Double.parseDouble(volMaxReservoir1));
                float reservoir2 = (float) (((ai2 - device.volReservoir2Min) / (device.volReservoir2Max - device.volReservoir2Min)) * 100);
                tracking.setReservoir2(reservoir2*Double.parseDouble(volMaxReservoir2));


            }

            if (new String(params2).equals("null"))
            {
                //  Console.println("insert nothing", Console.Colors.getGREEN());
            }
            else if(new String(params2).equals("cumul"))

            {
                tracking.setLVCAN_FUEL_CONSUMED(LV_fuel_consumed);
            }


            if (new String(params3).equals("null"))
            {
                //  Console.println("insert nothing", Console.Colors.getGREEN());
            }
            else if(new String(params3).equals("entre_2tracking"))
            {
                tracking.setKm(odoMetreValue);
            }else if(new String(params3).equals("entre_2tracking+cumul"))
            {
                tracking.LVCAN_TOTAL_MILEAGE = totalMileageValue;


            }



            switch (key) {



                case Keys.LVCAN_FUEL_RATE:
                case Keys.LVCAN_ENGINE_TEMPERATURE:
                case Keys.TEMPERATURE:
                case Keys.TEMPERATURE2:
                    value = value / 10;
                    break;
                case Keys.EXTERNAL_POWER_VOLTAGE:
                case Keys.INTERNAL_BATTERY_VOLTAGE:
                    value = value / 1000.0;
                    break;
            }


            switch (key) {
                case Keys.ACC:
                    tracking.setAcc_status((int) value);
                    break;
                case Keys.ACTUAL_PROFILE:
                    tracking.setActual_profile((int) value);
                    break;
                case Keys.AREA_CODE:
                    tracking.setArea_code((int) value);
                    break;
                case Keys.CURRENT_OPERATOR_CODE:

                    tracking.setOperator_code((int) value);
                    break;
                case Keys.EXTERNAL_POWER_VOLTAGE:
                    tracking.setExternal_power(value);
                    tracking.setCurrent_battery(value);
                    break;
                case Keys.GNSS_STATUS:
                    tracking.setGnss_status((int) value);
                    break;
                case Keys.GPS_HDOP:
                    tracking.setGps_hdop(value);
                    break;
                case Keys.GSM_LEVEL:
                    tracking.setEtatSignal((int) value);
                    break;
                case Keys.INTERNAL_BATTERY_VOLTAGE:
                    tracking.setInternal_battery(value);
                    break;
                case Keys.MOUVEMENT:
                    tracking.setMovement((int) value);
                    break;
                case Keys.ODOMETER:
                    tracking.HavOdometer=true;

                    break;
                case Keys.PCB_TEMPERATURE:
                    tracking.setPcb_temp(value);
                    break;
                case Keys.RFID:
                    tracking.setRfid(String.valueOf(value));
                    break;
                case Keys.SLEEP_MODE:
                    tracking.setSleep_mode((int) value);
                    break;
                case Keys.LVCAN_VEHICLE_SPEED:
                    tracking.LVCAN_VEHICLE_SPEED = value;
                    break;
                case Keys.LVCAN_ACCELERATOR_PEDAL_POSITION:
                    tracking.LVCAN_ACCELERATOR_PEDAL_POSITION = value;
                    break;
                case Keys.LVCAN_ENGINE_RPM:
                    tracking.LVCAN_ENGINE_RPM = (int) value;
                    break;
                case Keys.LVCAN_DOOR_STATUS:
                    tracking.LVCAN_DOOR_STATUS = (int) value;
                    break;
                case Keys.LVCAN_GREEN_DRIVING_TYPE:
                    tracking.GREEN_DRIVING_TYPE = value;
                    break;
                case Keys.CRASH_DETECTION:
                    tracking.CRASH_DETECTION = value;
                    break;
                case Keys.GREEN_DRIVING_VALUE:
                    tracking.GREEN_DRIVING_VALUE = value;
                    break;
                case Keys.LVCAN_JAMMING:
                    tracking.LVCAN_JAMMING = value;
                    break;
                case Keys.GREEN_DRIVING_EVENT_DURATION:
                    tracking.GREEN_DRIVING_EVENT_DURATION = value;
                    break;
                case Keys.LVCAN_ENGINE_TEMPERATURE:
                    if (value > 0)
                        tracking.LVCAN_ENGINE_TEMPERATURE = value;
                    break;
                case Keys.TEMPERATURE:
                    if (value / 10 < 300)
                        tracking.setTemperature(value);
                    break;
                case Keys.TEMPERATURE2:
                    if (value / 10 < 300)
                        tracking.setTemperature2(value);
                    break;


                case Keys.ENGINE_OIL_LEVEL:
                    tracking.ENGINE_OIL_LEVEL = value;
                    break;
                case Keys.LVCAN_FUEL_RATE:
                    tracking.LVCAN_FUEL_RATE = value;
                    break;


            }








        }








        if (ioElement.getProperty(Keys.ODOMETER) == null) {//todo who(port) need  this check
            tracking.setMessage("ODOMETER not activated");
        }
    }

    @Override
    protected void closeIOStream() throws IOException {
        if (dataOutputStream != null) {
            dataOutputStream.writeInt(0);
            dataOutputStream.close();
        }
        if (dataInputStream != null) {
            dataInputStream.close();
        }
        inputStream.close();
        outputStream.close();
    }

    private static final class Keys {
        private static final int
                ACC = 1,
                MOUVEMENT = 240,
                GSM_LEVEL = 21,
        //                GPS_PDOP = 181,
        GPS_HDOP = 182,
        //                CELL_ID = 205,
        AREA_CODE = 206,
                ODOMETER = 199,
                CURRENT_OPERATOR_CODE = 241,
                RFID = 207,
                ACTUAL_PROFILE = 22,
                EXTERNAL_POWER_VOLTAGE = 66,
                INTERNAL_BATTERY_VOLTAGE = 67,
        //                INTERNAL_BATTERY_CURRENT = 68,
        PCB_TEMPERATURE = 70,
                SLEEP_MODE = 200,
                GNSS_STATUS = 71,
        //                SPEEDOMETER = 24,
        AI_1 = 9,//Reservoir 1
                AI_2 = 10,//Reservoir 2
        //                AI_3 = 11,//Temperature 1
//                AI_4 = 19,//Temperature 2
        TEMPERATURE = 72,
                TEMPERATURE2 = 73,
                LVCAN_VEHICLE_SPEED = 81,
                LVCAN_ACCELERATOR_PEDAL_POSITION = 82,
                LVCAN_FUEL_CONSUMED = 83,
                LVCAN_FUEL_LEVEL = 84,
                LVCAN_ENGINE_RPM = 85,
                LVCAN_TOTAL_MILEAGE = 87,
                VOL_TANK_1 = 89,
                LVCAN_DOOR_STATUS = 90,
                LVCAN_FUEL_RATE = 110,
                LVCAN_ENGINE_TEMPERATURE = 115,
                ENGINE_OIL_LEVEL = 235,
                LVCAN_GREEN_DRIVING_TYPE = 253,
                CRASH_DETECTION = 247,
                GREEN_DRIVING_VALUE = 254,
                LVCAN_JAMMING = 249,
                GREEN_DRIVING_EVENT_DURATION = 243;
//              LVCAN_ENGINE_WORKTIME = 102,
//              LVCAN_ENGINE_WORKTIME_COUNTED = 103,
    }
}

