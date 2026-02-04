package com.dionialves.AsteraComm.config;

import com.dionialves.AsteraComm.entity.Aor;
import com.dionialves.AsteraComm.entity.Auth;
import com.dionialves.AsteraComm.entity.Endpoint;
import com.dionialves.AsteraComm.entity.EndpointStatus;
import com.dionialves.AsteraComm.repository.AorRepository;
import com.dionialves.AsteraComm.repository.AuthRepository;
import com.dionialves.AsteraComm.repository.EndpointRepository;
import com.dionialves.AsteraComm.repository.EndpointStatusRepository;

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
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private static final int TOTAL_REGISTROS = 100;
    private static final double PERCENTUAL_ONLINE = 0.80;
    private static final String PREFIXO_NUMERO = "49334";  // 493 + 34 (DDD Florianópolis)

    private final AuthRepository authRepository;
    private final AorRepository aorRepository;
    private final EndpointRepository endpointRepository;
    private final EndpointStatusRepository endpointStatusRepository;

    private final Random random = new Random();

    public DevDataSeeder(
            AuthRepository authRepository,
            AorRepository aorRepository,
            EndpointRepository endpointRepository,
            EndpointStatusRepository endpointStatusRepository) {
        this.authRepository = authRepository;
        this.aorRepository = aorRepository;
        this.endpointRepository = endpointRepository;
        this.endpointStatusRepository = endpointStatusRepository;
    }

    @Override
    public void run(String... args) {
        if (endpointRepository.count() > 0) {
            log.info("Dados já existem no banco. Pulando seed de desenvolvimento.");
            return;
        }

        log.info("Iniciando seed de dados de desenvolvimento...");
        log.info("Criando {} registros ({}% online)...", TOTAL_REGISTROS, (int)(PERCENTUAL_ONLINE * 100));

        int onlineCount = 0;
        int offlineCount = 0;

        for (int i = 0; i < TOTAL_REGISTROS; i++) {
            String id = gerarNumeroCliente(i);
            boolean isOnline = random.nextDouble() < PERCENTUAL_ONLINE;

            // 1. Criar Auth
            Auth auth = criarAuth(id);
            authRepository.save(auth);

            // 2. Criar Aor
            Aor aor = criarAor(id);
            aorRepository.save(aor);

            // 3. Criar Endpoint
            Endpoint endpoint = criarEndpoint(id, auth);
            endpointRepository.save(endpoint);

            // 4. Criar EndpointStatus
            EndpointStatus status = criarEndpointStatus(endpoint, isOnline);
            endpointStatusRepository.save(status);

            if (isOnline) {
                onlineCount++;
            } else {
                offlineCount++;
            }
        }

        log.info("Seed finalizado com sucesso!");
        log.info("Total: {} registros | Online: {} | Offline: {}",
                TOTAL_REGISTROS, onlineCount, offlineCount);
    }

    /**
     * Gera número do cliente no padrão 493XXXXXXX
     * Exemplo: 4933400001, 4933400002, etc.
     */
    private String gerarNumeroCliente(int indice) {
        // PREFIXO_NUMERO (49334) + 5 dígitos sequenciais com padding
        return String.format("%s%05d", PREFIXO_NUMERO, indice + 1);
    }

    /**
     * Gera senha aleatória de 8 caracteres
     */
    private String gerarSenhaAleatoria() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            senha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return senha.toString();
    }

    /**
     * Gera IP aleatório na faixa 192.168.x.x ou 10.0.x.x
     */
    private String gerarIpAleatorio() {
        if (random.nextBoolean()) {
            return String.format("192.168.%d.%d", random.nextInt(256), random.nextInt(256));
        } else {
            return String.format("10.0.%d.%d", random.nextInt(256), random.nextInt(256));
        }
    }

    /**
     * Gera RTT aleatório entre 5ms e 150ms
     */
    private String gerarRttAleatorio() {
        int rtt = 5 + random.nextInt(146);  // 5 a 150
        return rtt + "ms";
    }

    private Auth criarAuth(String id) {
        Auth auth = new Auth(id);
        auth.setUsername(id);
        auth.setPassword(gerarSenhaAleatoria());
        return auth;
    }

    private Aor criarAor(String id) {
        Aor aor = new Aor(id);
        aor.setMaxContacts(1);
        aor.setRemoveExisting("yes");
        aor.setQualifyFrequency(60);
        aor.setDefaultExpiration(3600);
        aor.setMinimumExpiration(60);
        aor.setMaximumExpiration(7200);
        return aor;
    }

    private Endpoint criarEndpoint(String id, Auth auth) {
        Endpoint endpoint = new Endpoint(id, auth, id);
        return endpoint;
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
