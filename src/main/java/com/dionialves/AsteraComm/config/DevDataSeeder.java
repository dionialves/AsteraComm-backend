package com.dionialves.AsteraComm.config;

import com.dionialves.AsteraComm.asterisk.aors.AorRepository;
import com.dionialves.AsteraComm.asterisk.aors.Aors;
import com.dionialves.AsteraComm.asterisk.auth.Auth;
import com.dionialves.AsteraComm.asterisk.auth.AuthRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.Endpoint;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointRepository;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatus;
import com.dionialves.AsteraComm.asterisk.endpoint.EndpointStatusRepository;
import com.dionialves.AsteraComm.asterisk.extension.Extension;
import com.dionialves.AsteraComm.asterisk.extension.ExtensionRepository;
import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.trunk.Trunk;
import com.dionialves.AsteraComm.trunk.TrunkRepository;
import com.dionialves.AsteraComm.user.User;
import com.dionialves.AsteraComm.user.UserRepository;
import com.dionialves.AsteraComm.user.UserRole;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Popula o banco de dados com dados de teste.
 * Executado apenas quando o profile "dev" está ativo.
 */
@RequiredArgsConstructor
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private static final int TOTAL_CIRCUITOS = 10;
    private static final double PERCENTUAL_ONLINE = 0.80;
    private static final String PREFIXO_NUMERO = "49334";

    private final CircuitRepository circuitRepository;
    private final TrunkRepository trunkRepository;
    private final AuthRepository authRepository;
    private final AorRepository aorRepository;
    private final EndpointRepository endpointRepository;
    private final EndpointStatusRepository endpointStatusRepository;
    private final UserRepository userRepository;
    private final ExtensionRepository extensionRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (circuitRepository.count() > 0) {
            log.info("Dados já existem no banco. Pulando seed de desenvolvimento.");
            return;
        }

        log.info("Iniciando seed de dados de desenvolvimento...");

        criarUsuarioAdmin();

        Trunk trunk = criarTronco();

        log.info("Criando {} circuitos vinculados ao tronco '{}'...", TOTAL_CIRCUITOS, trunk.getName());

        int onlineCount = 0;
        int offlineCount = 0;

        for (int i = 0; i < TOTAL_CIRCUITOS; i++) {
            String number = gerarNumeroCliente(i);
            String password = gerarSenhaAleatoria();
            boolean isOnline = random.nextDouble() < PERCENTUAL_ONLINE;

            Circuit circuit = new Circuit();
            circuit.setNumber(number);
            circuit.setPassword(password);
            circuit.setTrunkName(trunk.getName());
            circuitRepository.save(circuit);

            Auth auth = criarAuth(number, password);
            authRepository.save(auth);

            Aors aor = criarAors(number);
            aorRepository.save(aor);

            Endpoint endpoint = criarEndpoint(number, auth, aor, trunk.getName());
            endpointRepository.save(endpoint);

            criarExtensions(number, trunk.getName());

            EndpointStatus status = criarEndpointStatus(endpoint, isOnline);
            endpointStatusRepository.save(status);

            if (isOnline) onlineCount++;
            else offlineCount++;
        }

        log.info("Seed finalizado! Circuitos: {} | Online: {} | Offline: {}",
                TOTAL_CIRCUITOS, onlineCount, offlineCount);
    }

    private Trunk criarTronco() {
        if (trunkRepository.existsById("dev-operadora")) {
            return trunkRepository.findById("dev-operadora").orElseThrow();
        }

        Trunk trunk = new Trunk();
        trunk.setName("dev-operadora");
        trunk.setHost("sip.dev-operadora.local");
        trunk.setUsername("dev-user");
        trunk.setPassword("dev-senha");
        trunk.setPrefix(null);
        trunkRepository.save(trunk);

        log.info("Tronco fictício criado: '{}'", trunk.getName());
        return trunk;
    }

    private String gerarNumeroCliente(int indice) {
        return String.format("%s%05d", PREFIXO_NUMERO, indice + 1);
    }

    private String gerarSenhaAleatoria() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            senha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return senha.toString();
    }

    private String gerarIpAleatorio() {
        if (random.nextBoolean()) {
            return String.format("192.168.%d.%d", random.nextInt(256), random.nextInt(256));
        } else {
            return String.format("10.0.%d.%d", random.nextInt(256), random.nextInt(256));
        }
    }

    private String gerarRttAleatorio() {
        int rtt = 5 + random.nextInt(146);
        return rtt + "ms";
    }

    private Auth criarAuth(String number, String password) {
        Auth auth = new Auth();
        auth.setId(number);
        auth.setAuthType("userpass");
        auth.setUsername(number);
        auth.setPassword(password);
        return auth;
    }

    private Aors criarAors(String number) {
        Aors aor = new Aors();
        aor.setId(number);
        aor.setMaxContacts("1");
        aor.setRemoveExisting("yes");
        aor.setQualifyFrequency("60");
        aor.setDefaultExpiration("3600");
        return aor;
    }

    private Endpoint criarEndpoint(String number, Auth auth, Aors aors, String trunkName) {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(number);
        endpoint.setAors(aors);
        endpoint.setAuth(auth);
        endpoint.setContext("internal-" + trunkName);
        endpoint.setDisallow("all");
        endpoint.setAllow("ulaw,alaw");
        endpoint.setDirect_media("no");
        endpoint.setForce_rport("yes");
        endpoint.setRewriteContact("yes");
        endpoint.setRtpSymmetric("yes");
        endpoint.setCallerid(number);
        return endpoint;
    }

    private void criarExtensions(String number, String trunkName) {
        String pstnContext = "pstn-" + trunkName;

        Extension dial = new Extension();
        dial.setContext(pstnContext);
        dial.setExten(number);
        dial.setPriority(1);
        dial.setApp("Dial");
        dial.setAppdata("PJSIP/" + number + ",60");
        extensionRepository.save(dial);

        Extension hangup = new Extension();
        hangup.setContext(pstnContext);
        hangup.setExten(number);
        hangup.setPriority(2);
        hangup.setApp("Hangup");
        extensionRepository.save(hangup);
    }

    private void criarUsuarioAdmin() {
        if (userRepository.existsByUsername("admin@asteracomm.com")) {
            log.info("Usuário admin já existe.");
            return;
        }

        User admin = new User(
                "Administrador",
                "admin@asteracomm.com",
                "$2a$12$H3aKgAf.Q0eCN7UupcUAS.M5t/cGTmRb8fgznmtag8tFzfLed5tKe",
                UserRole.SUPER_ADMIN);

        userRepository.save(admin);
        log.info("Usuário admin criado: admin@asteracomm.com");
    }

    private EndpointStatus criarEndpointStatus(Endpoint endpoint, boolean isOnline) {
        EndpointStatus status = new EndpointStatus();
        status.setEndpoint(endpoint);
        status.setOnline(isOnline);
        status.setCheckedAt(LocalDateTime.now().minusMinutes(random.nextInt(60)));

        if (isOnline) {
            status.setIp(gerarIpAleatorio());
            status.setRtt(gerarRttAleatorio());
        } else {
            status.setIp(null);
            status.setRtt(null);
        }

        return status;
    }
}
