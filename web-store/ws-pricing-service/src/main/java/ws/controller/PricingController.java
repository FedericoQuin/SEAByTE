package ws.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.domain.Price;
import ws.service.PricingService;


@RestController
@RequestMapping("prices")
public class PricingController {
	

	@Autowired
	private PricingService service;



	@GetMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Price> getPrices() {
		return service.getAllPrices();
	}
	
	
	@DeleteMapping(value="")
	public void removePrices() {
		service.removeAllPrices();
	}

	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Optional<BigDecimal> getPrice(@PathVariable UUID id) {
		return service.getItemPrice(id);
	}


	@PutMapping("/{id}")
	public void setPrice(@PathVariable UUID id, @RequestBody BigDecimal price) {
		service.setNewPrice(id, price);
	}
}
