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
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public final class FindMeetingQuery {
  // returns a list of time ranges appropriate for the request
  // time complexity is O(max(Attendee * Events * log Events, OptionalAttendee * OptionalEvents * log OptionalEvents)
  //                       + (TotalEvents log TotalEvents) where:
  // -- Attendee is a number of people who MUST attend the event
  // -- OptionalAttendee is a number of people who can attend the event
  // -- Events is a number of events in total where anyone from set of Attendee may participate
  // -- OptionalEvents is a number of events in total where anyone from set of OptionAttendee MUST participate
  // -- TotalEvents is a number events in total where people from request must participate
  // space complexity is O(E)
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // similar to merge intervals problem. My idea is to merge all request's attendees' events
    // and consider gaps between those events, if the gap is big enough to fit request's time, add the gap to the answer
    // Follow up on optional attendees
    // I guess the algorithm is the following:
    // merge answer with the optional events, and if new answer is not empty, return new answer
    // otherwise ignore optional attendees

    // comparator for event sorting
    Comparator<Event> eventComparator = (Event e1, Event e2) -> {
      TimeRange e1when = e1.getWhen();
      TimeRange e2when = e2.getWhen();
      if (e1when.start() == e2when.start()) {
        return e1when.end() - e2when.end();
      }
      return e1when.start() - e2when.start();
    };

    // provides sorting
    TreeSet<Event> requiredEvents = new TreeSet<>(eventComparator);

    // check if request for a valid duration
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // get the list of events for required attendees
    for (String p : request.getAttendees()) {
      for (Event e : events) {
        // if event includes the person
        if (e.getAttendees().contains(p)) {
          requiredEvents.add(e);
        }
      }
    }

    // now merge required events
    ArrayList<TimeRange> answer = getFreeTimeRanges(getTakenIntervals(requiredEvents), request.getDuration());

    // merge optional events to required events
    // get the list of events for optional attendees
    for (String p : request.getOptionalAttendees()) {
      for (Event e : events) {
        // if event includes the person
        if (e.getAttendees().contains(p)) {
          requiredEvents.add(e);
        }
      }
    }

    ArrayList<TimeRange> answerIncludingOptionals =getFreeTimeRanges(getTakenIntervals(requiredEvents), request.getDuration());
    return answerIncludingOptionals.size() == 0 ? answer : answerIncludingOptionals;
  }

  // converts event to a more convinient Interval object
  private Interval eventToInterval(Event e) {
    return new Interval(e.getWhen().start(), e.getWhen().end());
  }

  // returns a list of taken intervals
  private ArrayList<Interval> getTakenIntervals(TreeSet<Event> events) {
    ArrayList<Interval> takenIntervals = new ArrayList<>();
    if (events.size() == 0) {
      return takenIntervals;
    }
    Iterator<Event> iter = events.iterator();
    takenIntervals.add(eventToInterval(iter.next())); // add the first event to the merged
    // merge remaining events
    while (iter.hasNext()) {
      Interval cur = eventToInterval(iter.next());
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
}
