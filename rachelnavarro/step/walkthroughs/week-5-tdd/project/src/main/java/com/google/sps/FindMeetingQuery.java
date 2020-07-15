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

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class FindMeetingQuery {

  private ArrayList<TimeRange> possibleTimes = new ArrayList<TimeRange>();
  private int currentTime = 0;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Get prescheduled events and data about the meeting request.
    long meetingLength = request.getDuration();
    ArrayList<String> meetingAttendees = new ArrayList<String>(request.getAttendees());
    ArrayList<String> meetingOptionalAttendees = new ArrayList<String>(request.getOptionalAttendees());
    ArrayList<Event> prescheduledEvents = new ArrayList<Event>(events);

    // A meeting with no atendees(and no optional attendees) can be scheduled for any time of the day.
    if (meetingAttendees.size() == 0 && meetingOptionalAttendees.size() == 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // A meeting that has a negative duration or that is longer than the whole day cannot be scheduled at all.
    if (meetingLength < 0 || meetingLength > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    // Sort the prescheduled events by their start time.
    Collections.sort(prescheduledEvents, sortByStart);


    // Determine available times and update possibleTimes based off of findings.
    getPossibleTimes(prescheduledEvents, meetingAttendees, meetingLength);
    return possibleTimes;
  }

  /*
   * Sort events by their start times.
   */
  private Comparator<Event> sortByStart =  (Event eventA, Event eventB) -> Long.compare(eventA.getWhen().start(), eventB.getWhen().start());

  /*
   * See if a prescheduled event and the requested meeting have an attendee in common.
   */
  private boolean attendeesInCommon(ArrayList<String> meetingAttendees, ArrayList<String> eventAttendees) {
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
   * Squeeze in the requested meeting between now and the prescheduled event. 
   */
  private void scheduleMeetingBetween(Event prescheduledEvent, int timeBetween) {
    possibleTimes.add(TimeRange.fromStartDuration(currentTime, timeBetween));
    currentTime = prescheduledEvent.getWhen().end();
  }


  /*
   * Update possibleTimes to include any times during which the meeting request could be scheduled.
   */
  private void getPossibleTimes(ArrayList<Event> prescheduledEvents, ArrayList<String> meetingAttendees, long meetingLength) {

    for (Event event : prescheduledEvents) {

      ArrayList<String> eventAttendees = new ArrayList<String>(event.getAttendees()); 

      if (attendeesInCommon(meetingAttendees, eventAttendees)) {
        
        // If there's no more time left for the meeting, don't schedule it.
        if (currentTime + meetingLength > TimeRange.END_OF_DAY) { 
          return;
        }

        // If there's enough time between now and the prescheduled event, schedule the requested meeting in-between.
        int timeBetween = getTimeBetween(event, currentTime);
        if (timeBetween >= meetingLength) { 
          scheduleMeetingBetween(event, timeBetween);
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
  }

}
