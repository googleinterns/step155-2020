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
import java.util.Comparator;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Initialize possibleTimes and currentTime.
    ArrayList<TimeRange> possibleTimes = new ArrayList<TimeRange>();
    int currentTime = 0;

    // Get prescheduled events and data about the meeting request.
    long meetingLength = request.getDuration();
    ArrayList<Event> prescheduledEvents = new ArrayList<Event>(events);

    // A meeting that has a negative duration or that is longer than the whole day cannot be
    // scheduled at all.
    if (meetingLength < 0 || meetingLength > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // Sort the prescheduled events by their start time.
    Comparator<Event> sortByStart =
        (Event eventA, Event eventB) ->
            Long.compare(eventA.getWhen().start(), eventB.getWhen().start());
    Collections.sort(prescheduledEvents, sortByStart);

    // Determine available times and update possibleTimes based off of findings.
    possibleTimes = getPossibleTimes(possibleTimes, prescheduledEvents, request.getAttendees(), meetingLength, currentTime);
    return possibleTimes;
  }

  /*
   * See if a prescheduled event and the requested meeting have an attendee in common.
   */
  private boolean attendeesInCommon(
      Collection<String> meetingAttendees, ArrayList<String> eventAttendees) {
    for (String attendee : eventAttendees) {
      if (meetingAttendees.contains(attendee)) {
        return true;
      }
    }
    return false;
  }

  /*
   * Get time between now and prescheduled event. 
   */
  private int getTimeBetween(Event prescheduledEvent, int currentTime) {
    int eventStart = prescheduledEvent.getWhen().start(); 
    int eventEnd = prescheduledEvent.getWhen().end();
    return eventStart - currentTime; 
  }

  /*
   * Update possibleTimes to include any times during which the meeting request could be scheduled.
   */
  private ArrayList<TimeRange> getPossibleTimes(
      ArrayList<TimeRange> possibleTimes,
      ArrayList<Event> prescheduledEvents,
      Collection<String> meetingAttendees,
      long meetingLength,
      int currentTime) {

    for (Event event : prescheduledEvents) {

      ArrayList<String> eventAttendees = new ArrayList<String>(event.getAttendees()); 

      if (attendeesInCommon(meetingAttendees, eventAttendees)) {
        
        // If there's no more time left for the meeting, don't schedule it.
        if (currentTime + meetingLength > TimeRange.END_OF_DAY) { 
          return possibleTimes;
        }

        // If there's enough time between now and the prescheduled event, schedule the requested
        // meeting in-between.
        int timeBetween = getTimeBetween(event, currentTime);
        if (timeBetween >= meetingLength) { 
          TimeRange availableSlot = TimeRange.fromStartDuration(currentTime, timeBetween);
          possibleTimes.add(availableSlot);
          currentTime = event.getWhen().end();
          continue;
        }

        // Otherwise, move on to trying the next available time option after the prescheduled event.
        currentTime = Math.max(currentTime, event.getWhen().end()); 
      }
    }

    // Schedule meeting towards the end of the day, if possible.
    int timeRemainingToday = TimeRange.END_OF_DAY - currentTime;
    if (timeRemainingToday >= meetingLength) { 
      possibleTimes.add(TimeRange.fromStartEnd(currentTime, TimeRange.END_OF_DAY, true));
    }
    return possibleTimes;
  }

}
