package org.infoufps.application.account;

/**
 * Puerto de salida para verificar tokens de acceso.
 */
public interface AccessTokenVerifier {

    /**
     * Verifica la firma y las claims del token.
     *
     * @param token token JWT recibido.
     * @return identificador de la cuenta autenticada.
     * @throws org.infoufps.domain.shared.exceptions.UnauthorizedException si el
     *         token es inválido, está vencido o no contiene un sujeto.
     */
    String verify(String token);
}
