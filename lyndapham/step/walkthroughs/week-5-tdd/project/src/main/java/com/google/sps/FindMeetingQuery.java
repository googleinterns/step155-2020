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
  /** This class finds available meeting times given a MeetingRequest */

  /**
   *
   * Returns a Collection of TimeRanges that would be suitable for a meeting
   * @param events events that are already taking place for some attendees
   * @param request the MeetingRequest with its details
   * @return suitable times for the meeting
   *     <p>Assumptions All events are passed correctly formatted to MeetingRequest All attendees
   *     are mandatory All events that attendees are attending are provided
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> unavailableTimes = new ArrayList<>();
    ArrayList<TimeRange> availableTimes = new ArrayList<>();

    if (request.getAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) {
      return availableTimes;
    }

    unavailableTimes = getUnavailableTimes(events, request);
    availableTimes = getAvailableTimes(unavailableTimes, request);

    return availableTimes;
  }

  /** Returns an ArrayList of unavailable time blocks for the meeting */
  private ArrayList<TimeRange> getUnavailableTimes(
      Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> unavailableTimes = new ArrayList<>();

    for (Event event : events) {
      if (containsAttendee(event, request)) {
        unavailableTimes.add(event.getWhen());
      }
    }
    unavailableTimes = collapseUnavailableTimes(unavailableTimes);
    Collections.sort(unavailableTimes, TimeRange.ORDER_BY_START);
    return unavailableTimes;
  }

  /** Returns whether the event passed in contains any of the meeting's attendees */
  private Boolean containsAttendee(Event event, MeetingRequest request) {
    for (String attendee : event.getAttendees()) {
      if (request.getAttendees().contains(attendee)) {
        return true;
      }
    }
    return false;
  }

  /** Combines and returns unavailable times into larger time blocks */
  private ArrayList<TimeRange> collapseUnavailableTimes(ArrayList<TimeRange> uncollapsed) {
    ArrayList<TimeRange> collapsed = new ArrayList<>();
    if (uncollapsed.size() > 0) {
      collapsed.add(uncollapsed.get(0));
      int collapsedIdx = 0;
      for (int i = 1; i < uncollapsed.size(); i++) {
        // check if next TimeRange is equal to, contained in, or overlaps, with collapsed time
        if (collapsed.get(collapsedIdx).equals(uncollapsed.get(i))
            || collapsed.get(collapsedIdx).contains(uncollapsed.get(i))
            || uncollapsed.get(i).contains(collapsed.get(collapsedIdx))) {
          continue;
        } else if (collapsed.get(collapsedIdx).overlaps(uncollapsed.get(i))) {
          // start should be smallest start time, end should be the largest end time
          int start =
              (collapsed.get(collapsedIdx).start() < uncollapsed.get(i).start())
                  ? collapsed.get(collapsedIdx).start()
                  : uncollapsed.get(i).start();
          int end =
              (collapsed.get(collapsedIdx).end() > uncollapsed.get(i).end())
                  ? collapsed.get(collapsedIdx).end()
                  : uncollapsed.get(i).end();
          collapsed.add(TimeRange.fromStartEnd(start, end, false));
          collapsed.remove(collapsedIdx);
        } else {
          // there is no overlap, just add to collapsed as a separate event
          collapsed.add(uncollapsed.get(i));
          collapsedIdx++;
        }
      }
    }
    return collapsed;
  }

  /** Returns an ArrayList of available time blocks for the meeting */
  private ArrayList<TimeRange> getAvailableTimes(
      ArrayList<TimeRange> unavailableTimes, MeetingRequest request) {
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    int startTime = TimeRange.START_OF_DAY;
    for (TimeRange time : unavailableTimes) {
      TimeRange newTime = TimeRange.fromStartEnd(startTime, time.start(), false);
      if (newTime.duration() >= request.getDuration()) {
        availableTimes.add(TimeRange.fromStartEnd(startTime, time.start(), false));
      }
      startTime = time.end();
    }

    if (startTime != TimeRange.END_OF_DAY + 1) {
      availableTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
    }

    return availableTimes;
  }
}
