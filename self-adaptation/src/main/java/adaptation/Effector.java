package adaptation;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import domain.command.Command;
import domain.command.RetrieveRunningServices;
import domain.setup.Setup.NewService;
import util.Networking;

public class Effector implements IEffector {

    private FeedbackLoop feedbackLoop;
    private NewService removedService;
    
    private Logger logger = Logger.getLogger(Effector.class.getName());
    

    public Effector(FeedbackLoop feedbackLoop) {
        this.feedbackLoop = feedbackLoop;
        this.removedService = null;
    }



    @Override
    public void clearABComponentHistory(String ABComponentName) {
        int networkPort = this.feedbackLoop.getKnowledge().getABComponentPort(ABComponentName);

        String url = String.format("http://localhost:%d/adaptation/reset", networkPort);
        try {
            var connection = new URL(url).openConnection();
            connection.connect();
            connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setABRouting(String ABComponentName, int a, int b) {
        int networkPort = this.feedbackLoop.getKnowledge().getABComponentPort(ABComponentName);
        int userLimit = this.feedbackLoop.getKnowledge().getUserProfile().getNumberOfUsers();

        logger.info(String.format("Adjusting weights for the AB component: A=%d B=%d LIMIT=%d", a, b, userLimit));

        String url = String.format(
            "http://localhost:%d/adaptation/change?A=%d&B=%d&userLimit=%d",
            networkPort, a, b, userLimit
        );
        
        try {
            var connection = new URL(url).openConnection();
            connection.connect();
            connection.getInputStream();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int deploySetup(String setupName) {
        var setup = this.feedbackLoop.getKnowledge().getSetup().get();
        int exposedPort = Networking.generateAvailableNetworkPort();
        var commands = setup.generateCommands(exposedPort);

        String services = new RetrieveRunningServices().execute().get();

        // Look for the service that corresponds to the service that will be removed
        for (String service : services.split("\n")) {
            if (!service.contains("\t")) {
                continue;
            }

            String name = service.split("\t")[0].replace("WS_", "");
            String image = service.split("\t")[1];

            if (name.equals(setup.getRemoveService())) {
                logger.info(String.format("Found a match of service that will be removed: %s | %s", name, image));
                this.removedService = new NewService(setup.getRemoveService(), image);
                break;
            }
        }


        logger.info(String.format("Executing %d commands...", commands.size()));
        
        try {
            commands.forEach(Command::execute);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        logger.info(String.format("Running of setup %s complete.", setup.getName()));

        return exposedPort;
    }


    @Override
    public void removeSetup(String setupName) {
        var setup = this.feedbackLoop.getKnowledge().getSetup().get();

        logger.info("Attempting to restart stopped service...");
        var commands = this.removedService == null ? setup.generateReverseCommands() : setup.generateReverseCommands(this.removedService);
        commands.forEach(Command::execute);

        this.removedService = null;
    }
}
