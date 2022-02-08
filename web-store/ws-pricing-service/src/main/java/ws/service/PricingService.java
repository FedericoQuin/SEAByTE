package ws.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import ws.domain.Price;
import ws.repository.PriceRepository;

public class PricingService {
	

	@Autowired
	private PriceRepository pricingRepo;



	public List<Price> getAllPrices() {
		return this.pricingRepo.findAll();
	}

	public Optional<BigDecimal> getItemPrice(UUID id) {
		return pricingRepo.findById(id)
			.map(p -> Optional.of(p.getPrice()))
			.orElse(Optional.empty());
	}

	public void setNewPrice(UUID id, BigDecimal price) {
		pricingRepo.findById(id).ifPresentOrElse(
			p -> p.setPrice(price), 
			() -> pricingRepo.save(new Price(id, price))
		);
	}


	public void removeAllPrices() {
		pricingRepo.deleteAll();
	}
}
