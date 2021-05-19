package com.peratonlabs.closure.testprog.example1;

import com.peratonlabs.closure.testprog.example1.annotations.*;

public class Example1 {
  @OrangeShareable
  private int valueA;

  public int getA() {
    return this.valueA; 
  }

  public Example1() {
    valueA = 42;
  }

  public static void main(String[] args) {
    Example1 e = new Example1();
    System.out.println("Hello Example 1!" + e.getA());
  }
}