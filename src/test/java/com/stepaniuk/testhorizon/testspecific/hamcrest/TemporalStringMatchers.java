package com.stepaniuk.testhorizon.testspecific.hamcrest;

import org.hamcrest.Matcher;
import org.hamcrest.number.OrderingComparison;

import java.time.Instant;
import java.util.function.Function;

public class TemporalStringMatchers {

  public static TemporalStringMatcher<Instant> instantThat(Matcher<Instant> instantMatcher) {
    return new TemporalStringMatcher<>(Instant::parse, instantMatcher);
  }

  public static TemporalStringMatcher<Instant> instantThat(Matcher<Instant> instantMatcher,
      Function<String, Instant> parser) {
    return new TemporalStringMatcher<>(parser, instantMatcher);
  }

  public static TemporalStringMatcher<Instant> instantComparesEqualTo(Instant instant) {
    return new TemporalStringMatcher<>(Instant::parse, OrderingComparison.comparesEqualTo(instant));
  }

  public static TemporalStringMatcher<Instant> instantComparesEqualTo(Instant instant,
      Function<String, Instant> parser) {
    return new TemporalStringMatcher<>(parser, OrderingComparison.comparesEqualTo(instant));
  }
}
