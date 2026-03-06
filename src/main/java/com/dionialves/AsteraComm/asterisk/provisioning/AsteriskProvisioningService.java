package com.dionialves.AsteraComm.asterisk.provisioning;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatusRepository;
import com.dionialves.AsteraComm.asterisk.extension.Extension;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.asterisk.registration.PsRegistration;
import com.dionialves.AsteraComm.asterisk.registration.PsRegistrationRepository;
import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.trunk.Trunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AsteriskProvisioningService {

    private final AorRepository aorRepository;
    private final AuthRepository authRepository;
    private final EndpointRepository endpointRepository;
    private final ExtensionRepository extensionRepository;
    private final EndpointStatusRepository endpointStatusRepository;
    private final PsRegistrationRepository psRegistrationRepository;
    private final AmiService amiService;

    @Transactional
    public void provision(Circuit circuit) {
        String number = circuit.getNumber();

        Aors aors = new Aors();
        aors.setId(number);
        aors.setDefaultExpiration("60");
        aors.setMaxContacts("1");
        aors.setRemoveExisting("yes");
        aors.setQualifyFrequency("30");
        aorRepository.save(aors);

        Auth auth = new Auth();
        auth.setId(number);
        auth.setAuthType("userpass");
        auth.setUsername(number);
        auth.setPassword(circuit.getPassword());
        authRepository.save(auth);

        Endpoint endpoint = new Endpoint();
        endpoint.setId(number);
        endpoint.setAors(aors);
        endpoint.setAuth(auth);
        endpoint.setContext("from-internal");
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDirect_media("no");
        endpoint.setForce_rport("yes");
        endpoint.setRewriteContact("yes");
        endpoint.setRtpSymmetric("yes");
        endpoint.setCallerid(number);
        endpointRepository.save(endpoint);

        Extension extension1 = new Extension();
        extension1.setContext("from-pstn");
        extension1.setExten(endpoint);
        extension1.setPriority(1);
        extension1.setApp("Dial");
        extension1.setAppdata("PJSIP/" + number + ",60");
        extensionRepository.save(extension1);

        Extension extension2 = new Extension();
        extension2.setContext("from-pstn");
        extension2.setExten(endpoint);
        extension2.setPriority(2);
        extension2.setApp("Hangup");
        extensionRepository.save(extension2);

        amiService.sendCommand("pjsip reload");
    }

    @Transactional
    public void reprovision(Circuit circuit) {
        authRepository.findById(circuit.getNumber()).ifPresent(auth -> {
            auth.setPassword(circuit.getPassword());
            authRepository.save(auth);
        });
        amiService.sendCommand("pjsip reload");
    }

    @Transactional
    public void deprovision(Circuit circuit) {
        endpointRepository.findById(circuit.getNumber()).ifPresent(endpoint -> {
            extensionRepository.deleteByExten(endpoint);
            endpointStatusRepository.deleteByEndpoint(endpoint);
            endpointRepository.delete(endpoint);
            authRepository.delete(endpoint.getAuth());
            aorRepository.delete(endpoint.getAors());
            amiService.sendCommand("pjsip reload");
            amiService.sendCommand("dialplan reload");
        });
    }

    @Transactional
    public void provisionTrunk(Trunk trunk) {
        String name = trunk.getName();
        String host = trunk.getHost();

        Aors aors = new Aors();
        aors.setId(name);
        aors.setContact("sip:" + host);
        aors.setQualifyFrequency("30");
        aorRepository.save(aors);

        Auth auth = new Auth();
        auth.setId(name);
        auth.setAuthType("userpass");
        auth.setUsername(trunk.getUsername());
        auth.setPassword(trunk.getPassword());
        authRepository.save(auth);

        Endpoint endpoint = new Endpoint();
        endpoint.setId(name);
        endpoint.setAors(aors);
        endpoint.setContext("from-external");
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDirect_media("no");
        endpoint.setOutboundAuth(name);
        endpointRepository.save(endpoint);

        PsRegistration registration = new PsRegistration();
        registration.setId(name);
        registration.setServerUri("sip:" + host);
        registration.setClientUri("sip:" + trunk.getUsername() + "@" + host);
        registration.setOutboundAuth(name);
        registration.setRetryInterval("60");
        psRegistrationRepository.save(registration);

        amiService.sendCommand("pjsip reload");
    }

    @Transactional
    public void reprovisionTrunk(Trunk trunk) {
        String name = trunk.getName();
        String host = trunk.getHost();

        authRepository.findById(name).ifPresent(auth -> {
            auth.setPassword(trunk.getPassword());
            authRepository.save(auth);
        });

        psRegistrationRepository.findById(name).ifPresent(reg -> {
            reg.setServerUri("sip:" + host);
            reg.setClientUri("sip:" + trunk.getUsername() + "@" + host);
            psRegistrationRepository.save(reg);
        });

        amiService.sendCommand("pjsip reload");
    }

    @Transactional
    public void deprovisionTrunk(Trunk trunk) {
        String name = trunk.getName();

        psRegistrationRepository.findById(name).ifPresent(psRegistrationRepository::delete);
        endpointRepository.findById(name).ifPresent(endpointRepository::delete);
        authRepository.findById(name).ifPresent(authRepository::delete);
        aorRepository.findById(name).ifPresent(aorRepository::delete);

        amiService.sendCommand("pjsip reload");
    }
}
