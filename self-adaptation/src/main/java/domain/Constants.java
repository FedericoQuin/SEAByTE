package domain;

public class Constants {
    public static String STACK_PREFIX = "WS";
    public static String STACK_PREFIX_SEPARATOR = "_";
    public static String WS_NETWORK = Constants.STACK_PREFIX + Constants.STACK_PREFIX_SEPARATOR + "custom-network";



    public static final int AB_COMPONENT_ADAPTATION_SERVER_PORT = 5000; 

    // How often the feedback loop is triggered (each ... seconds)
    public static final int FEEDBACK_LOOP_POLLING_FREQUENCY = 15;

    // How often the monitors retrieves URL requests from the AB component via the probe (each ... seconds)
    public static final int MONITOR_POLLING_FREQUENCY = 5;



    public static String generateStackName(String serviceName) {
        return String.format("%s%s%s", Constants.STACK_PREFIX, Constants.STACK_PREFIX_SEPARATOR, serviceName);
    }
}
