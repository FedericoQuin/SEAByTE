package util;

public class SoftwareVersion {
    // Class that represents "Semantic Versioning" in software systems
    private final int major;
    private final int minor;
    private final int patch;
    private final String preRelease;
    private final String build;


    public SoftwareVersion(int major, int minor, int patch) {
        this(major, minor, patch, "", "");
    }

    public SoftwareVersion(int major, int minor, int patch, String preRelease, String build) { 
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.build = build;
    }


    public static SoftwareVersion fromString(String version) {
        var s = version.split("+");
        String build = s.length == 1 ? "" : s[1];
        s = s[0].split("-");
        String preRelease = s.length == 1 ? "" : s[1];
        s = s[0].split(".");

        return new SoftwareVersion(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), preRelease, build);
    }

    
    public String toString() {
        return this.toString('.');
    }

    public String toString(char separator) {
        return String.format("%d%c%d%c%d%s%s", this.major, separator, this.minor, separator,
            this.patch, this.preRelease.isEmpty() ? "" : ("-" + this.preRelease),
            this.build.isEmpty() ? "" : ("+" + this.build));
    }
}
