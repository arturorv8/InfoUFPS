package org.infoufps.application.account;

import jakarta.transaction.Transactional;
import org.infoufps.application.ApplicationUseCasesTest;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.shared.exceptions.DomainException;
import org.infoufps.domain.shared.exceptions.EmailAlreadyExistsException;
import org.infoufps.application.account.RegisterAccountCommand;
import org.infoufps.application.account.RegisterAccountHandler;
import org.infoufps.application.account.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RegisterAccountHandler: normalización y validación de email")
class AccountRegistrationHandlerTest {

    private ApplicationUseCasesTest.InMemoryAccountRepository accountRepository;
    private RegisterAccountHandler handler;

    @BeforeEach
    void setUp() {
        accountRepository = new ApplicationUseCasesTest.InMemoryAccountRepository();
        handler = new RegisterAccountHandler(accountRepository, new FakePasswordHasher());
    }

    @Test
    @DisplayName("Normaliza el email antes de persistir la cuenta")
    void normalizesEmail() {
        Account account = handler.execute(new RegisterAccountCommand(
                "  Usuario@UFPS.EDU.CO ", "password123", "Usuario"));

        assertEquals("usuario@ufps.edu.co", account.getEmail());
        assertEquals("usuario@ufps.edu.co", accountRepository.findByEmail("USUARIO@ufps.edu.co").orElseThrow().getEmail());
    }

    @Test
    @DisplayName("Rechaza un email duplicado sin distinguir mayúsculas")
    void rejectsDuplicateEmailIgnoringCase() {
        handler.execute(new RegisterAccountCommand("usuario@ufps.edu.co", "password123", "Usuario"));

        assertThrows(EmailAlreadyExistsException.class, () -> handler.execute(
                new RegisterAccountCommand("USUARIO@UFPS.EDU.CO", "password123", "Otro")));
    }

    @Test
    @DisplayName("Rechaza un email con formato inválido")
    void rejectsInvalidEmailFormat() {
        assertThrows(DomainException.class, () -> handler.execute(
                new RegisterAccountCommand("correo-invalido", "password123", "Usuario")));
    }

    @Test
    @DisplayName("La operación de registro es transaccional")
    void executeIsTransactional() throws NoSuchMethodException {
        Method method = RegisterAccountHandler.class.getMethod("execute", RegisterAccountCommand.class);

        assertNotNull(method.getAnnotation(Transactional.class));
    }

    static class FakePasswordHasher implements PasswordHasher {
        @Override public String hash(String password) { return "hash:" + password; }
        @Override public boolean verify(String password, String hash) { return hash.equals(hash(password)); }
    }
}
