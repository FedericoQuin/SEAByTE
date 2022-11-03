package ws.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import ws.domain.Review;
import ws.repository.ReviewRepository;


public class ReviewService {

	private Logger logger = Logger.getLogger(ReviewService.class.getName());


	@Autowired
	private ReviewRepository repository;


	
	public void addReview(UUID reviewId, UUID userId,UUID itemId, String review,Integer rating) {
		this.repository.save(new Review(reviewId,userId,itemId,review,rating));
	}


	public Optional<Review> getReview(UUID reviewId) {
		var review = this.repository.findById(reviewId);

		return review;
		
		// return this.repository.findById(userId).orElseGet(() -> {
		// 	if (!initializeIfAbsent) {
		// 		return Optional.empty();
		// 	}
		// 	UserBasket basket = new UserBasket(userId);
		// 	this.repository.save(basket);
		// 	return basket;
		// }));
	}

	public Collection<Review> getReviewsAsCollection(UUID itemId) {
		return this.repository.findAll().stream().filter(r -> r.getItemId().equals(itemId)).collect(Collectors.toList());
	}

	public boolean hasReview(UUID userId, UUID itemId) {
		return this.getReviewsAsCollection(itemId).stream().anyMatch(r -> r.getUserId()==userId);
	}


}





