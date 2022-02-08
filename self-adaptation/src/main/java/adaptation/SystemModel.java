package adaptation;

import java.time.Instant;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import util.SoftwareVersion;

public class SystemModel {

    private SortedMap<Date, SystemState> history;
    private SystemState currentState;
    private final SoftwareVersion version;



    public SystemModel(SystemState state, SoftwareVersion version) {
        this.history = new TreeMap<>();
        this.currentState = state;
        this.version = version;
    }


    public void advanceAdaptationCycle() { 
        history.put(Date.from(Instant.now()), this.currentState);
    }

    public void setCurrentSystemState(SystemState state) {
        this.currentState = state;
    }


    public SoftwareVersion getVersion() {
        return this.version;
    }


}
