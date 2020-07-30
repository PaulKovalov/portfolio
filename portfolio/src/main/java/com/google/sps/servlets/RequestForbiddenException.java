/**
 * This file has class RequestForbiddenException
 */

package com.google.sps.servlets;

/**
 * Simple exception to handle cases when user doesn't have enough access
 */
public class RequestForbiddenException extends Exception {
  public RequestForbiddenException(String message) {
    super(message);
  }
}
