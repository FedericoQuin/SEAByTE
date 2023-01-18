package controller;

import java.util.logging.Logger;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/feedback")
public class FeedbackController {
	protected Logger logger = Logger.getLogger(FeedbackController.class.getName());


	@PostMapping(value="/{rating}")
	public void leaveFeedback(@PathVariable double rating) {
		// ignore the ratings here, tracked at the AB component already
		logger.info(String.format("Retrieved the following rating: %.2f", rating));
	}

}
