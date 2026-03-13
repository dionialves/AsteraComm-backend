package com.dionialves.AsteraComm.call;

import com.dionialves.AsteraComm.cdr.CdrRecord;
import com.dionialves.AsteraComm.cdr.CdrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CallProcessingService {

    private final CdrRepository cdrRepository;
    private final CallRepository callRepository;
    private final CallerIdParser callerIdParser;
    private final CallTypeClassifier callTypeClassifier;

    @Scheduled(fixedRateString = "${call.processing.interval.ms}")
    public void process() {
        List<CdrRecord> unprocessed = cdrRepository.findUnprocessed();
        for (CdrRecord cdr : unprocessed) {
            Call call = new Call();
            call.setUniqueId(cdr.getUniqueId());
            call.setCallDate(cdr.getCalldate());
            call.setCallerNumber(callerIdParser.parse(cdr.getClid()));
            call.setDst(cdr.getDst());
            call.setDurationSeconds(cdr.getDuration());
            call.setBillSeconds(cdr.getBillsec());
            call.setDisposition(cdr.getDisposition());
            call.setCallType(callTypeClassifier.classify(cdr.getDst()));
            call.setProcessedAt(LocalDateTime.now());
            callRepository.save(call);
        }
    }
}
