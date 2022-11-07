package ws.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="review")
public class Review {

	@Id
	private UUID reviewId;

	@Field
	private UUID userId;

	@Field
	private UUID itemId;

	@Field
	private String review;

	@Field
	private Integer rating;



	@PersistenceConstructor
	public Review(UUID reviewId, UUID userId, UUID itemId, String review, Integer rating) {
		this.reviewId = reviewId;
		this.userId = userId;
		this.itemId = itemId;
		this.review = review;
		this.rating = rating;
	}


	public UUID getUserId() {
		return userId;
	}

	public UUID getItemId() {return itemId;}

	public UUID getReviewId() {
		return reviewId;
	}


	public String getReview() {
		return review;
	}

	public Integer getRating(){return rating;}


	public void updateReview(String addition, Integer rating) {
		this.review += "\n appendix: \n";
		this.review += addition;
		this.rating = rating;
	}

}
