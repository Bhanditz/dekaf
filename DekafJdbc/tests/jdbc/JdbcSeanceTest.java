package org.jetbrains.dekaf.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dekaf.inter.InterLayout;
import org.jetbrains.dekaf.inter.InterTask;
import org.jetbrains.dekaf.util.JavaPrimitiveKind;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.dekaf.core.TaskKind.TASK_QUERY;
import static org.jetbrains.dekaf.inter.InterResultKind.RES_EXISTENCE;
import static org.jetbrains.dekaf.inter.InterResultKind.RES_ONE_ROW;
import static org.jetbrains.dekaf.inter.InterRowKind.*;
import static org.jetbrains.dekaf.jdbc.H2mem.hmInSession;
import static org.junit.jupiter.api.Assertions.assertNotNull;



public final class JdbcSeanceTest {

    @Test
    public void existence_0() {
        InterTask task = new InterTask(TASK_QUERY, "select 12345 where 1 is null");
        InterLayout layout = new InterLayout(RES_EXISTENCE, ROW_NONE, null, null);
        Serializable result = queryPortion(task, layout);
        assertThat(result).isInstanceOf(Boolean.class);
        Boolean b = (Boolean) result;
        assertThat(b).isFalse();
    }

    @Test
    public void existence_1() {
        InterTask task = new InterTask(TASK_QUERY, "select 12345");
        InterLayout layout = new InterLayout(RES_EXISTENCE, ROW_NONE, null, null);
        Serializable result = queryPortion(task, layout);
        assertThat(result).isInstanceOf(Boolean.class);
        Boolean b = (Boolean) result;
        assertThat(b).isTrue();
    }

    @Test
    public void select_123_asSingleValue() {
        InterTask task = new InterTask(TASK_QUERY, "select 123");
        InterLayout layout = new InterLayout(RES_ONE_ROW, ROW_ONE_VALUE, null, Number.class);
        Serializable result = queryPortion(task, layout);
        assertThat(result).isInstanceOf(Number.class);
        int v = ((Number)result).intValue();
        assertThat(v).isEqualTo(123);
    }

    @Test
    public void select_123_asRowOfNumber() {
        InterTask task = new InterTask(TASK_QUERY, "select 123");
        InterLayout layout = new InterLayout(RES_ONE_ROW, ROW_OBJECTS, null, Number.class);
        Serializable result = queryPortion(task, layout);
        assertThat(result).isInstanceOf(Number[].class);
        Number[] row = (Number[]) result;
        assertThat(row).hasSize(1);
        Number number = row[0];
        assertThat(number.intValue()).isEqualTo(123);
    }

    @Test
    public void select_123_asRowOfShort() {
        InterTask task = new InterTask(TASK_QUERY, "select 123");
        InterLayout layout = new InterLayout(RES_ONE_ROW, ROW_PRIMITIVES, JavaPrimitiveKind.JAVA_SHORT, null);
        Serializable result = queryPortion(task, layout);
        assertThat(result).isInstanceOf(short[].class);
        short[] row = (short[]) result;
        assertThat(row).hasSize(1);
        short value = row[0];
        assertThat(value).isEqualTo((short)123);
    }


    @NotNull
    private static Serializable queryPortion(final @NotNull InterTask task,
                                             final @NotNull InterLayout layout) {
        Object result =
                hmInSession(session -> {
                    final JdbcSeance seance = session.openSeance();
                    seance.prepare(task);
                    seance.execute();
                    JdbcCursor cursor = seance.openCursor((byte) 0, layout);
                    assertNotNull(cursor);
                    Object portion = cursor.retrievePortion();
                    cursor.close();
                    seance.close();
                    return portion;
                });
        assertThat(result).isNotNull().isInstanceOf(Serializable.class);
        return (Serializable) result;
    }


}
