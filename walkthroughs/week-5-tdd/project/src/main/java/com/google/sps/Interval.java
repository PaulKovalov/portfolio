package com.google.sps;

// represents an interval by a pair of numbers start and end
public class Interval implements Comparable<Interval>{
  public int start;
  public int end;

  public Interval(int s, int e) {
    this.start = s;
    this.end = e;
  }

  @Override
  public String toString() {
    return "Interval [ " + this.start + ", " + this.end + "] ";
  }

  @Override
  public int compareTo(Interval other) {
    if (this.start == other.start) {
      return this.end - other.end;
    }
    return this.start - other.start;
  }
}
