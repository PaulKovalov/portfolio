package com.google.sps.servlets;

public class BadRequestException extends Exception {
  public BadRequestException(String message) {
    super(message);
  }
}
