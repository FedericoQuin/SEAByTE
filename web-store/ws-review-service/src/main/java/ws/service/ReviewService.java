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


	
	public void addReview(UUID userId,UUID itemId, String review,Integer rating) {
		UUID reviewId = UUID.randomUUID();
		this.repository.save(new Review(reviewId,userId,itemId,review,rating));
	}


	public Optional<Review> getReview(UUID reviewId) {
		var review = this.repository.findById(reviewId);

		return review;

	}

	public Collection<Review> getReviewsAsCollectionForItem(UUID itemId) {
		return this.repository.findAll().stream().filter(r -> r.getItemId().equals(itemId)).collect(Collectors.toList());
	}

	public Collection<Review> getReviewsAsCollectionForUser(UUID userId) {
		return this.repository.findAll().stream().filter(r -> r.getUserId().equals(userId)).collect(Collectors.toList());
	}

	public boolean hasReview(UUID userId, UUID itemId) {
		return this.getReviewsAsCollectionForItem(itemId).stream().anyMatch(r -> r.getUserId()==userId);
	}

	public void changeReview(UUID reviewId, String reviewText, Integer rating){
		var review = this.repository.findById(reviewId).get();
		review.updateReview(reviewText,rating);
		this.repository.save(review);
	}


}





