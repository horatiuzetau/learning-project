package org.example.reflection;

import java.lang.reflect.InvocationTargetException;

public class ReflectionTestService {

  public void testReflection() {
    modifyPrivateFieldWithoutSetter();
    callPrivateMethods();
  }

  private void callPrivateMethods() {
    var reflectionClass = new ReflectionClass();

    try {
      var privateMethod = ReflectionClass.class.getDeclaredMethod("privateMethod");
      // By setting the field to accessible = true, it can now be accessed and modified
      privateMethod.setAccessible(true);
      System.out.printf("Private method output: %s%n", privateMethod.invoke(reflectionClass));
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void modifyPrivateFieldWithoutSetter() {
    var reflectionClass = new ReflectionClass();
    reflectionClass.setPrivateString("PRIVATE");
    reflectionClass.setPublicString("PUBLIC");
    System.out.printf(
        "Public field: %s; Private field: %s%n",
        reflectionClass.getPublicString(), reflectionClass.getPrivateString()
    );
    try {
      // Modify public field at runtime
      var publicField = ReflectionClass.class.getDeclaredField("publicString");
      publicField.set(reflectionClass, "PUBLIC MODIFIED");

      var privateField = ReflectionClass.class.getDeclaredField("privateString");
      // By setting the field to accessible = true, it can now be accessed and modified
      privateField.setAccessible(true);
      privateField.set(reflectionClass, "MODIFIED PRIVATE");

      System.out.printf(
          "Public field: %s; Private field: %s%n%n",
          publicField.get(reflectionClass), privateField.get(reflectionClass)
      );
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
