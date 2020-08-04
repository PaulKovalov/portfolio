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
import java.util.Collection;

public final class FindMeetingQuery {
  // returns a list of time ranges appropriate for the request
  // time complexity is O(G * E) where:
  // -- G is a number of people who need to attend the event
  // -- E is a number of events in total where anyone of set G may participate
  // space complexity is O(max(E, G))
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // similar to merge intervals problem. My idea is to merge all request's attendees' events
    // and consider gaps between those events, if the gap is big enough to fit request's time, add the gap to the answer
    ArrayList<TimeRange> answer = new ArrayList<>();
    ArrayList<String> attendees = new ArrayList<>(request.getAttendees());
    ArrayList<Event> relevantEvents = new ArrayList<>();

    // check if request for a valid duration
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return answer;
    }
  
    for (String p: attendees) {
      // get the list of all events for this person
      for (Event e: events) {
        // if event includes the person
        if (e.getAttendees().contains(p)) {
          relevantEvents.add(e);
        }
      }
    }

    // sort events by start date
    relevantEvents.sort((e1, e2) -> {
      TimeRange e1when = e1.getWhen();
      TimeRange e2when = e2.getWhen();
      if (e1when.start() == e2when.start()) {
        return e1when.end() - e2when.start();
      }
      return e1when.start() - e2when.start();
    });
    // if no events, return whole day
    if (relevantEvents.size() == 0) {
      answer.add(TimeRange.WHOLE_DAY);
      return answer;
    }

    // now merge all events
    ArrayList<Interval> takenIntervals = new ArrayList<>();
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

    // when all events are merged, find appropriate intervals
    // handle corner case
    Interval first = takenIntervals.get(0);
    if (first.start != 0) {
      if (first.start >= request.getDuration()) {
        // add interval to answer if its duration is big enough
        answer.add(TimeRange.fromStartEnd(0, first.start, false));
      }
    }
    // general case
    for (int i  = 0; i < takenIntervals.size() - 1; ++i) {
      Interval cur = takenIntervals.get(i);
      Interval next = takenIntervals.get(i + 1);
      int duration = next.start - cur.end;
      if (duration >= request.getDuration()) {
        // add interval to answer if its duration is big enough
        answer.add(TimeRange.fromStartEnd(cur.end, next.start, false));
      }
    }
    // another corner case when there is may be a gap between the end of the day and the last interval
    Interval last = takenIntervals.get(takenIntervals.size() - 1);
    if (last.end != 1440) {
      // add only if enough duration
      if (1440 - last.end >= request.getDuration()) {
        answer.add(TimeRange.fromStartEnd(last.end, 1440, false));
      }
    }
    return answer;
  }

  // converts event to a more convinient Interval object
  public Interval eventToInterval(Event e) {
    return new Interval(e.getWhen().start(), e.getWhen().end());
  }
}
