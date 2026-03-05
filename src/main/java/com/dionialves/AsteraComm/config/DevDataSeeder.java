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

    private static final int TOTAL_REGISTROS = 100;
    private static final double PERCENTUAL_ONLINE = 0.80;
    private static final String PREFIXO_NUMERO = "49334"; // 493 + 34 (DDD Florianópolis)

    private final CircuitRepository circuitRepository;
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

        log.info("Criando {} registros ({}% online)...", TOTAL_REGISTROS, (int) (PERCENTUAL_ONLINE * 100));

        int onlineCount = 0;
        int offlineCount = 0;

        for (int i = 0; i < TOTAL_REGISTROS; i++) {
            String number = gerarNumeroCliente(i);
            String password = gerarSenhaAleatoria();
            boolean isOnline = random.nextDouble() < PERCENTUAL_ONLINE;

            // 1. Circuit (entidade de domínio)
            Circuit circuit = new Circuit();
            circuit.setNumber(number);
            circuit.setPassword(password);
            circuitRepository.save(circuit);

            // 2. Auth (PJSIP)
            Auth auth = criarAuth(number, password);
            authRepository.save(auth);

            // 3. Aors (PJSIP)
            Aors aor = criarAors(number);
            aorRepository.save(aor);

            // 4. Endpoint (PJSIP)
            Endpoint endpoint = criarEndpoint(number, auth, aor);
            endpointRepository.save(endpoint);

            // 5. Extensions
            criarExtensions(endpoint);

            // 6. EndpointStatus (simulação dev)
            EndpointStatus status = criarEndpointStatus(endpoint, isOnline);
            endpointStatusRepository.save(status);

            if (isOnline) onlineCount++;
            else offlineCount++;
        }

        log.info("Seed finalizado com sucesso!");
        log.info("Total: {} registros | Online: {} | Offline: {}",
                TOTAL_REGISTROS, onlineCount, offlineCount);
    }

    private String gerarNumeroCliente(int indice) {
        return String.format("%s%05d", PREFIXO_NUMERO, indice + 1);
    }

    private String gerarSenhaAleatoria() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 8; i++) {
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

    private Endpoint criarEndpoint(String number, Auth auth, Aors aors) {
        Endpoint endpoint = new Endpoint();
        endpoint.setId(number);
        endpoint.setAors(aors);
        endpoint.setAuth(auth);
        return endpoint;
    }

    private void criarExtensions(Endpoint endpoint) {
        Extension extension1 = new Extension();
        extension1.setContext("from-pstn");
        extension1.setExten(endpoint);
        extension1.setPriority(1);
        extension1.setApp("Dial");
        extension1.setAppdata("PJSIP/" + endpoint.getId() + ",60");
        extensionRepository.save(extension1);

        Extension extension2 = new Extension();
        extension2.setContext("from-pstn");
        extension2.setExten(endpoint);
        extension2.setPriority(2);
        extension2.setApp("Hangup");
        extensionRepository.save(extension2);
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
