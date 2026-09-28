package com.friendslikethese.backend.round;
import java.util.UUID;
public record RoundStandingResponse(int position,UUID teamId,String teamName,int roundScore,int overallScore){}
