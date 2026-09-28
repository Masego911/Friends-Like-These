package com.friendslikethese.backend.registration;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.team.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import com.friendslikethese.backend.event.CurrentEventProvider;
@Service
public class RegistrationPreviewService {
 private final GoogleSheetsClient sheets; private final CurrentEventProvider currentEvents; private final TeamRepository teams;
 public RegistrationPreviewService(GoogleSheetsClient s, CurrentEventProvider currentEvents, TeamRepository t){sheets=s;this.currentEvents=currentEvents;teams=t;}
 @Transactional(readOnly=true) public RegistrationPreviewResponse preview(){
  var event=currentEvents.requireRegistrationOpen(); var b=sheets.fetchRegistrations(); var active=teams.findByEventIdAndDeletedFalse(event.getId());
  var out=new ArrayList<RegistrationPreview>(); int fresh=0, existing=0, conflicts=0;
  for(var r:b.registrations()){var n=r.teamName().trim(); var m=clean(r.members()); String st;
   var matches=active.stream().filter(t->t.getName().equalsIgnoreCase(n)).toList();
   if(n.isBlank()||m.isEmpty())st="INVALID"; else if(matches.size()>1)st="POSSIBLE_DUPLICATE"; else if(matches.isEmpty()){st="NEW";fresh++;} else if(canonical(matches.get(0).getMembers().stream().map(TeamMember::getFullName).toList()).equals(canonical(m))){st="ALREADY_IMPORTED";existing++;} else {st="UPDATED";conflicts++;}
   out.add(new RegistrationPreview(n,m,st)); }
  return new RegistrationPreviewResponse(b.rowsRead(),out.size(),fresh,existing,conflicts,b.rowsSkipped(),out);
 }
 private List<String> clean(List<String> v){return v.stream().map(String::trim).filter(x->!x.isBlank()).toList();}
 private List<String> canonical(List<String> v){return v.stream().map(String::trim).sorted().toList();}
}
