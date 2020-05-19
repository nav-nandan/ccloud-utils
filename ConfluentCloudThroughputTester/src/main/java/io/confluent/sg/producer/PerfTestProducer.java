package io.confluent.sg.producer;

import java.io.FileReader;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Node;

import com.google.gson.Gson;

public class PerfTestProducer extends Thread {
	
	private Properties props;
	private String topic;
	private Producer<String, Object> producer;
	
	private String filename;
	private Object record;
	
	private static AtomicLong messageCount = new AtomicLong(0);
	private static Long startTime = new Date().getTime();
	private static Long endTime = new Date().getTime();
	
	public PerfTestProducer(final String producerTopic, final String sampleData) {
		try {
			props = new Properties();
			
			props.setProperty("ssl.endpoint.identification.algorithm", "https");
			props.setProperty("sasl.mechanism", "PLAIN");
			props.setProperty("request.timeout.ms", "20000");
			props.setProperty("bootstrap.servers", "<confluent_cloud_cluster_bootstrap_server>");
			props.setProperty("retry.backoff.ms", "500");
			props.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"<API KEY>\" password=\"<API SECRET\";");
			props.setProperty("security.protocol", "SASL_SSL");
			
			props.put(ProducerConfig.ACKS_CONFIG, "all");
			props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
			props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaJsonSerializer");
			
			topic = producerTopic;
			
			producer = new KafkaProducer<String, Object>(props);
			
			filename = sampleData;
			
			Gson gson = new Gson();
			record = gson.fromJson(new FileReader(filename), Object.class);
			
			AdminClient adminclient = KafkaAdminClient.create(props);
			ListTopicsResult topics = adminclient.listTopics();
		    Set<String> names = topics.names().get();
		    System.out.println("-->cluster topics<--");
		    for(String name: names) {
		    	System.out.println(name);
		    }
		    
		    Collection<Node> nodes = adminclient.describeCluster().nodes().get();
		    System.out.println("Describe cluster nodes: " + nodes.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		// Produce sample data
	    final Long numMessages = 1000L;
	    
	    String baseString = "ABC";
    	StringBuilder sb = new StringBuilder(3);
    	for (int i = 0; i < 3; i++) { 
            int index = (int)(baseString.length() * Math.random());
            sb.append(baseString.charAt(index));
        }
    	
    	String key = sb.toString();
    	
	    for (Long i = 0L; i < numMessages; i++) {
//	    	System.out.printf("Producing record: %s\t%s%n", key, record);
	    	
	    	producer.send(new ProducerRecord<String, Object>(topic, key, record), new Callback() {
	    		@Override
	    		public void onCompletion(RecordMetadata m, Exception e) {
	    			if (e != null) {
	    				e.printStackTrace();
	    			} else {
	    				messageCount.incrementAndGet();
//	    				System.out.printf("Produced %d records to topic %s partition [%d] @ offset %d%n", messageCount.get(), m.topic(), m.partition(), m.offset());
	    			}
	    		}
	    	});
	    }
	    
	    producer.flush();
	    
	    producer.close();
	    
	    endTime = new Date().getTime();
	    
	    System.out.printf("%d messages were produced to topic %s%n", messageCount.get(), topic);
	    System.out.printf("Start time: %d%n", startTime);
	    System.out.printf("End time: %d%n", endTime);
	    System.out.printf("Total time (in ms): %d%n", (endTime - startTime));
	    System.out.printf("Writes (msgs/second): %d%n", Math.round((float) messageCount.get() / ((float)(endTime - startTime) / 1000)));
	    System.out.printf("%n%n%n");
	}
}
