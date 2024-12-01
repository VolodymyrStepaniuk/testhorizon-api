package com.stepaniuk.testhorizon.testspecific.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.time.temporal.Temporal;
import java.util.function.Function;

public class TemporalStringMatcher<T extends Temporal> extends TypeSafeMatcher<String> {

  private final Function<String, T> parser;
  private final Matcher<T> matcher;

  public TemporalStringMatcher(Function<String, T> parser, Matcher<T> matcher) {
    this.parser = parser;
    this.matcher = matcher;
  }

  @Override
  protected boolean matchesSafely(String item) {
    try {
      return this.matcher.matches(this.parser.apply(item));
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  protected void describeMismatchSafely(String item, Description mismatchDescription) {
    T parsedTemporal = null;
    try {
      parsedTemporal = this.parser.apply(item);
    } catch (Exception e) {
      mismatchDescription.appendText("was not a parsable temporal (").appendValue(item)
          .appendText(")").appendText(" because we got an exception: ").appendValue(e);
      if (e.getCause() != null) {
        mismatchDescription.appendText(" and cause: ").appendValue(e.getCause());
      }
      return;
    }

    if (parsedTemporal == null) {
      mismatchDescription.appendText("got null instead of temporal for value (").appendValue(item)
          .appendText(")");
    }
    this.matcher.describeMismatch(parsedTemporal, mismatchDescription);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a string representing a temporal that is ")
        .appendDescriptionOf(this.matcher);
  }
}
