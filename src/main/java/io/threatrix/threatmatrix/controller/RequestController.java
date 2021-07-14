package io.threatrix.threatmatrix.controller;

import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/request")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @GetMapping("/list")
    public List<MyReqeust> getRequests(Principal principal) {
        List<MyRequest> requests = requestService.getAllRequests();

        return requests.filter(req => req.user == principal.getUser());
    }
}
