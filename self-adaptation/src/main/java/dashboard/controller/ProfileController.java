package dashboard.controller;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import dashboard.model.ABRepository;
import domain.experiment.UserProfile;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private Logger logger = Logger.getLogger(ProfileController.class.getName());

    @Autowired
    private ABRepository repository;



    @RequestMapping("")
    public String index() {
        return "profile.html";
    }



    @PostMapping(value="/newProfile")
    @ResponseStatus(value=HttpStatus.OK)
    public void addNewExperiment(@RequestBody String data) {
        logger.info("Adding new user profile.");

        var root = JsonParser.parseString(data).getAsJsonObject();

        UserProfile profile = new UserProfile(
            root.get("name").getAsString(),
            root.get("locustUsers").getAsJsonObject().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsInt())),
            root.get("extraVars").getAsJsonObject().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()))
        );

        repository.addUserProfile(profile);
    }

    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<UserProfile> getExperiments() {
        return repository.getAllUserProfiles();
    } 
    
}
