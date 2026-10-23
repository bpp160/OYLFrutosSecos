package com.oyl.frutossecos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationValidatorTest {
    @Test fun registroVacioMuestraErroresPorCampo() {
        val errors = RegistrationValidator.validate("", "", "", "", "")
        assertEquals(setOf("name", "alias", "password", "phone", "address"), errors.keys)
    }

    @Test fun registroValidoNoMuestraErrores() {
        val errors = RegistrationValidator.validate(
            "Ana Perez", "ana_123", "clave123", "912345678", "Av Siempre Viva 123"
        )
        assertTrue(errors.isEmpty())
    }

    @Test fun telefonoIncorrectoMuestraError() {
        val errors = RegistrationValidator.validate(
            "Ana Perez", "ana_123", "clave123", "abc", "Av Siempre Viva 123"
        )
        assertTrue(errors.containsKey("phone"))
    }

    @Test fun loginVacioMuestraErrores() {
        assertEquals(setOf("alias", "password"), RegistrationValidator.validateLogin("", "").keys)
        assertFalse(RegistrationValidator.validateLogin("ana_123", "clave123").isNotEmpty())
    }
}
