package pe.edu.upc.safelab.userprofiles

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileChanges
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.ProfileSaveResult
import pe.edu.upc.safelab.boundedcontext.userprofiles.domain.model.validateProfile

class ProfileValidationTest {
    private val valid = ProfileChanges(
        firstNames = "Ever Giusephi", lastNames = "Carlos Lavado",
        email = "ever@gmail.com", phone = "987654321",
        age = "", city = "", jobTitle = ""
    )

    @Test fun acceptsAccentedNamesAndOptionalFields() {
        assertEquals(ProfileSaveResult.SUCCESS,
            validateProfile(valid.copy(firstNames = "José Andrés", age = "22", city = "Trujillo")))
    }

    @Test fun rejectsNumbersInNames() {
        assertEquals(ProfileSaveResult.INVALID_FIRST_NAMES, validateProfile(valid.copy(firstNames = "Ever123")))
    }

    @Test fun rejectsUnsupportedEmail() {
        assertEquals(ProfileSaveResult.INVALID_EMAIL, validateProfile(valid.copy(email = "not-an-email")))
    }

    @Test fun rejectsInvalidPhone() {
        assertEquals(ProfileSaveResult.INVALID_PHONE, validateProfile(valid.copy(phone = "123")))
    }

    @Test fun rejectsInvalidAge() {
        assertEquals(ProfileSaveResult.INVALID_AGE, validateProfile(valid.copy(age = "121")))
    }
}
