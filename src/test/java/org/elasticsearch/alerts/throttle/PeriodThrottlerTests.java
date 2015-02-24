/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.alerts.throttle;

import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.elasticsearch.alerts.Alert;
import org.elasticsearch.alerts.ExecutionContext;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.PeriodType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.test.ElasticsearchTestCase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class PeriodThrottlerTests extends ElasticsearchTestCase {

    @Test @Repeat(iterations = 10)
    public void testBelowPeriod() throws Exception {
        PeriodType periodType = randomFrom(PeriodType.millis(), PeriodType.seconds(), PeriodType.minutes());
        TimeValue period = TimeValue.timeValueSeconds(randomIntBetween(2, 5));
        PeriodThrottler throttler = new PeriodThrottler(period, periodType);

        ExecutionContext ctx = mock(ExecutionContext.class);
        Alert alert = mock(Alert.class);
        Alert.Status status = mock(Alert.Status.class);
        when(ctx.alert()).thenReturn(alert);
        when(alert.status()).thenReturn(status);
        when(status.lastExecuted()).thenReturn(new DateTime().minusSeconds((int) period.seconds() - 1));

        Throttler.Result result = throttler.throttle(ctx);
        assertThat(result, notNullValue());
        assertThat(result.throttle(), is(true));
        assertThat(result.reason(), notNullValue());
        assertThat(result.reason(), startsWith("throttling interval is set to [" + period.format(periodType) + "]"));
    }

    @Test @Repeat(iterations = 10)
    public void testAbovePeriod() throws Exception {
        PeriodType periodType = randomFrom(PeriodType.millis(), PeriodType.seconds(), PeriodType.minutes());
        TimeValue period = TimeValue.timeValueSeconds(randomIntBetween(2, 5));
        PeriodThrottler throttler = new PeriodThrottler(period, periodType);

        ExecutionContext ctx = mock(ExecutionContext.class);
        Alert alert = mock(Alert.class);
        Alert.Status status = mock(Alert.Status.class);
        when(ctx.alert()).thenReturn(alert);
        when(alert.status()).thenReturn(status);
        when(status.lastExecuted()).thenReturn(new DateTime().minusSeconds((int) period.seconds() + 1));

        Throttler.Result result = throttler.throttle(ctx);
        assertThat(result, notNullValue());
        assertThat(result.throttle(), is(false));
        assertThat(result.reason(), nullValue());
    }

}
