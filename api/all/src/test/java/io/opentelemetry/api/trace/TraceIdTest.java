/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceId}. */
class TraceIdTest {
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes =
      new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'A'};
  private static final String first = TraceId.fromBytes(firstBytes);

  private static final String second =
      TraceId.fromLongs(
          ByteBuffer.wrap(secondBytes).getLong(), ByteBuffer.wrap(secondBytes, 8, 8).getLong());

  @Test
  void invalid() {
    assertThat(TraceId.getInvalid()).isEqualTo("00000000000000000000000000000000");
    assertThat(TraceId.asBytes(TraceId.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(TraceId.highPartAsLong(TraceId.getInvalid())).isEqualTo(0);
    assertThat(TraceId.lowPartAsLong(TraceId.getInvalid())).isEqualTo(0);
  }

  @Test
  void isValid() {
    assertThat(TraceId.isValid(TraceId.getInvalid())).isFalse();
    assertThat(TraceId.isValid(first)).isTrue();
    assertThat(TraceId.isValid(second)).isTrue();

    assertThat(TraceId.isValid("000000000000004z0000000000000016")).isFalse();
    assertThat(TraceId.isValid("001")).isFalse();
  }

  @Test
  void testGetRandomTracePart() {
    byte[] id = {
      0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x00
    };
    String traceId = TraceId.fromBytes(id);
    assertThat(TraceId.getTraceIdRandomPart(traceId)).isEqualTo(0x090A0B0C0D0E0F00L);
  }

  @Test
  void testGetRandomTracePart_NegativeLongRepresentation() {
    byte[] id = {
      (byte) 0xFF, // force a negative value
      0x01,
      0x02,
      0x03,
      0x04,
      0x05,
      0x06,
      0x00,
      (byte) 0xFF, // force a negative value
      0x0A,
      0x0B,
      0x0C,
      0x0D,
      0x0E,
      0x0F,
      0x00
    };
    String traceId = TraceId.fromBytes(id);
    assertThat(TraceId.highPartAsLong(traceId)).isEqualTo(0xFF01020304050600L);
    assertThat(TraceId.lowPartAsLong(traceId)).isEqualTo(0xFF0A0B0C0D0E0F00L);
  }

  @Test
  void fromLowerHex() {
    assertThat(TraceId.fromBytes(TraceId.asBytes("00000000000000000000000000000000")))
        .isEqualTo(TraceId.getInvalid());
    assertThat(TraceId.asBytes("00000000000000000000000000000061")).isEqualTo(firstBytes);
    assertThat(TraceId.asBytes("ff000000000000000000000000000041")).isEqualTo(secondBytes);
  }

  @Test
  void toLowerHex() {
    assertThat(TraceId.getInvalid()).isEqualTo("00000000000000000000000000000000");
    assertThat(TraceId.fromBytes(firstBytes)).isEqualTo("00000000000000000000000000000061");
    assertThat(TraceId.fromBytes(secondBytes)).isEqualTo("ff000000000000000000000000000041");
  }

  @Test
  void toFromLongs() {
    Random random = new Random();
    for (int i = 0; i < 10000; i++) {
      long idHi = random.nextLong();
      long idLo = random.nextLong();
      String traceId = TraceId.fromLongs(idHi, idLo);
      assertThat(TraceId.highPartAsLong(traceId)).isEqualTo(idHi);
      assertThat(TraceId.lowPartAsLong(traceId)).isEqualTo(idLo);
    }
  }
}
