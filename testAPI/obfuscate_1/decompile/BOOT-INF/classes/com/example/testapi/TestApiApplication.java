package com.example.testapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApiApplication {
   public static String ALLATORIxDEMO(String var0) {
      int var10000 = (2 ^ 5) << 4 ^ 2 << 2 ^ 1;
      int var10001 = 4 << 4 ^ 3;
      int var10002 = (3 ^ 5) << 3 ^ 4;
      String var3;
      int var10003 = (var3 = (String)var0).length();
      char[] var10004 = new char[var10003];
      boolean var10006 = true;
      int var5 = var10003 - 1;
      var10003 = var10002;
      int a;
      var10002 = a = var5;
      char[] var1 = var10004;
      int var4 = var10003;
      var10000 = var10002;

      for(int var2 = var10001; var10000 >= 0; var10000 = a) {
         var10001 = a;
         char var7 = var3.charAt(a);
         --a;
         var1[var10001] = (char)(var7 ^ var2);
         if (a < 0) {
            break;
         }

         var10002 = a--;
         var1[var10002] = (char)(var3.charAt(var10002) ^ var4);
      }

      return new String(var1);
   }

   public static void main(String[] a) {
      System.out.println("\n################################################\n#                                              #\n#        ## #   #    ## ### ### ##  ###        #\n#       # # #   #   # #  #  # # # #  #         #\n#       ### #   #   ###  #  # # ##   #         #\n#       # # ### ### # #  #  ### # # ###        #\n#                                              #\n# Obfuscation by Allatori Obfuscator v8.4 DEMO #\n#                                              #\n#           http://www.allatori.com            #\n#                                              #\n################################################\n");
      SpringApplication.run(TestApiApplication.class, a);
   }
}
