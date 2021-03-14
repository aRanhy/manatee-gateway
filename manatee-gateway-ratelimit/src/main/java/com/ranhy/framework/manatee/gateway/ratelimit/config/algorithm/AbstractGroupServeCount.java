package com.ranhy.framework.manatee.gateway.ratelimit.config.algorithm;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
abstract public class AbstractGroupServeCount implements RateLimit {

   private volatile int  serveCount = 1 ;


   @Override
   public void setGroupServeCount(int count) {
      serveCount = count;
   }

   @Override
   public int getGroupServeCount() {
      return serveCount;
   }
}
