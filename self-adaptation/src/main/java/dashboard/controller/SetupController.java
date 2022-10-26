package dashboard.controller;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gson.JsonParser;

import dashboard.service.ABSetupService;
import domain.setup.Setup;
import domain.setup.Setup.NewService;

@Controller
@RequestMapping("/setup")
public class SetupController {
    
	protected Logger logger = Logger.getLogger(SetupController.class.getName());


    @Autowired
    private ABSetupService setupService;


    @GetMapping(value="")
    public String testPage() {
        logger.info("Requested AB test setup page");
        return "setup.html";
    }


    @GetMapping(value="/retrieve", produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Collection<Setup> getSetups() {
        logger.info("Requested all stored setups");
        logger.info(String.format("Amount of stored setups: %d", setupService.getAllSetups().size()));
        return setupService.getAllSetups();
    }

    @GetMapping(value="/images", produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Collection<String> getDockerImages() {
        logger.info("Requested list of all available docker images.");
        return setupService.getAvailableDockerImages();
    }


    @PostMapping(value="/newSetup")
    @ResponseStatus(value=HttpStatus.OK)
    public void startNewSetup(@RequestBody String data) {
        var root = JsonParser.parseString(data).getAsJsonObject();

        // TODO verify that the specified images are present in the docker repository?
        String name = root.get("name").getAsString();
        Setup setup = new Setup(name,
            new NewService(root.get("versionA").getAsJsonObject().get("serviceName").getAsString(),
                root.get("versionA").getAsJsonObject().get("imageName").getAsString()),
            new NewService(root.get("versionB").getAsJsonObject().get("serviceName").getAsString(),
                root.get("versionB").getAsJsonObject().get("imageName").getAsString()),
            new NewService(root.get("abComponent").getAsJsonObject().get("serviceName").getAsString(),
                root.get("abComponent").getAsJsonObject().get("imageName").getAsString()),
            root.get("decommission").getAsString()
        );

        setupService.addSetup(setup);
    }
}
