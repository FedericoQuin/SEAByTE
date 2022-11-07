package domain.experiment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile {
    private String name;
    private List<LocustUser> userProfiles;


    public UserProfile(String name, String locustUserName, int amount) {
        this(name, List.of(new LocustUser(locustUserName, amount, new HashMap<>())));
    }

    public UserProfile(String name, List<LocustUser> locustUsers) {
        this.name = name;
        this.userProfiles = locustUsers;
    }


    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public int getNumberOfUsers() {
        return this.userProfiles.stream().mapToInt(LocustUser::numberOfUsers).reduce(0, Integer::sum);
    }

    public List<LocustUser> getLocustUsers() {
        return this.userProfiles;
    }

    public record LocustUser(String name, int numberOfUsers, Map<String, String> extraProperties) {}
}
