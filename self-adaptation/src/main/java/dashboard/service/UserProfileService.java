package dashboard.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class UserProfileService {
    

    private UserProfileManifest manifest;

    public UserProfileService() {

        // Read all the available user profiles listed in the locust manifest file
        Path path = Paths.get(System.getProperty("user.dir"), "Locust", "UserManifest.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            this.manifest = mapper.readValue(path.toFile(), UserProfileManifest.class);
            // System.out.println(manifest.users.size());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not open the user profile manifest file correctly: %s", 
                e.toString()));
        }
    }

    
    public Collection<LocustProfileInfo> getLocustProfilesInformation() {
        return manifest.users(); //.stream().map(UserProfile::name).toList()
        // return List.of("testing", "anotherprofile");
    }


    public record UserProfileManifest(List<LocustProfileInfo> users) {}
    public record LocustProfileInfo(String name, String description, List<String> requiredVariables) {}
}
