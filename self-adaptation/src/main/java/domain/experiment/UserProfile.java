package domain.experiment;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile {
    private String name;
    private Map<String, Integer> userProfiles;
    private Map<String, String> extraProperties;


    public UserProfile(String name, String locustUserName, int amount) {
        this(name, Map.of(locustUserName, amount), new HashMap<>());
    }

    public UserProfile(String name, Map<String, Integer> locustUsers, Map<String, String> extraProperties) {
        this.name = name;
        this.userProfiles = locustUsers;
        this.extraProperties = extraProperties;
    }


    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public int getNumberOfUsers() {
        return this.userProfiles.values().stream().reduce(0, Integer::sum);
    }

    public Map<String, Integer> getProfiles() {
        return this.userProfiles;
    }

    public Map<String, String> getExtraProperties() {
        return this.extraProperties;
    }
}
