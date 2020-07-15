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
import java.util.HashSet;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> slotsOpen = new ArrayList<TimeRange>();

    // A requested meeting duration longer than the time of day should not be possible.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return slotsOpen;
    }

    if (events.isEmpty()) {
      slotsOpen.add(TimeRange.WHOLE_DAY);
      return slotsOpen;
    }

    ArrayList<TimeRange> slotsTaken = getSlotsTaken(events, request);

    int previousEndTime = TimeRange.START_OF_DAY;
    for (TimeRange occupied : slotsTaken) {
      slotsOpen.add(TimeRange.fromStartEnd(previousEndTime, occupied.start(), false));
      previousEndTime = occupied.end();
    }

    slotsOpen.add(TimeRange.fromStartEnd(previousEndTime, TimeRange.END_OF_DAY, true));

    return sufficientTimeAvailable(slotsOpen, request);
  }

  /**
   * Checks if the requested time interval overlaps with another. If true, then the conflicting
   * TimeRange is removed and a new TimeRange is returned with the events time merged. Otherwise,
   * returns null if no TimeRange overlaps.
   */
  private TimeRange doesOverlap(TimeRange requestedTime, ArrayList<TimeRange> slotsTaken) {
    for (TimeRange occupied : slotsTaken) {
      if (requestedTime.overlaps(occupied)) {
        // If an overlap occurs, merge them by getting the earliest start time and the latest start
        // time.
        int startTime =
            occupied.start() > requestedTime.start() ? requestedTime.start() : occupied.start();
        int endTime = occupied.end() > requestedTime.end() ? occupied.end() : requestedTime.end();
        slotsTaken.remove(occupied);
        return TimeRange.fromStartEnd(startTime, endTime, false);
      }
    }
    return null;
  }

  /**
   * Checks if all required attendees (those that the event is booked under) are the same as the one
   * making the Meeting Request. Returns false if a required attendee is not present. Otherwise,
   * returns true.
   */
  private boolean areRequiredAttendeesPresent(
      Collection<String> requiredAttendees, Collection<String> presentAttendees) {
    for (String attendee : presentAttendees) {
      if (!requiredAttendees.contains(attendee)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Stores all attendees which the event is booked under and stores them inside a Set to avoid
   * duplicates. Returns a HashSet with all attendees booked under an event.
   */
  private HashSet<String> allEventAttendees(Collection<Event> events) {
    HashSet<String> eventAttendees = new HashSet<String>();
    for (Event event : events) {
      eventAttendees.addAll(event.getAttendees());
    }

    return eventAttendees;
  }

  /** Returns an ArrayList of TimeRange of all slots that are occupied by events. */
  private ArrayList<TimeRange> getSlotsTaken(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> slotsTaken = new ArrayList<TimeRange>();
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> eventAttendees = allEventAttendees(events);

    for (Event event : events) {
      TimeRange requestedTime = event.getWhen();
      TimeRange overlappedCombined = doesOverlap(requestedTime, slotsTaken);

      boolean mandatoryAttendeesPresent =
          areRequiredAttendeesPresent(eventAttendees, mandatoryAttendees);
      if (!mandatoryAttendeesPresent) {
        continue;
      }

      if (overlappedCombined != null) {
        slotsTaken.add(overlappedCombined);
        continue;
      }
      slotsTaken.add(requestedTime);
    }

    return slotsTaken;
  }

  /**
   * Checks all available slots to see if the there is sufficient time for the requested meeting
   * duration. If there is a sufficient time slot, it is added to an ArrayList of TimeRange. When
   * all slots have been checked, returns an ArrayList of TimeRange with sufficient times.
   */
  private ArrayList<TimeRange> sufficientTimeAvailable(
      ArrayList<TimeRange> slotsOpen, MeetingRequest request) {
    ArrayList<TimeRange> sufficientTimes = new ArrayList<TimeRange>();
    for (TimeRange availableSlot : slotsOpen) {
      if (availableSlot.duration() >= request.getDuration()) {
        sufficientTimes.add(availableSlot);
      }
    }
    return sufficientTimes;
  }
}
