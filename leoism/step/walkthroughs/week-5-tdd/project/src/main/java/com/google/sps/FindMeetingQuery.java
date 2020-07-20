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
import java.util.List;

public final class FindMeetingQuery {
  /**
   * Returns a Collection of TimeRange for the requested meeting can take place and attendees can
   * attend. Assumes that all events are provided in order from start of day to end of day.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> slotsOpen = new ArrayList<>();
    // A requested meeting duration longer than the time of day should not be possible.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return slotsOpen;
    }

    if (events.isEmpty()) {
      slotsOpen.add(TimeRange.WHOLE_DAY);
      return slotsOpen;
    }

    ArrayList<TimeRange> slotsTaken = getCombinedSlotsTaken(events, request, false);

    slotsOpen = calculateOpenSlots(slotsTaken, request.getDuration());

    // There are no optional attendees so there is no need to check if they can attend or not.
    if (request.getOptionalAttendees().isEmpty()) {
      return slotsOpen;
    }

    return getAvailableOptionalTimes(slotsOpen, events, request);
  }

  /**
   * Given an ArrayList of TimeRange of slots that are unavailble and a requested meetings duration,
   * an ArrayList of TimeRange is returned of all time slots that are equal to or greater than the
   * requested meeting duration.
   */
  private ArrayList<TimeRange> calculateOpenSlots(ArrayList<TimeRange> slotsTaken, long duration) {
    ArrayList<TimeRange> slotsOpen = new ArrayList<>();

    int previousEndTime = TimeRange.START_OF_DAY;
    for (TimeRange occupied : slotsTaken) {
      TimeRange potentialOpenSlot =
          TimeRange.fromStartEnd(previousEndTime, occupied.start(), false);
      if (potentialOpenSlot.duration() >= duration) {
        slotsOpen.add(potentialOpenSlot);
      }
      previousEndTime = occupied.end();
    }

    // END_OF_DAY is + 1 because END_OF_DAY is not inclusive by default.
    if (previousEndTime != TimeRange.END_OF_DAY + 1) {
      slotsOpen.add(TimeRange.fromStartEnd(previousEndTime, TimeRange.END_OF_DAY, true));
    }

    return slotsOpen;
  }

  /**
   * Checks if the requested time interval overlaps with another. If true, then the conflicting
   * TimeRange is removed and a new TimeRange is returned with the events time merged. Otherwise,
   * returns null if no TimeRange overlaps.
   */
  private TimeRange combineOverlaps(TimeRange requestedTime, ArrayList<TimeRange> slotsTaken) {
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
   * Returns true if at least one required attendee is present. If no required attendees are
   * present, returns false.
   */
  private boolean areRequiredAttendeesPresent(
      Collection<String> requiredAttendees, Collection<String> presentAttendees) {
    for (String attendee : requiredAttendees) {
      if (presentAttendees.contains(attendee)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns an ArrayList of TimeRange of all slots that are occupied by events. Assumes that all
   * events are provided in sorted order from start of day to end of day.
   */
  private ArrayList<TimeRange> getCombinedSlotsTaken(
      Collection<Event> events, MeetingRequest request, boolean isOptional) {
    ArrayList<TimeRange> slotsTaken = new ArrayList<>();
    Collection<String> currentAttendees =
        isOptional ? request.getOptionalAttendees() : request.getAttendees();

    for (Event event : events) {
      boolean mandatoryAttendeesPresent =
          areRequiredAttendeesPresent(currentAttendees, event.getAttendees());
      if (!mandatoryAttendeesPresent) {
        continue;
      }

      TimeRange requestedTime = event.getWhen();
      TimeRange overlappedCombined = combineOverlaps(requestedTime, slotsTaken);

      if (overlappedCombined != null) {
        slotsTaken.add(overlappedCombined);
      } else {
        slotsTaken.add(requestedTime);
      }
    }

    return slotsTaken;
  }

  /**
   * Returns a Collection of TimeRange slots with the optional attendees considered. Gets all
   * possible TimeRange openings for optional attendees. If no optional attendees can attend, then
   * the previous TimeRange slots are returned.
   */
  private Collection<TimeRange> getAvailableOptionalTimes(
      ArrayList<TimeRange> currSlotsOpen, Collection<Event> events, MeetingRequest request) {

    ArrayList<TimeRange> newOpenSlots = new ArrayList<>();
    newOpenSlots.addAll(currSlotsOpen);
    ArrayList<TimeRange> slotsTaken = getCombinedSlotsTaken(events, request, true);

    // Since elements inside newOpenSlots may be deleted, they are stored temporarily so that
    // slotsTaken can also remove the TimeRanges.
    List<Object> tempHolderForDeletion = Arrays.asList(newOpenSlots.toArray());
    newOpenSlots.removeAll(Arrays.asList(slotsTaken.toArray()));
    slotsTaken.removeAll(tempHolderForDeletion);

    // Traditional for loop is used to prevent forEach iterators from throwing modification
    // exceptions.
    for (int i = 0; i < slotsTaken.size(); i++) {
      TimeRange slotTaken = slotsTaken.get(i);
      if (request.getDuration() > slotTaken.duration()) {
        slotsTaken.remove(slotTaken);
      }
    }

    if (slotsTaken.isEmpty() && !newOpenSlots.isEmpty()) {
      return newOpenSlots;
    }

    newOpenSlots = calculateOpenSlots(slotsTaken, request.getDuration());

    return newOpenSlots.isEmpty() ? currSlotsOpen : newOpenSlots;
  }
}
