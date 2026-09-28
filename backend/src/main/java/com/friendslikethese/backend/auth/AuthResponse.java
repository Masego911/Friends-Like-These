package com.friendslikethese.backend.auth;
public record AuthResponse(boolean authenticated,String username,String email,String displayName,AdminRole role){}
