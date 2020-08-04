// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {
  // returns a list of time ranges appropriate for the request
  // time complexity is O(G * E) where:
  // -- G is a number of people who need to attend the event
  // -- E is a number of events in total where anyone of set G may participate
  // space complexity is O(E)
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // similar to merge intervals problem. My idea is to merge all request's attendees' events
    // and consider gaps between those events, if the gap is big enough to fit request's time, add the gap to the answer
    // Follow up on optional attendees
    // I guess the algorithm is the following:
    // merge answer with the optional events, and if new answer is not empty, return new answer
    // otherwise ignore optional attendees
    ArrayList<Event> relevantEvents = new ArrayList<>();
    // the list of events of optional employees'
    ArrayList<Event> optionalEvents = new ArrayList<>();

    // check if request for a valid duration
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // get the list of events for required attendees
    for (String p : request.getAttendees()) {
      for (Event e : events) {
        // if event includes the person
        if (e.getAttendees().contains(p)) {
          relevantEvents.add(e);
        }
      }
    }

    // get the list of events for optional attendees
    for (String p : request.getOptionalAttendees()) {
      for (Event e : events) {
        // if event includes the person
        if (e.getAttendees().contains(p)) {
          optionalEvents.add(e);
        }
      }
    }

    // sort events by start date
    relevantEvents.sort((e1, e2) -> {
      TimeRange e1when = e1.getWhen();
      TimeRange e2when = e2.getWhen();
      if (e1when.start() == e2when.start()) {
        return e1when.end() - e2when.end();
      }
      return e1when.start() - e2when.start();
    });    
    // now merge all events
    ArrayList<Interval> takenIntervals = getTakenIntervals(relevantEvents);
    ArrayList<TimeRange> answer = getFreeTimeRanges(takenIntervals, request.getDuration());
    ArrayList<TimeRange> answerIncludingOptionals = answer;
    // handle optionals by inserting each of their events to the taken intervals, and if
    // there are no free gaps remains, return noOptionalAnswer
    for (Event e: optionalEvents) {
      // check if some free space left that can accomodate the request
      takenIntervals = insertInterval(takenIntervals, eventToInterval(e));
      answerIncludingOptionals = getFreeTimeRanges(takenIntervals, request.getDuration());
    }
    if (answerIncludingOptionals.size() == 0) {
      return answer;
    } else {
      return answerIncludingOptionals;
    }
  }

  // converts event to a more convinient Interval object
  private Interval eventToInterval(Event e) {
    return new Interval(e.getWhen().start(), e.getWhen().end());
  }

  // returns a list of taken intervals
  private ArrayList<Interval> getTakenIntervals(ArrayList<Event> relevantEvents) {
    ArrayList<Interval> takenIntervals = new ArrayList<>();
    if (relevantEvents.size() == 0) {
      return takenIntervals;
    }
    takenIntervals.add(eventToInterval(relevantEvents.get(0))); // add the first event to the merged
    // merge remaining events
    for (int i = 1; i < relevantEvents.size(); ++i) {
      Interval cur = eventToInterval(relevantEvents.get(i));
      Interval last = takenIntervals.get(takenIntervals.size() - 1);
      // two possible cases - either current event's start is later than last merged event's end, or not
      if (cur.start > last.end) {
        // simply add new interval to the merged
        takenIntervals.add(cur);
      } else {
        // otherwise update last merged one's end
        takenIntervals.get(takenIntervals.size() - 1).end = Math.max(cur.end, last.end);
      }
    }
    return takenIntervals;
  }

  // returns a list of free time gaps based on taken intervals
  private ArrayList<TimeRange> getFreeTimeRanges(ArrayList<Interval> takenIntervals, long requestedDuration) {
    ArrayList<TimeRange> answer = new ArrayList<>();
    if (takenIntervals.size() == 0) {
      answer.add(TimeRange.WHOLE_DAY);
      return answer;
    }
    // when all events are merged, find appropriate intervals
    // handle corner case
    takenIntervals.add(0, new Interval(0, 0));
    takenIntervals.add(takenIntervals.size(), new Interval(1440, 1440));
    // general case
    for (int i = 0; i < takenIntervals.size() - 1; ++i) {
      Interval cur = takenIntervals.get(i);
      Interval next = takenIntervals.get(i + 1);
      int duration = next.start - cur.end;
      if (duration >= requestedDuration) {
        // add interval to answer if its duration is big enough
        answer.add(TimeRange.fromStartEnd(cur.end, next.start, false));
      }
    }
    return answer;
  }

  // inserts the list of intervals to the {@code to} and returns new merged list
  private ArrayList<Interval> insertInterval(ArrayList<Interval> to, Interval newInterval) {
    ArrayList<Interval> result = new ArrayList<>();
    int i = 0;
    // add all the intervals ending before newInterval starts
    while (i < to.size() && to.get(i).end < newInterval.start) {
      result.add(to.get(i++));
    }
    // merge all overlapping intervals to one considering newInterval
    while (i < to.size() && to.get(i).start <= newInterval.end) {
      newInterval = new Interval( // we could mutate newInterval here also
          Math.min(newInterval.start, to.get(i).start), Math.max(newInterval.end, to.get(i).end));
      i++;
    }
    result.add(newInterval); // add the union of intervals we got
    // add all the rest
    while (i < to.size()) {
      result.add(to.get(i++));
    }
    return result;
  }
}
