package dashboard;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import adaptation.FeedbackLoop;
import dashboard.model.ABRepository;
import dashboard.service.ABExperimentService;
import dashboard.service.ABRunService;
import dashboard.service.ABSetupService;
import dashboard.service.AdaptationService;

@SpringBootApplication()
public class Dashboard {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "dashboard-config");

		SpringApplication.run(Dashboard.class, args);
		Logger.getLogger(Dashboard.class.getName()).setLevel(Level.ALL);;
	}



	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	
    @Bean
    public ABSetupService abSetupService() {
        return new ABSetupService();
    }

    @Bean
    public ABExperimentService abExperimentService() {
        return new ABExperimentService();
    }
	
	@Bean
	public ABRunService abRunService() {
		return new ABRunService();
	}

	@Bean
	public AdaptationService AdaptationService() {
		return new AdaptationService();
	}



	@Bean
	public ABRepository abRepository() {
		return new ABRepository();
	}


	@Bean
	public FeedbackLoop feedbackLoop() {
		return new FeedbackLoop();
	}

	
}
