package app;

import app.dao.DeviceDAO;
import app.dao.TrackingDAO;
import app.tools.*;
import app.tools.avldata.AvlData;
import app.tools.avldata.AvlDataFM4;
import app.tools.avldata.AvlDataGH;
import app.tools.trackingDebugger.TemperatureCorrespondence;
import config.Env;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
public class Main {
    public static void main(String[] args) {
        boolean activateLogFile = false;
        try {
            for (String arg : args) {
                if ("-d".equals(arg)) {
                    activateLogFile = true;
                }
            }
        } catch (Exception e) {
            Console.printStackTrace(e);
        }


        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);


        String topic = "myto";
        String key = "my-key";
        String value = "Data From listener !!!!!!!!!!!!!!!!";

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
        // Console.initLogger(activateLogFile);
        //   Env.printConfig();
        //  Console.println("####################################################################", Console.Colors.getGREEN());
        //  Console.println("####################################################################", Console.Colors.getGREEN());
        //  Console.printlnWithDate("starting Tracking Listener " + Env.app_version + "  ...");
        initDAO();
        ConnexionDB.start();
        registerCodecStoreInstances();
        TemperatureCorrespondence.init();

        MainListener.startAllListener();
        Console.printlnWithDate("The tracking listener has been fully activated", Console.Colors.getGREEN());
        Console.println("####################################################################", Console.Colors.getGREEN());
        Console.println("####################################################################", Console.Colors.getGREEN());
        Console.println("####################################################################", Console.Colors.getGREEN());
    }


    private static void initDAO() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        Tools.trackingDAO = context.getBean(TrackingDAO.class);
        Tools.deviceDAO = context.getBean(DeviceDAO.class);
    }

    private static void registerCodecStoreInstances() {
        CodecStore.register(AvlData.getCodec());
        CodecStore.register(AvlDataFM4.getCodec());
        CodecStore.register(AvlDataGH.getCodec());
    }
}

