package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import logica.ValidacionUbicaciones;
import ubicacion.Ubicacion;

class ValidacionUbicacionesTest {

	@Test
	void coordenadasAceptanComaDecimalYEspacios() {

		Ubicacion u = ValidacionUbicaciones.validarCoordenadas(" -42,77 ", "-65.04", "Origen");

		assertEquals(-42.77, u.getLatitud(), 1e-9);
		assertEquals(-65.04, u.getLongitud(), 1e-9);
	}

	@Test
	void coordenadasInvalidasIndicanCualEs() {

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ValidacionUbicaciones.validarCoordenadas("abc", "-65", "Destino"));
		assertTrue(e.getMessage().contains("destino"));

		assertThrows(IllegalArgumentException.class, () -> ValidacionUbicaciones.validarCoordenadas("", "-65", "Origen"));
		assertThrows(IllegalArgumentException.class, () -> ValidacionUbicaciones.validarCoordenadas(null, "-65", "Origen"));
		assertThrows(IllegalArgumentException.class, () -> ValidacionUbicaciones.validarCoordenadas("95", "-65", "Origen"));
		assertThrows(IllegalArgumentException.class, () -> ValidacionUbicaciones.validarCoordenadas("NaN", "-65", "Origen"));
	}

	@Test
	void fueraDelAreaSeRechaza() {

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ValidacionUbicaciones.validarYAjustar(new Ubicacion(40.0, -3.0), new MotorFalso()));
		assertTrue(e.getMessage().contains("fuera del area"));
	}

	@Test
	void sinCalleCercanaSeRechazaYConCalleSeAjusta() {

		MotorFalso motor = new MotorFalso();
		Ubicacion dentro = new Ubicacion(-42.77, -65.04);

		assertEquals(dentro, ValidacionUbicaciones.validarYAjustar(dentro, motor));

		motor.hayCalles = false;
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ValidacionUbicaciones.validarYAjustar(dentro, motor));
		assertTrue(e.getMessage().contains("calle"));
	}
}
