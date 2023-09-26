package org.example.functionalinterfaces;

public class FunctionalInterfaceService {

  public void testFunctionalInterface() {
    displayTwoIntegers(2, 5, (a, b) -> String.format("Sum %d + %d = %d", a, b, a + b));
    displayTwoIntegers(5, 5, (a, b) -> String.format("%d ---- %d", a, b));
    displayTwoIntegers(5, 10, (a, b) -> String.format("%d --///-- %d", a, b));
  }

  public void displayTwoIntegers(int a, int b, MyFunctionalInterface myFunctionalInterface) {
    System.out.println(myFunctionalInterface.sum(a, b));
  }
}
