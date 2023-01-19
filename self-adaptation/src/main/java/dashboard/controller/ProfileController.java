package dashboard.controller;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dashboard.model.ABRepository;
import dashboard.service.UserProfileService;
import dashboard.service.UserProfileService.LocustProfileInfo;
import domain.experiment.UserProfile;
import domain.experiment.UserProfile.ABRoutingMode;
import domain.experiment.UserProfile.LocustUser;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private Logger logger = Logger.getLogger(ProfileController.class.getName());

    @Autowired
    private ABRepository repository;

    @Autowired
    private UserProfileService userProfileService;



    @RequestMapping("")
    public String index() {
        return "profile.html";
    }



    @PostMapping(value="/newProfile")
    @ResponseStatus(value=HttpStatus.OK)
    public void addNewExperiment(@RequestBody String data) {
        logger.info("Adding new user profile.");

        var root = JsonParser.parseString(data).getAsJsonObject();

        // var sharedVars = root.get("sharedVars").getAsJsonObject().entrySet().stream()
        //     .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));

        UserProfile profile = new UserProfile(
            root.get("name").getAsString(),
            StreamSupport.stream(root.get("locustUsers").getAsJsonArray().spliterator(), true)
                .map(JsonElement::getAsJsonObject)
                .map(o -> new LocustUser(o.get("locustUser").getAsString(), 
                        o.get("numberOfUsers").getAsInt(),
                        StreamSupport.stream(o.get("environmentVars").getAsJsonArray().spliterator(), true)
                            .map(JsonElement::getAsJsonObject)
                            .collect(Collectors.toMap(p -> p.get("variableName").getAsString(), 
                                p -> p.get("variableValue").getAsString()))
                ))
                .toList(),
            root.get("abRoutingMode").getAsString().isEmpty() ? 
                ABRoutingMode.getDefaultRoutingMode() : 
                ABRoutingMode.getRoutingMode(root.get("abRoutingMode").getAsString())
        );

        repository.addUserProfile(profile);
    }

    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<UserProfile> getExperiments() {
        return repository.getAllUserProfiles();
    }


    @GetMapping(value="/locustProfiles")
    public @ResponseBody Collection<LocustProfileInfo> getLocustProfilesInformation() {
        return userProfileService.getLocustProfilesInformation();
    }

    // record LocustUser(String locustUser, int numberOfUsers, Map<String, String> environmentVariables) {}
}
