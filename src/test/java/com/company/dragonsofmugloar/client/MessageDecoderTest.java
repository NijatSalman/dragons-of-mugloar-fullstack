package com.company.dragonsofmugloar.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MessageDecoderTest {

    @Test
    void decodeReturnsPlainTextUnchanged() {
        assertThat(MessageDecoder.decode("Help Bob", null)).isEqualTo("Help Bob");
    }

    @Test
    void decodeDecodesBase64() {
        assertThat(MessageDecoder.decode("UGllY2Ugb2YgY2FrZQ==", MessageDecoder.BASE64)).isEqualTo("Piece of cake");
    }

    @ParameterizedTest
    @CsvSource({
            "Cvrpr bs pnxr, Piece of cake",
            "Fhvpvqr zvffvba, Suicide mission",
            "Uzzz...., Hmmm....",
            "nOp XyZ 123, aBc KlM 123"
    })
    void decodeDecodesRot13AndLeavesOtherCharactersAlone(String encoded, String expected) {
        assertThat(MessageDecoder.decode(encoded, MessageDecoder.ROT13)).isEqualTo(expected);
    }

    @Test
    void decodeReturnsTextUnchangedWhenEncodingIsUnknown() {
        assertThat(MessageDecoder.decode("whatever", 9)).isEqualTo("whatever");
    }

    @Test
    void decodeReturnsNullWhenTextIsNull() {
        assertThat(MessageDecoder.decode(null, MessageDecoder.BASE64)).isNull();
    }
}
