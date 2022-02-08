package dashboard.controller;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import dashboard.service.ABRunService;

@Controller
@RequestMapping("/run")
public class RunController {
    
    private static Logger logger = Logger.getLogger(RunController.class.getName());

    @Autowired
    ABRunService runService;


    @GetMapping(value="")
    public String index() {
        return "run.html";
    }



    @PostMapping(value="/runSetup")
    @ResponseStatus(value=HttpStatus.OK)
    public void runSetup(@RequestParam String name) {
        logger.info("Running custom setup in the AB application");
        runService.runSetup(name);
    }
}
