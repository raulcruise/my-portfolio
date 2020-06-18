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
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long meetingDurationMinutes = request.getDuration();

    if (meetingDurationMinutes > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
   
    List<TimeRange> unavailableMandatoryTimeRanges = getUnavailableTimeRanges(
        events, mandatoryAttendees);
    List<TimeRange> availableMandatoryTimeRanges = getAvailableTimeRanges(
        unavailableMandatoryTimeRanges, meetingDurationMinutes);

    List<TimeRange> unavailableOptionalTimeRanges = getUnavailableTimeRanges(events, optionalAttendees);
    List<TimeRange> availableOptionalTimeRanges = getAvailableTimeRanges(
        unavailableOptionalTimeRanges, meetingDurationMinutes);
    
    if (request.getAttendees().isEmpty()) {
      return availableOptionalTimeRanges;
    } else if (request.getOptionalAttendees().isEmpty() || availableOptionalTimeRanges.isEmpty()) {
      return availableTimeRanges;
    } else {
      return mergeAvailableTimeRanges(
          availableMandatoryTimeRanges, availableOptionalTimeRanges, meetingDurationMinutes);
    }
}
  /**
   * Returns a List of TimeRanges that are unavailable in as little TimeRanges as possible.
   */
  private List<TimeRange> getUnavailableTimeRanges(Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> unavailableTimeRanges = new ArrayList<>();

    // If an event's attendee list contains a names of an attendee that needs to attend
    // (from Collection<String> attendees, then we add the event's TimeRange to a list.
    events.forEach(event -> {
      Collection<String> eventAttendants = event.getAttendees();

      for (String attendee : eventAttendants) {
        if (attendees.contains(attendee)) {
          TimeRange currentEventTimeRange = event.getWhen();
          
          // If the currentEventTimeRange isn't merged into a TimeRange within the
          // unavailableTimeRanges list, then add the currentEventTimeRange to the list.
          if (!mergeOnOverlap(unavailableTimeRanges, currentEventTimeRange)) {
            unavailableTimeRanges.add(currentEventTimeRange);
          }

          break;
        }
      }
    });

    return unavailableTimeRanges;
  }

  private boolean mergeOnOverlap(Collection<TimeRange> timeRanges, TimeRange newTimeRange) {
    // The default value is set to false, which is converted to true if a merge takes place.
    boolean overlaps = false;

    // Store items that we will add and remove from the collection after we finish iterating
    // through it in order to avoid ConcurrentModificationException from being thrown
    List<TimeRange> toRemove = new ArrayList<>();
    List<TimeRange> toAdd = new ArrayList<>();

    for (TimeRange takenTimeRange : timeRanges) {
      if (newTimeRange.overlaps(takenTimeRange)) {
        TimeRange combinedTimeRange = mergeTimeRanges(newTimeRange, takenTimeRange);
        toRemove.add(takenTimeRange);
        toAdd.add(combinedTimeRange);
        overlaps = true;
      }
    }

    for (TimeRange timeRange : toRemove) {
      timeRanges.remove(timeRange);
    }

    for (TimeRange timeRange : toAdd) {
      timeRanges.add(timeRange);
    }

    return overlaps;
  }

  private TimeRange mergeTimeRanges(TimeRange firstTimeRange, TimeRange secondTimeRange) {
    TimeRange mergedTimeRange = TimeRange.fromStartEnd(
        Math.min(firstTimeRange.start(), secondTimeRange.start()),
        Math.max(firstTimeRange.end(), secondTimeRange.end()),
        /* inclusive = */ false);

    return mergedTimeRange;
  }

  private List<TimeRange> getAvailableTimeRanges(List<TimeRange> unavailableTimeRanges, long durationMinutes) {
    if (unavailableTimeRanges.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(unavailableTimeRanges, TimeRange.ORDER_BY_START);
    List<TimeRange> availableTimeRanges = new ArrayList<>();

    // Check for available time starting at the beginning of the day.
    if (unavailableTimeRanges.get(0).start() >= durationMinutes) {
      availableTimeRanges.add(TimeRange.fromStartEnd(
          TimeRange.WHOLE_DAY.start(),
          unavailableTimeRanges.get(0).start(),
          /* inclusive = */ false));
    }
    
    int i;
    for (i = 0; i < unavailableTimeRanges.size() - 1; i++) {
      if(enoughTimeBetween(unavailableTimeRanges.get(i), unavailableTimeRanges.get(i + 1), durationMinutes)) {
        availableTimeRanges.add(TimeRange.fromStartEnd(
            unavailableTimeRanges.get(i).end(),
            unavailableTimeRanges.get(i + 1).start(),
            /* inclusive = */ false));
        }
    }

    // Check for available time at the end of the day.
    int timeAfterLastMeeting = TimeRange.WHOLE_DAY.end() - unavailableTimeRanges.get(i).end();
    if (timeAfterLastMeeting >= durationMinutes) {
      availableTimeRanges.add(TimeRange.fromStartEnd(
          unavailableTimeRanges.get(unavailableTimeRanges.size() - 1).end(),
          TimeRange.WHOLE_DAY.end(),
          /* inclusive = */ false));
    }

    return availableTimeRanges;
  }

  private List<TimeRange> mergeAvailableTimeRanges(
      List<TimeRange> mandatory, List<TimeRange> optional, long durationMinutes) {
    List<TimeRange> combined = new ArrayList<>();
    int i = 0;
    int j = 0;
    while(i < mandatory.size() && j < optional.size()) {
      TimeRange mandatoryRange = mandatory.get(i);
      TimeRange optionalRange = optional.get(j);

      // Start by comparing the first two TimeRanges
      if (mandatoryRange.overlaps(optionalRange)) {
        int rangeStart = Math.max(mandatoryRange.start(), optionalRange.start());
        int rangeEnd = Math.min(mandatoryRange.end(), optionalRange.end());
      
        if (rangeEnd - rangeStart >= durationMinutes) {
          combined.add(TimeRange.fromStartEnd(
              rangeStart,
              rangeEnd,
              /* inclusive = */ false));
        }
      }
      
      // Move to the next index depending on which lists' current timeRange
      // ends first since the timeRange which ends later can be merged with
      // another or multiple other timeRanges.
      if (mandatoryRange.end() >= optionalRange.end()) {
        j++;
      } else {
        i++;
      }
    }

    return combined;
  }

  private boolean enoughTimeBetween(TimeRange firstTime, TimeRange nextTime, long durationMinutes) {
    return (nextTime.start() - firstTime.end()) >= durationMinutes;
  }
}
