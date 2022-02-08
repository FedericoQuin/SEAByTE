package domain;

public class URLRequest {

    // The requested url
    private String target;
    
    // The source domain from which the request originated
    private String origin;

    // Either A or B
    // private ABInstance instance;

    // The response time of the request
    private double responseTime;
    
    
    public URLRequest(String target, String origin, double responseTime) {
        this.target = target;
        this.origin = origin;
        this.responseTime = responseTime;
        // this.instance = instance;
    }


    public String getTarget() {
        return this.target;
    }

    public String getOrigin() {
        return this.origin;
    }

    public double getResponseTime() {
        return this.responseTime;
    }

    // public ABInstance getABInstance() {
    //     return this.instance;
    // }


    public enum ABInstance {A, B}
}
