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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long meetingDurationMinutes = request.getDuration();

    Set<String> bothAttendees = new HashSet<String>();
    bothAttendees.addAll(mandatoryAttendees);
    bothAttendees.addAll(optionalAttendees);

    // If the meeting duration is over 24 hours, then return no available timeRange.
    if (meetingDurationMinutes > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    // If no Attendees are passed in, return a timeRange containing the whole day.
    if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Store unavailable TimeRanges for mandatory and mandatory + optional attendees separately.
    List<TimeRange> unavailableMandatoryTimeRanges =
        getUnavailableTimeRanges(events, mandatoryAttendees);
    List<TimeRange> unavailableOptionalTimeRanges = getUnavailableTimeRanges(events, bothAttendees);

    // Get available TimeRanges from unavailable TimeRanges.
    List<TimeRange> availableMandatoryTimeRanges =
        getAvailableTimeRanges(unavailableMandatoryTimeRanges, meetingDurationMinutes);
    List<TimeRange> availableOptionalTimeRanges =
        getAvailableTimeRanges(unavailableOptionalTimeRanges, meetingDurationMinutes);

    // If the List of available TimeRanges when including optional attendees is empty,
    // and mandatory attendees were passed in, then return availableMandatoryTimeRanges.
    // Else return availableOptionalTimeRanges.
    if (availableOptionalTimeRanges.isEmpty() && !mandatoryAttendees.isEmpty()) {
      return availableMandatoryTimeRanges;
    } else {
      return availableOptionalTimeRanges;
    }
  }

  /** Returns a List of TimeRanges that are unavailable in as little TimeRanges as possible. */
  private List<TimeRange> getUnavailableTimeRanges(
      Collection<Event> events, Collection<String> attendees) {
    List<TimeRange> unavailableTimeRanges = new ArrayList<>();

    // Convert the Collection of attendeese into a hash set in order to make each check
    // for a certain attendee O(1).
    Set<String> attendeeSet = new HashSet<String>();
    attendeeSet.addAll(attendees);

    // If an event's attendee list contains a names of an attendee that needs to attend
    // (from Collection<String> attendees, then we add the event's TimeRange to a list.
    events.forEach(
        event -> {
          Collection<String> eventAttendees = event.getAttendees();

          for (String attendee : eventAttendees) {
            if (!attendeeSet.contains(attendee)) {
              continue;
            }

            TimeRange currentEventTimeRange = event.getWhen();

            mergeOnOverlap(unavailableTimeRanges, currentEventTimeRange);

            break;
          }
        });

    return unavailableTimeRanges;
  }

  /**
   * Returns whether a merge has ocurred or not and modifies the Collection of TimeRanges to include
   * a merged TimeRange where the newTimeRange overlaps with the passed Collection.
   */
  private void mergeOnOverlap(Collection<TimeRange> timeRanges, TimeRange newTimeRange) {
    // Store items that we will remove from the collection after we finish iterating
    // through it in order to avoid ConcurrentModificationException from being thrown.
    List<TimeRange> toRemove = new ArrayList<>();

    // Iterate through each TimeRange in timeRanges and if any overlap with the
    // the newTimeRange, then merge them together, store the merged TimeRange as the
    // newTimeRange, and remove the timeRange from timeRanges that was merged.
    for (TimeRange takenTimeRange : timeRanges) {
      if (newTimeRange.overlaps(takenTimeRange)) {
        newTimeRange = mergeTimeRanges(newTimeRange, takenTimeRange);
        toRemove.add(takenTimeRange);
      }
    }

    for (TimeRange timeRange : toRemove) {
      timeRanges.remove(timeRange);
    }

    // Add the newTimeRange either as the original value that was passed in, or merged with
    // overlapping ranges.
    timeRanges.add(newTimeRange);
  }

  private TimeRange mergeTimeRanges(TimeRange firstTimeRange, TimeRange secondTimeRange) {
    TimeRange mergedTimeRange =
        TimeRange.fromStartEnd(
            Math.min(firstTimeRange.start(), secondTimeRange.start()),
            Math.max(firstTimeRange.end(), secondTimeRange.end()),
            /* inclusive = */ false);

    return mergedTimeRange;
  }

  /**
   * Returns available timeRanges as a List by sorting a copy of unavailableTimeRanges by start time
   * and adding each gap between each timeRange to the List which will be returned. The List that
   * was passed in will not be modified.
   */
  private List<TimeRange> getAvailableTimeRanges(
      List<TimeRange> unavailableTimeRanges, long durationMinutes) {
    if (unavailableTimeRanges.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    List<TimeRange> unavailableTimeRangesCopy = new ArrayList<>(unavailableTimeRanges);
    Collections.sort(unavailableTimeRangesCopy, TimeRange.ORDER_BY_START);

    List<TimeRange> availableTimeRanges = new ArrayList<>();
    int lastListIndex = unavailableTimeRangesCopy.size() - 1;

    // Check for available time starting at the beginning of the day.
    if (unavailableTimeRangesCopy.get(0).start() >= durationMinutes) {
      availableTimeRanges.add(
          TimeRange.fromStartEnd(
              TimeRange.WHOLE_DAY.start(),
              unavailableTimeRangesCopy.get(0).start(),
              /* inclusive = */ false));
    }

    for (int i = 0; i < lastListIndex; i++) {
      if (enoughTimeBetween(
          unavailableTimeRangesCopy.get(i),
          unavailableTimeRangesCopy.get(i + 1),
          durationMinutes)) {
        availableTimeRanges.add(
            TimeRange.fromStartEnd(
                unavailableTimeRangesCopy.get(i).end(),
                unavailableTimeRangesCopy.get(i + 1).start(),
                /* inclusive = */ false));
      }
    }

    // Check for available time at the end of the day.
    int timeAfterLastMeeting =
        TimeRange.WHOLE_DAY.end() - unavailableTimeRangesCopy.get(lastListIndex).end();

    if (timeAfterLastMeeting >= durationMinutes) {
      availableTimeRanges.add(
          TimeRange.fromStartEnd(
              unavailableTimeRangesCopy.get(lastListIndex).end(),
              TimeRange.WHOLE_DAY.end(),
              /* inclusive = */ false));
    }

    return availableTimeRanges;
  }

  private boolean enoughTimeBetween(TimeRange firstTime, TimeRange nextTime, long durationMinutes) {
    return (nextTime.start() - firstTime.end()) >= durationMinutes;
  }
}
