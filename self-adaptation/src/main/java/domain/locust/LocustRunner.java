package domain.locust;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LocustRunner {

    private final static Path pathLocustFiles = Paths.get(System.getProperty("user.dir"), "Locust");

    private final static String LOCUST_COMMAND_TEMPLATE = "./venv/bin/python3 ./venv/bin/locust " +
        "--headless --users %d --spawn-rate %d -H http://localhost:8080 -f %s";

    private static int MAX_SPAWN_RATE = 10;

    private String locustUser;
    private int numberOfUsers;
    private int startingId;
    private int userIdLimitA;
    private Map<String, String> extraProperties;


    private Process locustProcess;
    
    public LocustRunner(String locustUser, int numberOfUsers, int startingId, 
            int userIdLimitA, Map<String, String> extraProperties) {
        this.locustUser = locustUser;
        this.numberOfUsers = numberOfUsers;
        this.startingId = startingId;
        this.userIdLimitA = userIdLimitA;
        this.extraProperties = extraProperties;
        this.locustProcess = null;
    }



    public boolean isRunning() {
        return this.locustProcess != null;
    }

    public void startLocust() throws IOException {
        if (this.locustProcess != null) {
            throw new RuntimeException("Cannot start UserProfile if another profile is still running. " + 
                "Make sure the 'stopLocust' method is called first.");
        }

        String command = String.format(LocustRunner.LOCUST_COMMAND_TEMPLATE, 
            this.numberOfUsers, Math.min(this.numberOfUsers, LocustRunner.MAX_SPAWN_RATE), this.locustUser);

        var pb = new ProcessBuilder(command.split(" "))
            .directory(LocustRunner.pathLocustFiles.toFile())
            .redirectError(Redirect.DISCARD)
            .redirectOutput(Redirect.DISCARD);
            // .redirectError(Redirect.INHERIT)
            // .redirectOutput(Redirect.INHERIT);
            
        var env = pb.environment();
        env.putAll(this.extraProperties);
        env.put("UserIdLimitA", Integer.toString(this.userIdLimitA));
        env.put("numberOfUsers", Integer.toString(this.numberOfUsers));
        env.put("startId", Integer.toString(this.startingId));

        this.locustProcess = pb.start();
    }


    public void stopLocust() {
        if (this.locustProcess == null) {
            // Cannot stop a process that is not running
            return;
        }

        this.locustProcess.destroyForcibly();
        this.locustProcess = null;
    }
}
