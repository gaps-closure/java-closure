package com.peratonlabs.closure.testprog.example1.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.peratonlabs.closure.testprog.example1.annotations.Cledef;
 
//@Target(ElementType.FIELD)
@Target({ElementType.FIELD, 
         ElementType.METHOD, 
         ElementType.CONSTRUCTOR,
         ElementType.LOCAL_VARIABLE})
// local variable annotations do not get retained at runtime, and we do JVM analysis not source analysis
@Retention(RetentionPolicy.RUNTIME)
@Cledef(clejson = "{" + 
                "               \"level\":\"orange\"," + 
                "               \"cdf\":[" + 
                "                  {" + 
                "                     \"remotelevel\":\"green\"," + 
                "                     \"direction\":\"egress\"," + 
                "                     \"guarddirective\":{" + 
                "                        \"operation\":\"allow\"" + 
                "                     }" + 
                "                  }" + 
                "               ]" + 
                "            }")
public @interface OrangeShareable {}

