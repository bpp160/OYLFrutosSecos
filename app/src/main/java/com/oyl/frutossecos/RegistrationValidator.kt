package com.oyl.frutossecos

/**
 * Reglas de validación separadas de la interfaz Compose.
 * Devuelve errores por campo para mostrarlos en la pantalla.
 */
object RegistrationValidator {
    fun validate(name: String, alias: String, password: String, phone: String, address: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (name.trim().length < 3) errors["name"] = "Ingresa un nombre de al menos 3 caracteres"
        if (!Regex("^[a-zA-Z0-9_]{3,20}$").matches(alias.trim()))
            errors["alias"] = "Usa entre 3 y 20 letras, números o guion bajo"
        // Se conserva el mínimo de 4 caracteres de la demo para no bloquear cuentas existentes.
        if (password.length < 4) errors["password"] = "La contraseña debe tener al menos 4 caracteres"
        if (!Regex("^\\+?[0-9]{8,12}$").matches(phone.trim()))
            errors["phone"] = "Ingresa un teléfono válido de 8 a 12 dígitos"
        if (address.trim().length < 5) errors["address"] = "Ingresa una dirección de al menos 5 caracteres"
        return errors
    }

    fun validateLogin(alias: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (alias.isBlank()) errors["alias"] = "Ingresa tu alias"
        if (password.isBlank()) errors["password"] = "Ingresa tu contraseña"
        return errors
    }
}
