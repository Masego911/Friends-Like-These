package com.friendslikethese.backend.round;
import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/events") public class RoundController{
 private final RoundService service; public RoundController(RoundService service){this.service=service;}
 @GetMapping("/current/rounds") public List<RoundResponse> current(){return service.current();}
 @PostMapping("/current/rounds/{number}/start") public RoundResponse start(@PathVariable int number){return service.start(number);}
 @PostMapping("/current/rounds/{number}/complete") public RoundResponse complete(@PathVariable int number){return service.complete(number);}
 @GetMapping("/current/rounds/{number}/leaderboard") public List<RoundStandingResponse> currentStandings(@PathVariable int number){var list=service.current();if(list.isEmpty())return List.of();return service.standings(list.get(0).eventId(),number);}
 @GetMapping("/{eventId}/rounds") public List<RoundResponse> eventRounds(@PathVariable UUID eventId){return service.list(eventId);}
 @GetMapping("/{eventId}/rounds/{number}/leaderboard") public List<RoundStandingResponse> standings(@PathVariable UUID eventId,@PathVariable int number){return service.standings(eventId,number);}
}
