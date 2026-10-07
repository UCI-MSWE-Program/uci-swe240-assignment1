import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link User}.
 *
 * <p>Verifies the {@link User.Builder} required-field enforcement, setter
 * validation and normalization (trimming, truncation, formats), getters,
 * the value-based {@code equals} contract, and hash code caching and
 * invalidation. Assertion-based guards require {@code -ea}, which is
 * configured in build.gradle.kts.</p>
 */
public class UserTests {

    /**
     * Helper method to generate a User.Builder populated with default values.
     */
    private User.Builder getValidUserBuilder() {
        return new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .street("123 Main Street")
                .state("CA")
                .zip("12345")
                .ssn("123-45-6789")
                .unit("Apt 1");
    }

    // ------------------------------------------------------------------
    // Builder.build
    // ------------------------------------------------------------------

    @Test
    public void testBuilder_BuildAllFields_Success() {
        User user = getValidUserBuilder().build();
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("123 Main Street", user.getStreet());
        assertEquals("CA", user.getState());
        assertEquals("12345", user.getZip());
        assertEquals("123-45-6789", user.getSsn());
        assertEquals("Apt 1", user.getUnit());
        assertNull(user.getMiddleName());
    }

    @Test
    public void testBuilder_BuildNoFields_ListsAllMissing() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new User.Builder().build());
        assertEquals("Missing parameters: first name, last name, street, state, zip, ssn", e.getMessage());
    }

    @Test
    public void testBuilder_BuildPartialFields_ListsOnlyMissing() {
        User.Builder builder = new User.Builder().firstName("John").lastName("Doe");
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Missing parameters: street, state, zip, ssn", e.getMessage());
    }

    @Test
    public void testBuilder_BuildSingleMissing_NamesThatField() {
        User.Builder builder = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .street("123 Main Street")
                .state("CA")
                .zip("12345");
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("Missing parameters: ssn", e.getMessage());
    }

    @Test
    public void testBuilder_BuildTwice_ReturnsSameInstance() {
        User.Builder builder = getValidUserBuilder();
        User first = builder.build();
        User second = builder.build();
        assertSame(first, second);
    }

    @Test
    public void testBuilder_Chaining_ReturnsSameBuilder() {
        User.Builder builder = new User.Builder();
        assertSame(builder, builder.firstName("John"));
        assertSame(builder, builder.lastName("Doe"));
        assertSame(builder, builder.street("123 Main Street"));
        assertSame(builder, builder.state("CA"));
        assertSame(builder, builder.zip("12345"));
        assertSame(builder, builder.ssn("123-45-6789"));
        assertSame(builder, builder.middleName("Ed"));
        assertSame(builder, builder.unit("Apt 1"));
    }

    // ------------------------------------------------------------------
    // setFirstName
    // ------------------------------------------------------------------

    @Test
    public void testSetFirstName_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setFirstName(null));
    }

    @Test
    public void testSetFirstName_Empty_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setFirstName(""));
        assertEquals("First name cannot be empty.", e.getMessage());
    }

    @Test
    public void testSetFirstName_Blank_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("   "));
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("\t\n"));
    }

    @Test
    public void testSetFirstName_StripsWhitespace() {
        User user = getValidUserBuilder().firstName("  John  ").build();
        assertEquals("John", user.getFirstName());
    }

    @Test
    public void testSetFirstName_TruncatesTo35Characters() {
        String name35 = "a".repeat(35);
        String name36 = "b".repeat(36);
        assertEquals(35, getValidUserBuilder().firstName(name35).build().getFirstName().length());
        User user = getValidUserBuilder().firstName(name36).build();
        assertEquals(35, user.getFirstName().length());
        assertEquals(name36.substring(0, 35), user.getFirstName());
    }

    // ------------------------------------------------------------------
    // setLastName
    // ------------------------------------------------------------------

    @Test
    public void testSetLastName_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setLastName(null));
    }

    @Test
    public void testSetLastName_Empty_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setLastName(""));
        assertEquals("Last name cannot be empty.", e.getMessage());
    }

    @Test
    public void testSetLastName_Blank_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setLastName("  "));
    }

    @Test
    public void testSetLastName_StripsWhitespace() {
        User user = getValidUserBuilder().lastName("  Doe  ").build();
        assertEquals("Doe", user.getLastName());
    }

    @Test
    public void testSetLastName_TruncatesTo35Characters() {
        User user = getValidUserBuilder().lastName("c".repeat(36)).build();
        assertEquals(35, user.getLastName().length());
    }

    // ------------------------------------------------------------------
    // setMiddleName
    // ------------------------------------------------------------------

    @Test
    public void testSetMiddleName_Null_KeepsPreviousValue() {
        User user = getValidUserBuilder().middleName("Ed").middleName(null).build();
        assertEquals("Ed", user.getMiddleName());
    }

    @Test
    public void testSetMiddleName_Blank_KeepsPreviousValue() {
        User user = getValidUserBuilder().middleName("Ed").middleName("   ").build();
        assertEquals("Ed", user.getMiddleName());
    }

    @Test
    public void testSetMiddleName_NullOnFreshUser_StaysNull() {
        User user = getValidUserBuilder().middleName(null).build();
        assertNull(user.getMiddleName());
    }

    @Test
    public void testSetMiddleName_StripsWhitespace() {
        User user = getValidUserBuilder().middleName("  Ed  ").build();
        assertEquals("Ed", user.getMiddleName());
    }

    @Test
    public void testSetMiddleName_TruncatesTo35Characters() {
        User user = getValidUserBuilder().middleName("m".repeat(36)).build();
        assertEquals(35, user.getMiddleName().length());
    }

    // ------------------------------------------------------------------
    // setStreet
    // ------------------------------------------------------------------

    @Test
    public void testSetStreet_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setStreet(null));
    }

    @Test
    public void testSetStreet_NoLeadingDigit_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setStreet("Main Street"));
        assertEquals("Street address must start with a number and be followed by alphanumeric characters.", e.getMessage());
    }

    @Test
    public void testSetStreet_DigitsOnly_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setStreet("123"));
    }

    @Test
    public void testSetStreet_InvalidCharacters_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setStreet("123 Main St."));
        assertThrows(IllegalArgumentException.class, () -> user.setStreet("123!Main"));
        assertThrows(IllegalArgumentException.class, () -> user.setStreet("123_Main"));
    }

    @Test
    public void testSetStreet_DigitThenWord_Accepted() {
        User user = getValidUserBuilder().street("5th Avenue").build();
        assertEquals("5th Avenue", user.getStreet());
    }

    @Test
    public void testSetStreet_TruncatesTo46Characters() {
        String street46 = "123 " + "a".repeat(42);
        String street47 = "123 " + "a".repeat(43);
        assertEquals(46, getValidUserBuilder().street(street46).build().getStreet().length());
        User user = getValidUserBuilder().street(street47).build();
        assertEquals(46, user.getStreet().length());
        assertEquals(street47.substring(0, 46), user.getStreet());
    }

    // ------------------------------------------------------------------
    // setUnit
    // ------------------------------------------------------------------

    @Test
    public void testSetUnit_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setUnit(null));
    }

    @Test
    public void testSetUnit_InvalidCharacters_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setUnit("apt#2"));
        assertEquals("The unit of the address must only contain alphanumeric characters.", e.getMessage());
    }

    @Test
    public void testSetUnit_Blank_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setUnit(""));
        assertThrows(IllegalArgumentException.class, () -> user.setUnit("   "));
    }

    @Test
    public void testSetUnit_StripsWhitespace() {
        User user = getValidUserBuilder().unit(" Apt 2 ").build();
        assertEquals("Apt 2", user.getUnit());
    }

    // ------------------------------------------------------------------
    // setState
    // ------------------------------------------------------------------

    @Test
    public void testSetState_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setState(null));
    }

    @Test
    public void testSetState_InvalidAbbreviation_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setState("ZZ"));
        assertEquals("Must be a valid state.", e.getMessage());
    }

    @Test
    public void testSetState_Lowercase_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setState("ca"));
    }

    @Test
    public void testSetState_ValidLiteral_Accepted() {
        assertEquals("CA", getValidUserBuilder().state("CA").build().getState());
        assertEquals("AK", getValidUserBuilder().state("AK").build().getState());
        assertEquals("WY", getValidUserBuilder().state("WY").build().getState());
    }

    @Test
    public void testSetState_NonInternedString_Throws() {
        // Array.contains compares by reference, so an equal but distinct
        // String instance is not recognized as a valid state.
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setState(new String("CA")));
    }

    // ------------------------------------------------------------------
    // setZip
    // ------------------------------------------------------------------

    @Test
    public void testSetZip_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setZip(null));
    }

    @Test
    public void testSetZip_NonNumeric_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setZip("12a4"));
        assertEquals("Must only contain numeric characters.", e.getMessage());
    }

    @Test
    public void testSetZip_Empty_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setZip(""));
    }

    @Test
    public void testSetZip_Whitespace_Throws() {
        User user = getValidUserBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> user.setZip("12345 "));
    }

    @Test
    public void testSetZip_EdgeCases_Accepted() {
        assertEquals("0", getValidUserBuilder().zip("0").build().getZip());
        assertEquals("00000", getValidUserBuilder().zip("00000").build().getZip());
        assertEquals("1234567890", getValidUserBuilder().zip("1234567890").build().getZip());
    }

    // ------------------------------------------------------------------
    // setSsn
    // ------------------------------------------------------------------

    @Test
    public void testSetSsn_Null_ThrowsAssertionError() {
        User user = getValidUserBuilder().build();
        assertThrows(AssertionError.class, () -> user.setSsn(null));
    }

    @Test
    public void testSetSsn_ValidFormat_Accepted() {
        assertEquals("123-45-6789", getValidUserBuilder().ssn("123-45-6789").build().getSsn());
    }

    @Test
    public void testSetSsn_InvalidFormats_Throws() {
        User user = getValidUserBuilder().build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> user.setSsn("123456789"));
        assertEquals("SSN does not match required format: XXX-XX-XXXX", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> user.setSsn("12-345-6789"));
        assertThrows(IllegalArgumentException.class, () -> user.setSsn("123-45-678"));
        assertThrows(IllegalArgumentException.class, () -> user.setSsn("123-45-67890"));
        assertThrows(IllegalArgumentException.class, () -> user.setSsn("abc-de-fghi"));
        assertThrows(IllegalArgumentException.class, () -> user.setSsn(""));
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    @Test
    public void testGetters_ReturnSetValues() {
        User user = getValidUserBuilder().middleName("Edward").build();
        assertEquals("John", user.getFirstName());
        assertEquals("Edward", user.getMiddleName());
        assertEquals("Doe", user.getLastName());
        assertEquals("123 Main Street", user.getStreet());
        assertEquals("Apt 1", user.getUnit());
        assertEquals("CA", user.getState());
        assertEquals("12345", user.getZip());
        assertEquals("123-45-6789", user.getSsn());
    }

    @Test
    public void testGetters_OptionalFieldsNullWhenUnset() {
        User user = getValidUserBuilder().build();
        assertNull(user.getMiddleName());
    }

    // ------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------

    @Test
    public void testEquals_Reflexive_SameInstance() {
        User user = getValidUserBuilder().build();
        assertTrue(user.equals(user));
    }

    @Test
    public void testEquals_Symmetric_EquivalentObjects() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().build();
        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user1));
    }

    @Test
    public void testEquals_NullObject_False() {
        assertFalse(getValidUserBuilder().build().equals(null));
    }

    @Test
    public void testEquals_DifferentClass_False() {
        assertFalse(getValidUserBuilder().build().equals("Not A User"));
    }

    @Test
    public void testEquals_DifferentFirstName_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().firstName("Jane").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentLastName_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().lastName("Smith").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentStreet_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().street("456 Elm Street").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentUnit_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().unit("Apt 2").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentState_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().state("NY").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentZip_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().zip("98765").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentSsn_False() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().ssn("987-65-4321").build();
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testEquals_DifferentMiddleName_False() throws Exception {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().build();

        java.lang.reflect.Field middleNameField = User.class.getDeclaredField("middleName");
        middleNameField.setAccessible(true);
        middleNameField.set(user1, "Edward");
        middleNameField.set(user2, "James");

        assertFalse(user1.equals(user2));

        middleNameField.set(user2, "Edward");
        assertTrue(user1.equals(user2));
    }

    // ------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------

    @Test
    public void testHashCode_EqualUsers_HaveEqualHashCodes() {
        User user1 = getValidUserBuilder().build();
        User user2 = getValidUserBuilder().build();
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testHashCode_CachedAfterFirstComputation() throws Exception {
        User user = getValidUserBuilder().build();
        user.hashCode();

        java.lang.reflect.Field hashField = User.class.getDeclaredField("hash");
        hashField.setAccessible(true);
        assertNotNull(hashField.get(user), "hash should be cached after hashCode() is called");
    }

    @Test
    public void testHashCode_InvalidatedBySetter() throws Exception {
        User user = getValidUserBuilder().build();
        user.hashCode();

        java.lang.reflect.Field hashField = User.class.getDeclaredField("hash");
        hashField.setAccessible(true);
        user.setFirstName("Jane");

        assertNull(hashField.get(user), "hash should be reset to null by every setter");
    }

    @Test
    public void testHashCode_RecomputedAfterMutation() throws Exception {
        User user = getValidUserBuilder().build();
        int before = user.hashCode();
        user.setZip("99999");
        int after = user.hashCode();
        assertNotEquals(before, after);
    }
}
