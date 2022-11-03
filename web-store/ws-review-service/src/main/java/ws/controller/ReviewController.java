package ws.controller;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.domain.Review;
import ws.service.ReviewService;

@RestController
@RequestMapping("review")
public class ReviewController {

	protected Logger logger = Logger.getLogger(ReviewController.class.getName());


	@Autowired
	private ReviewService service;



	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Collection<Review> getReviews(@PathVariable UUID id) {
		logger.info(String.format("Requested reviews of item with ID=%s", id.toString()));
		return service.getReviewsAsCollection(id);
	}

	@PostMapping(value="/add", produces=MediaType.APPLICATION_JSON_VALUE)
	public void addReviewToProduct(@CookieValue(name="user-id") UUID id, @RequestBody(required = false) ReviewItem item) {

			service.addReview(item.reviewId, id, item.itemId(), item.review(),item.rating());
		
	}

	public record ReviewItem(UUID reviewId, UUID itemId, String review, Integer rating) {}
}
