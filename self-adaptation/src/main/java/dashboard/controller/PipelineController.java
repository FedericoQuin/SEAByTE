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

import dashboard.service.ABPipelineService;
import domain.pipeline.Pipeline;

@Controller
@RequestMapping("/pipeline")
public class PipelineController {
    
	protected Logger logger = Logger.getLogger(PipelineController.class.getName());

    @Autowired
    private ABPipelineService pipelineService;
    

    @GetMapping(value="")
    public String indexPage() {
        this.logger.info("Requested AB pipeline page");
        return "pipeline.html";
    }
    
    @PostMapping(value="/newPipeline")
    @ResponseStatus(value=HttpStatus.OK)
    public void addNewPipeline(@RequestBody Pipeline pipeline) {
        this.logger.info("Adding new pipeline.");
        this.pipelineService.addPipeline(pipeline);
    }

    @GetMapping(value="/retrieve")
    public @ResponseBody Collection<Pipeline> getPipelines() {
        return this.pipelineService.getAllPipelines();
    }
}
