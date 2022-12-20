package dashboard.controller;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import dashboard.service.ABPopulationSplitService;
import domain.split.PopulationSplit;

@Controller
@RequestMapping("/split")
public class PopulationSplitController {
    
	protected Logger logger = Logger.getLogger(PopulationSplitController.class.getName());

    @Autowired
    private ABPopulationSplitService populationSplitService;



    @GetMapping(value="")
    public String index() {
        logger.info("Requested population split page");
        return "split.html";
    }


    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<PopulationSplit> getAllPopulationSplits() {
        return this.populationSplitService.getAllPopulationSplits();
    }


    @PostMapping(value="/newSplit")
    @ResponseStatus(value=HttpStatus.OK)
    public void addPopulationSplit(@RequestBody PopulationSplit populationSplit) {
        this.populationSplitService.addPopulationSplit(populationSplit);
    }

    
}
