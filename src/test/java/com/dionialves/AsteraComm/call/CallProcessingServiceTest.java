package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.cdr.CdrRecord;
import com.dionialves.AsteraComm.cdr.CdrRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallProcessingServiceTest {

    @Mock
    private CdrRepository cdrRepository;

    @Mock
    private CallRepository callRepository;

    @Mock
    private CallerIdParser callerIdParser;

    @Mock
    private CallTypeClassifier callTypeClassifier;

    @InjectMocks
    private CallProcessingService callProcessingService;

    private CdrRecord buildCdr(String uniqueId, String disposition) {
        CdrRecord cdr = new CdrRecord();
        cdr.setUniqueId(uniqueId);
        cdr.setCalldate(LocalDateTime.of(2026, 3, 12, 10, 0, 0));
        cdr.setClid("\"Cliente\" <11933334444>");
        cdr.setSrc("11933334444");
        cdr.setDst("1133334444");
        cdr.setDuration(120);
        cdr.setBillsec(115);
        cdr.setDisposition(disposition);
        return cdr;
    }

    @Test
    void process_shouldCreateCall_forAnsweredCdr() {
        CdrRecord cdr = buildCdr("1000.1", "ANSWERED");
        when(cdrRepository.findUnprocessed()).thenReturn(List.of(cdr));
        when(callerIdParser.parse("\"Cliente\" <11933334444>")).thenReturn("11933334444");
        when(callTypeClassifier.classify("1133334444")).thenReturn(CallType.FIXED_LONG_DISTANCE);

        callProcessingService.process();

        ArgumentCaptor<Call> captor = ArgumentCaptor.forClass(Call.class);
        verify(callRepository).save(captor.capture());
        Call saved = captor.getValue();
        assertThat(saved.getUniqueId()).isEqualTo("1000.1");
        assertThat(saved.getCallerNumber()).isEqualTo("11933334444");
        assertThat(saved.getDst()).isEqualTo("1133334444");
        assertThat(saved.getCallType()).isEqualTo(CallType.FIXED_LONG_DISTANCE);
        assertThat(saved.getDisposition()).isEqualTo("ANSWERED");
        assertThat(saved.getDurationSeconds()).isEqualTo(120);
        assertThat(saved.getBillSeconds()).isEqualTo(115);
    }

    @Test
    void process_shouldCreateCall_forAllDispositions() {
        CdrRecord noAnswer = buildCdr("1000.2", "NO ANSWER");
        CdrRecord busy = buildCdr("1000.3", "BUSY");
        CdrRecord failed = buildCdr("1000.4", "FAILED");
        when(cdrRepository.findUnprocessed()).thenReturn(List.of(noAnswer, busy, failed));
        when(callerIdParser.parse(any())).thenReturn("11933334444");
        when(callTypeClassifier.classify(any())).thenReturn(CallType.FIXED_LONG_DISTANCE);

        callProcessingService.process();

        verify(callRepository, times(3)).save(any(Call.class));
    }

    @Test
    void process_shouldNotSaveAnything_whenAllCdrsAlreadyProcessed() {
        when(cdrRepository.findUnprocessed()).thenReturn(List.of());

        callProcessingService.process();

        verify(callRepository, never()).save(any());
    }
}
