package io.kestra.plugin.jdbc.pinot;

import com.google.common.collect.ImmutableMap;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.jdbc.AbstractJdbcQuery;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * See :
 * - https://docs.pinot.apache.org/configuration-reference/schema
 */
@MicronautTest
public class PinotTest {
    @Inject
    RunContextFactory runContextFactory;

    @Test
    void select() throws Exception {
        RunContext runContext = runContextFactory.of(ImmutableMap.of());

        Query task = Query.builder()
            .url("jdbc:pinot://localhost:49000")
            .fetchOne(true)
            .timeZoneId("Europe/Paris")
            .sql("select \n" +
                "  -- NULL as t_null,\n" +
                "  'string' AS t_string,\n" +
                "  CAST(2147483647 AS INT) as t_integer,\n" +
                "  CAST(9223372036854775807 AS LONG) as t_long,\n" +
                "  CAST(12345.124 AS FLOAT) as t_float,\n" +
                "  CAST(12345.124 AS DOUBLE) as t_double,\n" +
                "  CAST(123.45600 AS BIG_DECIMAL) as t_bigdecimal,\n" +
                "  ST_GeogFromText('LINESTRING (30 10, 10 30, 40 40)') as t_geo,\n" +
                "  ToDateTime(1639137263000, 'yyyy-MM-dd')  as t_date,\n" +
                "  ToEpochSeconds(1613472303000) AS t_epoch\n" +
                "  \n" +
                "from airlineStats \n" +
                "limit 1")
            .build();

        AbstractJdbcQuery.Output runOutput = task.run(runContext);
        assertThat(runOutput.getRow(), notNullValue());

        assertThat(runOutput.getRow().get("t_null"), is(nullValue()));
        assertThat(runOutput.getRow().get("t_string"), is("string"));
        assertThat(runOutput.getRow().get("t_integer"), is(2147483647L));
        assertThat(runOutput.getRow().get("t_long"), is(9223372036854775807L));
        assertThat(runOutput.getRow().get("t_float"), is(12345.1240234375D));
        assertThat(runOutput.getRow().get("t_double"), is(12345.124D));
        assertThat(runOutput.getRow().get("t_bigdecimal"), is("123.456"));
        assertThat(runOutput.getRow().get("t_geo"), is("82000000010000000300000000403e00000000000040240000000000004024000000000000403e00000000000040440000000000004044000000000000"));
        assertThat(runOutput.getRow().get("t_date"), is("2021-12-10"));
        assertThat(runOutput.getRow().get("t_epoch"), is(1613472303L));
    }
}
