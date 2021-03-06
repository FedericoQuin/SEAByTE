package adaptation;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import domain.command.Command;
import util.Networking;

public class Effector implements IEffector {

    private FeedbackLoop feedbackLoop;

    private Logger logger = Logger.getLogger(Effector.class.getName());
    

    public Effector(FeedbackLoop feedbackLoop) {
        this.feedbackLoop = feedbackLoop;
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
        setup.generateReverseCommands().forEach(Command::execute);
    }
}
