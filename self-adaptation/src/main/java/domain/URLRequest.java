package domain;

public class URLRequest {

    // The requested url
    private String target;
    
    // The client ID from which the request originated
    private String clientId;

    // Either A or B
    // private ABInstance instance;

    // The response time of the request
    private double responseTime;
    
    
    public URLRequest(String target, String origin, double responseTime) {
        this.target = target;
        this.clientId = origin;
        this.responseTime = responseTime;
        // this.instance = instance;
    }


    public String getTarget() {
        return this.target;
    }

    public String getClientId() {
        return this.clientId;
    }

    public double getResponseTime() {
        return this.responseTime;
    }

    // public ABInstance getABInstance() {
    //     return this.instance;
    // }


    public enum ABInstance {A, B}
}
