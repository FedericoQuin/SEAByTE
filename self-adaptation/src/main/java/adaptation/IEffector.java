package adaptation;

public interface IEffector {

    /**
     * Remove the history of adaptation related data from the specified AB component.
     * @param ABComponentName The name of the AB component
     */
    void clearABComponentHistory(String ABComponentName);
    
    /**
     * Set the Routin weights for the AB component.
     *  The sum of both weights have to equal 100.
     * 
     * @param ABComponentName The name of the deployed AB component.
     * @param a The weight for variant A.
     * @param b The weight for variant B.
     */
    void setABRouting(String ABComponentName, int a, int b);
    
    /**
     * Deploy a Setup in the AB application.
     * @param setupName The name of the setup to be deployed.
     * @return The port number of this host that can be used to communicate with the newly deployed AB-component.
     */
    int deploySetup(String setupName);

    /**
     * Reverse the deployment of a Setup in the AB application.
     * @param setupName The name of the setup to be undeployed.
     */
    void removeSetup(String setupName);


    void deployMLComponent(String populationSplitName);

    void removeMLComponent(String populationSplitName);
}
