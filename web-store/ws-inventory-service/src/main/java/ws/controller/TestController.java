package ws.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.service.InventoryService;

@RestController
@RequestMapping("test")
public class TestController {
	
	protected Logger logger = Logger.getLogger(TestController.class.getName());

	@Autowired
	private InventoryService service;


	@GetMapping(value="/data")
	public String testdata() {
		service.initializeDatabaseWithTestData();
		return "OK\n";
	}
	
}
