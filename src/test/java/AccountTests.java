import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Account}.
 *
 * <p>Verifies construction (including id and balance edge cases), the
 * immutability of the id, getter behavior, balance arithmetic with at most
 * 2 decimal places, and the merge compatibility rules based on
 * {@link User#equals(Object)}.</p>
 */
public class AccountTests {

    /**
     * Helper method to generate a User with the given first name and ssn,
     * using fixed defaults for the remaining fields.
     */
    private User createUser(String firstName, String ssn) {
        return new User.Builder()
                .firstName(firstName)
                .lastName("Smith")
                .street("123 Test Ave")
                .state("CA")
                .zip("54321")
                .ssn(ssn)
                .build();
    }

    /**
     * Asserts that the given BigDecimal equals the expected value, ignoring
     * differences in scale.
     */
    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    @Test
    public void testConstructor_TwoArgs_StartsAtZeroBalance() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 1);

        assertSame(user, account.getUser());
        assertEquals(1, account.getId());
        assertBigDecimalEquals("0", account.getBalance());
    }

    @Test
    public void testConstructor_ThreeArgs_StoresUserIdAndBalance() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 42, new BigDecimal("123.45"));

        assertSame(user, account.getUser());
        assertEquals(42, account.getId());
        assertBigDecimalEquals("123.45", account.getBalance());
    }

    @Test
    public void testConstructor_NullBalance_ThrowsIllegalArgumentException() {
        User user = createUser("Alice", "111-11-1111");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Account(user, 1, null));
        assertEquals("Amount must have at most 2 decimal places!", e.getMessage());
    }

    @Test
    public void testConstructor_BalanceWithThreeDecimals_Throws() {
        User user = createUser("Alice", "111-11-1111");
        assertThrows(IllegalArgumentException.class,
                () -> new Account(user, 1, new BigDecimal("0.001")));
    }

    @Test
    public void testConstructor_BalanceWithNegativeThreeDecimals_Throws() {
        User user = createUser("Alice", "111-11-1111");
        assertThrows(IllegalArgumentException.class,
                () -> new Account(user, 1, new BigDecimal("-1.234")));
    }

    @Test
    public void testConstructor_BalanceWithExactlyTwoDecimals_Accepted() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 1, new BigDecimal("0.00"));
        assertBigDecimalEquals("0.00", account.getBalance());
    }

    @Test
    public void testConstructor_NegativeScaleBalance_Accepted() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 1, new BigDecimal("1E+3"));
        assertBigDecimalEquals("1000", account.getBalance());
    }

    @Test
    public void testConstructor_ExtremeIds_Accepted() {
        User user = createUser("Alice", "111-11-1111");

        assertEquals(Long.MAX_VALUE, new Account(user, Long.MAX_VALUE).getId());
        assertEquals(Long.MIN_VALUE, new Account(user, Long.MIN_VALUE).getId());
        assertEquals(0, new Account(user, 0).getId());
        assertEquals(-1, new Account(user, -1).getId());
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    @Test
    public void testGetUser_ReturnsSameInstance() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 1);
        assertSame(user, account.getUser());
    }

    @Test
    public void testGetId_ReturnsAssignedId() {
        User user = createUser("Alice", "111-11-1111");
        assertEquals(7, new Account(user, 7).getId());
        assertEquals(-7, new Account(user, -7).getId());
    }

    @Test
    public void testGetBalance_ReturnsStartingBalance() {
        User user = createUser("Alice", "111-11-1111");
        Account account = new Account(user, 1, new BigDecimal("99.99"));
        assertBigDecimalEquals("99.99", account.getBalance());
    }

    // ------------------------------------------------------------------
    // addFunds
    // ------------------------------------------------------------------

    @Test
    public void testAddFunds_IncreasesBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.addFunds(new BigDecimal("5.50"));
        assertBigDecimalEquals("15.50", account.getBalance());
    }

    @Test
    public void testAddFunds_Zero_LeavesBalanceUnchanged() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.addFunds(BigDecimal.ZERO);
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testAddFunds_NegativeAmount_DecreasesBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.addFunds(new BigDecimal("-3.25"));
        assertBigDecimalEquals("6.75", account.getBalance());
    }

    @Test
    public void testAddFunds_NegativeAmount_AllowsNegativeBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("1.00"));
        account.addFunds(new BigDecimal("-5.00"));
        assertBigDecimalEquals("-4.00", account.getBalance());
    }

    @Test
    public void testAddFunds_RepeatedAddsAccumulate() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1);
        account.addFunds(new BigDecimal("0.01"));
        account.addFunds(new BigDecimal("0.02"));
        account.addFunds(new BigDecimal("0.03"));
        assertBigDecimalEquals("0.06", account.getBalance());
    }

    @Test
    public void testAddFunds_ExactDecimalPrecision() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1);
        account.addFunds(new BigDecimal("0.1"));
        account.addFunds(new BigDecimal("0.2"));
        assertBigDecimalEquals("0.3", account.getBalance());
    }

    @Test
    public void testAddFunds_AmountWithThreeDecimals_ThrowsAndKeepsBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> account.addFunds(new BigDecimal("0.001")));
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testAddFunds_NullAmount_ThrowsAndKeepsBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> account.addFunds(null));
        assertEquals("Amount must have at most 2 decimal places!", e.getMessage());
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testAddFunds_NegativeScaleAmount_Accepted() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1);
        account.addFunds(new BigDecimal("1E+2"));
        assertBigDecimalEquals("100", account.getBalance());
    }

    // ------------------------------------------------------------------
    // subtractFunds
    // ------------------------------------------------------------------

    @Test
    public void testSubtractFunds_DecreasesBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.subtractFunds(new BigDecimal("4.25"));
        assertBigDecimalEquals("5.75", account.getBalance());
    }

    @Test
    public void testSubtractFunds_Zero_LeavesBalanceUnchanged() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.subtractFunds(BigDecimal.ZERO);
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testSubtractFunds_NegativeAmount_IncreasesBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        account.subtractFunds(new BigDecimal("-2.00"));
        assertBigDecimalEquals("12.00", account.getBalance());
    }

    @Test
    public void testSubtractFunds_BalanceMayGoBelowZero() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("3.00"));
        account.subtractFunds(new BigDecimal("10.00"));
        assertBigDecimalEquals("-7.00", account.getBalance());
    }

    @Test
    public void testSubtractFunds_AmountWithThreeDecimals_ThrowsAndKeepsBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> account.subtractFunds(new BigDecimal("0.999")));
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testSubtractFunds_NullAmount_ThrowsAndKeepsBalance() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("10.00"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> account.subtractFunds(null));
        assertEquals("Amount must have at most 2 decimal places!", e.getMessage());
        assertBigDecimalEquals("10.00", account.getBalance());
    }

    @Test
    public void testSubtractFunds_ExactDecimalPrecision() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1, new BigDecimal("0.30"));
        account.subtractFunds(new BigDecimal("0.1"));
        account.subtractFunds(new BigDecimal("0.2"));
        assertBigDecimalEquals("0.00", account.getBalance());
    }

    // ------------------------------------------------------------------
    // canMerge
    // ------------------------------------------------------------------

    @Test
    public void testCanMerge_SameUserInstance_ReturnsTrue() {
        User user = createUser("Alice", "111-11-1111");
        Account account1 = new Account(user, 1);
        Account account2 = new Account(user, 2);
        assertTrue(account1.canMerge(account2));
    }

    @Test
    public void testCanMerge_ValueEqualUsers_ReturnsTrue() {
        User alice1 = createUser("Alice", "111-11-1111");
        User alice2 = createUser("Alice", "111-11-1111");
        Account account1 = new Account(alice1, 1);
        Account account2 = new Account(alice2, 2);
        assertTrue(account1.canMerge(account2));
    }

    @Test
    public void testCanMerge_SelfMerge_ReturnsTrue() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1);
        assertTrue(account.canMerge(account));
    }

    @Test
    public void testCanMerge_DifferentUsers_ReturnsFalse() {
        Account account1 = new Account(createUser("Alice", "111-11-1111"), 1);
        Account account2 = new Account(createUser("Bob", "222-22-2222"), 2);
        assertFalse(account1.canMerge(account2));
    }

    @Test
    public void testCanMerge_Null_ThrowsNullPointerException() {
        Account account = new Account(createUser("Alice", "111-11-1111"), 1);
        assertThrows(NullPointerException.class, () -> account.canMerge(null));
    }
}
