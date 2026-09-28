package com.friendslikethese.backend.auth;
import com.friendslikethese.backend.common.BusinessRuleException;import org.springframework.stereotype.Component;import java.time.*;import java.util.concurrent.*;
@Component public class LoginThrottle{
 private record Attempt(int failures,Instant blockedUntil){} private final ConcurrentMap<String,Attempt> attempts=new ConcurrentHashMap<>();
 public void check(String key){Attempt a=attempts.get(key);if(a!=null&&a.blockedUntil()!=null&&Instant.now().isBefore(a.blockedUntil()))throw new BusinessRuleException("Too many login attempts. Please try again later.");}
 public void failed(String key){attempts.compute(key,(k,a)->{int n=a==null?1:a.failures()+1;return new Attempt(n,n>=5?Instant.now().plus(Duration.ofMinutes(15)):null);});}
 public void succeeded(String key){attempts.remove(key);}
}
