package domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import util.SoftwareVersion;

public class Constants {
    private static String DOCKER_TOPLEVEL_SERVICE_NAME = "nginx-toplevel-service";
    
    private static String NETWORK_TEMPLATE_STRING = "WS-%s_custom-network";
    private static String DOCKER_CONNECT_STRING = "docker network connect %s %s";


    public static String WS_NETWORK = "WS-1-0-0_custom-network";



    public static final int AB_COMPONENT_ADAPTATION_SERVER_PORT = 5000; 

    // How often the feedback loop is triggered (each ... seconds)
    public static final int FEEDBACK_LOOP_POLLING_FREQUENCY = 15;

    // How often the monitors retrieves URL requests from the AB component via the probe (each ... seconds)
    public static final int MONITOR_POLLING_FREQUENCY = 5;

    public static String generateDockerNetworkString(SoftwareVersion version) {
        return String.format(Constants.NETWORK_TEMPLATE_STRING, version.toString().replaceAll("\\.", "-"));
    }


    public static String generateNetworkConnectCommand(SoftwareVersion version) {
        return String.format(Constants.DOCKER_CONNECT_STRING, 
            Constants.generateDockerNetworkString(version), Constants.DOCKER_TOPLEVEL_SERVICE_NAME);
    }


    public static void main(String[] args) {
        var a = Constants.generateNetworkConnectCommand(new SoftwareVersion(1,2,3));
        System.out.println(a);
        try {
            var result = Runtime.getRuntime().exec("docker --version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            result.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
