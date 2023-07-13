package com.example.testapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestApiController {
   @Autowired
   private UserRepository ALLATORIxDEMO;

   public static String ALLATORIxDEMO(String var0) {
      int var10000 = (2 ^ 5) << 4 ^ 3;
      int var10001 = (3 ^ 5) << 4 ^ 3;
      int var10002 = 5 << 4 ^ 4 << 1;
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

   @PostMapping({"/input"})
   public String InputSave(@ModelAttribute Input a) {
      TestApiController var2 = this;
      TestApiController var3 = new User();
      var3.setID(a.getID());
      var3.setName(a.getName());
      var3.setGender(a.getGender());
      var3.setBirthdate(a.getBirthdate());
      var2.ALLATORIxDEMO.save(var3);
      return TestApiApplication.ALLATORIxDEMO("\u0010A W&G0");
   }

   @RequestMapping({"/input"})
   public String InputForm(@ModelAttribute Input var1) {
      return ALLATORIxDEMO("*6\u0013-\u0017");
   }

   @GetMapping({"/all"})
   @ResponseBody
   public Iterable<User> getAllUsers() {
      return a.ALLATORIxDEMO.findAll();
   }
}
