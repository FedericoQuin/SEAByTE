package domain.experiment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfile {
    private String name;
    private List<LocustUser> userProfiles;

    private ABRoutingMode abRoutingMode;


    public UserProfile(String name, String locustUserName, int amount) {
        this(name, List.of(new LocustUser(locustUserName, amount, new HashMap<>())), ABRoutingMode.getDefaultRoutingMode());
    }

    public UserProfile(String name, List<LocustUser> locustUsers) {
        this(name, locustUsers, ABRoutingMode.getDefaultRoutingMode());
    }

    public UserProfile(String name, List<LocustUser> locustUsers, ABRoutingMode mode) {
        this.name = name;
        this.userProfiles = locustUsers;
        this.abRoutingMode = mode;
    }


    public UserProfile(UserProfile other, ABRoutingMode mode) {
        this.name = other.name;
        this.userProfiles = other.userProfiles;
        this.abRoutingMode = mode;
    }


    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public ABRoutingMode getABRoutingMode() {
        return this.abRoutingMode;
    }

    @JsonIgnore
    public int getNumberOfUsers() {
        return this.userProfiles.stream().mapToInt(LocustUser::numberOfUsers).reduce(0, Integer::sum);
    }

    public List<LocustUser> getLocustUsers() {
        return this.userProfiles;
    }

    public record LocustUser(String name, int numberOfUsers, Map<String, String> extraProperties) {}

    public enum ABRoutingMode {
        Classic,
        PredeterminedById,
        Split;

        public static ABRoutingMode getRoutingMode(String name) {
            return Arrays.stream(ABRoutingMode.values())
                .filter(x -> x.name().toLowerCase().equals(name))
                .findFirst().orElse(ABRoutingMode.Classic);
        }

        public static ABRoutingMode getDefaultRoutingMode() {
            return ABRoutingMode.PredeterminedById;
        }
    }
}
