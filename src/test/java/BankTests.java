import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Bank}.
 *
 * <p>Tests rely on assertions being enabled (-ea is configured in build.gradle.kts).
 * All monetary values are {@link BigDecimal}s with at most 2 decimal places.</p>
 */
public class BankTests {

    private Bank bank;

    @BeforeEach
    public void setUp() {
        bank = new Bank();
    }

    /**
     * Helper method to generate a User object with the given first name and
     * ssn, using fixed defaults for the remaining fields.
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
     * Adds a user with the given first name and ssn to the bank under test
     * and returns the created account.
     */
    private Account addUser(String firstName, String ssn) {
        return bank.addUser(createUser(firstName, ssn));
    }

    /**
     * Asserts that running {@code action} throws the given bank exception,
     * comparing both the exception type and its message.
     */
    private static void assertBankException(BankException expected, Executable action) {
        BankException e = assertThrows(BankException.class, action);
        assertEquals(expected.getMessage(), e.getMessage());
    }

    /**
     * Asserts that the given BigDecimal equals the expected value, ignoring
     * differences in scale.
     */
    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    /**
     * Asserts that two BigDecimals are numerically equal, ignoring
     * differences in scale.
     */
    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    @Test
    public void testConstructor_CreatesEmptyBank() {
        assertEquals(-1.0, bank.getMedianID(), 1e-9);
    }

    // ------------------------------------------------------------------
    // deposit
    // ------------------------------------------------------------------

    @Test
    public void testDeposit_IdZero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.deposit(0, new BigDecimal("50")));
    }

    @Test
    public void testDeposit_NegativeId_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_ACCOUNT(-5), () -> bank.deposit(-5, new BigDecimal("50")));
    }

    @Test
    public void testDeposit_IdLongMinValue_ThrowsInvalidAccount() throws BankException {
        assertBankException(BankException.INVALID_ACCOUNT(Long.MIN_VALUE),
                () -> bank.deposit(Long.MIN_VALUE, new BigDecimal("50")));
    }

    @Test
    public void testDeposit_ZeroAmount_ThrowsInvalidAmount() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_AMOUNT(BigDecimal.ZERO), () -> bank.deposit(account.getId(), BigDecimal.ZERO));
    }

    @Test
    public void testDeposit_NegativeAmount_ThrowsInvalidAmount() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_AMOUNT(new BigDecimal("-10.5")), () -> bank.deposit(account.getId(), new BigDecimal("-10.5")));
    }

    @Test
    public void testDeposit_LargeAmount_Succeeds() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        BigDecimal amount = new BigDecimal("1000000000000000");
        bank.deposit(account.getId(), amount);
        assertBigDecimalEquals(amount, account.getBalance());
    }

    @Test
    public void testDeposit_NonexistentId_ThrowsAccountNotFound() throws BankException {
        assertBankException(BankException.ACCOUNT_NOT_FOUND(99), () -> bank.deposit(99, new BigDecimal("50")));
    }

    @Test
    public void testDeposit_LongMaxId_ThrowsAccountNotFound() throws BankException {
        assertBankException(BankException.ACCOUNT_NOT_FOUND(Long.MAX_VALUE),
                () -> bank.deposit(Long.MAX_VALUE, new BigDecimal("50")));
    }

    @Test
    public void testDeposit_DeletedId_ThrowsAccountNotFound() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deleteUser(account.getId());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(account.getId()),
                () -> bank.deposit(account.getId(), new BigDecimal("50")));
    }

    @Test
    public void testDeposit_AccumulatesBalance() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("100"));
        bank.deposit(account.getId(), new BigDecimal("50"));
        bank.deposit(account.getId(), new BigDecimal("0.25"));
        assertBigDecimalEquals("150.25", account.getBalance());
    }

    @Test
    public void testDeposit_ReclaimedId() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        Account middle = addUser("Carol", "333-33-3333");
        bank.deleteUser(middle.getId());
        Account reclaimed = addUser("Dave", "444-44-4444");
        bank.deposit(reclaimed.getId(), new BigDecimal("75"));
        assertBigDecimalEquals("75", reclaimed.getBalance());
    }

    @Test
    public void testDeposit_Precision() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("0.1"));
        bank.deposit(account.getId(), new BigDecimal("0.2"));
        assertBigDecimalEquals("0.3", account.getBalance());
    }

    @Test
    public void testDeposit_ThreeDecimals_ThrowsInvalidPrecision() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("0.001")),
                () -> bank.deposit(account.getId(), new BigDecimal("0.001")));
    }

    @Test
    public void testDeposit_ManyDecimals_ThrowsInvalidPrecision() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("1.23456")),
                () -> bank.deposit(account.getId(), new BigDecimal("1.23456")));
    }

    @Test
    public void testDeposit_NegativeWithThreeDecimals_ThrowsInvalidPrecision() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("-0.001")),
                () -> bank.deposit(account.getId(), new BigDecimal("-0.001")));
    }

    @Test
    public void testDeposit_ThreeDecimalsLargeAmount_ThrowsInvalidPrecision() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("1000000000000000.001")),
                () -> bank.deposit(account.getId(), new BigDecimal("1000000000000000.001")));
    }

    @Test
    public void testDeposit_TwoDecimals_Succeeds() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("50.00"));
        assertBigDecimalEquals("50.00", account.getBalance());
    }

    @Test
    public void testDeposit_OneDecimal_Succeeds() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("0.5"));
        assertBigDecimalEquals("0.5", account.getBalance());
    }

    @Test
    public void testDeposit_IntegerAmount_Succeeds() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("100"));
        assertBigDecimalEquals("100", account.getBalance());
    }

    @Test
    public void testDeposit_ThreeDecimalsRejected_LeavesBalanceUntouched() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("10"));
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("0.001")),
                () -> bank.deposit(account.getId(), new BigDecimal("0.001")));
        assertBigDecimalEquals("10", account.getBalance());
    }

    // ------------------------------------------------------------------
    // addUser
    // ------------------------------------------------------------------

    @Test
    public void testAddUser_FirstUserGetsId1() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        assertEquals(1L, account.getId());
    }

    @Test
    public void testAddUser_IncrementsIds() throws BankException {
        Account first = addUser("Alice", "111-11-1111");
        Account second = addUser("Bob", "222-22-2222");
        Account third = addUser("Carol", "333-33-3333");
        assertEquals(1L, first.getId());
        assertEquals(2L, second.getId());
        assertEquals(3L, third.getId());
    }

    @Test
    public void testAddUser_ReturnsAccountWithUserAndZeroBalance() throws BankException {
        User user = createUser("Alice", "111-11-1111");
        Account account = bank.addUser(user);
        assertEquals(user, account.getUser());
        assertBigDecimalEquals("0", account.getBalance());
    }

    @Test
    public void testAddUser_ReclaimsDeletedId() throws BankException {
        addUser("Alice", "111-11-1111");
        Account second = addUser("Bob", "222-22-2222");
        bank.deleteUser(second.getId());
        Account reclaimed = addUser("Carol", "333-33-3333");
        assertEquals(2L, reclaimed.getId());
    }

    @Test
    public void testAddUser_ReclaimsSmallestClosedIdFirst() throws BankException {
        addUser("Alice", "111-11-1111");
        Account second = addUser("Bob", "222-22-2222");
        Account third = addUser("Carol", "333-33-3333");
        bank.deleteUser(third.getId());
        bank.deleteUser(second.getId());
        assertEquals(2L, addUser("Dave", "444-44-4444").getId());
        assertEquals(3L, addUser("Eve", "555-55-5555").getId());
        assertEquals(4L, addUser("Frank", "666-66-6666").getId());
    }

    @Test
    public void testAddUser_ReclaimsLargestIdAppendsAtTail() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        Account third = addUser("Carol", "333-33-3333");
        bank.deleteUser(third.getId());
        Account reclaimed = addUser("Dave", "444-44-4444");
        assertEquals(3L, reclaimed.getId());
        bank.deposit(reclaimed.getId(), new BigDecimal("25"));
        assertBigDecimalEquals("25", reclaimed.getBalance());
        assertEquals(4L, addUser("Eve", "555-55-5555").getId());
    }

    @Test
    public void testAddUser_NullUser_ThrowsAssertionError() throws BankException {
        assertThrows(AssertionError.class, () -> bank.addUser(null));
    }

    @Test
    public void testAddUser_MedianAfterEachAdd() throws BankException {
        addUser("Alice", "111-11-1111");
        assertEquals(1.0, bank.getMedianID(), 1e-9);
        addUser("Bob", "222-22-2222");
        assertEquals(1.5, bank.getMedianID(), 1e-9);
        addUser("Carol", "333-33-3333");
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        addUser("Dave", "444-44-4444");
        assertEquals(2.5, bank.getMedianID(), 1e-9);
    }

    // ------------------------------------------------------------------
    // deleteUser
    // ------------------------------------------------------------------

    @Test
    public void testDeleteUser_IdZero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.deleteUser(0));
    }

    @Test
    public void testDeleteUser_NegativeId_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.INVALID_ACCOUNT(-3), () -> bank.deleteUser(-3));
    }

    @Test
    public void testDeleteUser_NonexistentId_ThrowsAccountNotFound() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.ACCOUNT_NOT_FOUND(7), () -> bank.deleteUser(7));
    }

    @Test
    public void testDeleteUser_DoubleDelete_ThrowsAccountNotFound() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deleteUser(account.getId());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(account.getId()),
                () -> bank.deleteUser(account.getId()));
    }

    @Test
    public void testDeleteUser_Head() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(1);
        assertBankException(BankException.ACCOUNT_NOT_FOUND(1), () -> bank.deposit(1, new BigDecimal("10")));
        assertEquals(2.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testDeleteUser_Middle() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(2);
        assertBankException(BankException.ACCOUNT_NOT_FOUND(2), () -> bank.deposit(2, new BigDecimal("10")));
        assertEquals(2.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testDeleteUser_Tail() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(3);
        assertBankException(BankException.ACCOUNT_NOT_FOUND(3), () -> bank.deposit(3, new BigDecimal("10")));
        assertEquals(1.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testDeleteUser_DeletedIdUnreachableByPayUserToUser() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        bank.deposit(payer.getId(), new BigDecimal("100"));
        bank.deleteUser(payee.getId());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(payee.getId()),
                () -> bank.payUserToUser(payer.getId(), payee.getId(), new BigDecimal("10")));
    }

    @Test
    public void testDeleteUser_AllAccounts_MedianMinusOne() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(1);
        bank.deleteUser(2);
        bank.deleteUser(3);
        assertEquals(-1.0, bank.getMedianID(), 1e-9);
    }

    // ------------------------------------------------------------------
    // payUserToUser
    // ------------------------------------------------------------------

    @Test
    public void testPayUserToUser_PayerIdZero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.payUserToUser(0, 1, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_NegativePayerId_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(-1), () -> bank.payUserToUser(-1, 1, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_PayeeIdZero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.payUserToUser(1, 0, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_NegativePayeeId_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(-2), () -> bank.payUserToUser(1, -2, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_ZeroAmount_ThrowsInvalidAmount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_AMOUNT(BigDecimal.ZERO), () -> bank.payUserToUser(1, 2, BigDecimal.ZERO));
    }

    @Test
    public void testPayUserToUser_NegativeAmount_ThrowsInvalidAmount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_AMOUNT(new BigDecimal("-5")), () -> bank.payUserToUser(1, 2, new BigDecimal("-5")));
    }

    @Test
    public void testPayUserToUser_LargeAmount_Succeeds() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        BigDecimal amount = new BigDecimal("1000000000000000");
        bank.deposit(payer.getId(), amount);
        bank.payUserToUser(payer.getId(), payee.getId(), amount);
        assertBigDecimalEquals("0", payer.getBalance());
        assertBigDecimalEquals(amount, payee.getBalance());
    }

    @Test
    public void testPayUserToUser_SameId_ThrowsInvalidTransaction() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("100"));
        assertBankException(BankException.INVALID_TRANSACTION(),
                () -> bank.payUserToUser(account.getId(), account.getId(), new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_PayerMissing_ThrowsAccountNotFound() throws BankException {
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.ACCOUNT_NOT_FOUND(9),
                () -> bank.payUserToUser(9, 1, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_PayeeMissing_ThrowsAccountNotFound() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        bank.deposit(payer.getId(), new BigDecimal("100"));
        assertBankException(BankException.ACCOUNT_NOT_FOUND(9),
                () -> bank.payUserToUser(payer.getId(), 9, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_BothMissing_PayerIdSmaller_ThrowsPayerNotFound() throws BankException {
        assertBankException(BankException.ACCOUNT_NOT_FOUND(4),
                () -> bank.payUserToUser(4, 5, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_BothMissing_PayerIdLarger_ThrowsPayeeNotFound() throws BankException {
        assertBankException(BankException.ACCOUNT_NOT_FOUND(4),
                () -> bank.payUserToUser(5, 4, new BigDecimal("10")));
    }

    @Test
    public void testPayUserToUser_InsufficientFunds_Throws() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        bank.deposit(payer.getId(), new BigDecimal("50"));
        assertBankException(BankException.INSUFFICIENT_FUNDS(),
                () -> bank.payUserToUser(payer.getId(), payee.getId(), new BigDecimal("100")));
    }

    @Test
    public void testPayUserToUser_ExactBalance_Succeeds() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        bank.deposit(payer.getId(), new BigDecimal("100"));
        bank.payUserToUser(payer.getId(), payee.getId(), new BigDecimal("100"));
        assertBigDecimalEquals("0", payer.getBalance());
        assertBigDecimalEquals("100", payee.getBalance());
    }

    @Test
    public void testPayUserToUser_SuccessfulTransfer() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        bank.deposit(payer.getId(), new BigDecimal("500"));
        bank.payUserToUser(payer.getId(), payee.getId(), new BigDecimal("200"));
        assertBigDecimalEquals("300", payer.getBalance());
        assertBigDecimalEquals("200", payee.getBalance());
    }

    @Test
    public void testPayUserToUser_ReclaimedId() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        Account extra = addUser("Carol", "333-33-3333");
        bank.deleteUser(extra.getId());
        Account reclaimedPayee = addUser("Dave", "444-44-4444");
        bank.deposit(payer.getId(), new BigDecimal("100"));
        bank.payUserToUser(payer.getId(), reclaimedPayee.getId(), new BigDecimal("40"));
        assertBigDecimalEquals("60", payer.getBalance());
        assertBigDecimalEquals("40", reclaimedPayee.getBalance());
        assertBigDecimalEquals("0", payee.getBalance());
    }

    @Test
    public void testPayUserToUser_Precedence_InvalidIdBeforeAmount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(0),
                () -> bank.payUserToUser(0, 2, new BigDecimal("-5")));
        assertBankException(BankException.INVALID_AMOUNT(new BigDecimal("-5")),
                () -> bank.payUserToUser(1, 2, new BigDecimal("-5")));
    }

    @Test
    public void testPayUserToUser_ThreeDecimals_ThrowsInvalidPrecision() throws BankException {
        Account payer = addUser("Alice", "111-11-1111");
        Account payee = addUser("Bob", "222-22-2222");
        bank.deposit(payer.getId(), new BigDecimal("100"));
        assertBankException(BankException.INVALID_PRECISION(new BigDecimal("0.001")),
                () -> bank.payUserToUser(payer.getId(), payee.getId(), new BigDecimal("0.001")));
        assertBigDecimalEquals("100", payer.getBalance());
        assertBigDecimalEquals("0", payee.getBalance());
    }

    // ------------------------------------------------------------------
    // getMedianID
    // ------------------------------------------------------------------

    @Test
    public void testGetMedianID_EmptyBank_ReturnsMinusOne() throws BankException {
        assertEquals(-1.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_SingleAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        assertEquals(1.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_TwoAccounts() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertEquals(1.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_ThreeAccounts() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        assertEquals(2.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_FourAccounts() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        addUser("Dave", "444-44-4444");
        assertEquals(2.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_NonContiguousIds() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(2);
        assertEquals(2.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_DeleteMiddleThenReaddReclaimedId() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        addUser("Carol", "333-33-3333");
        bank.deleteUser(2);
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        Account reclaimed = addUser("Dave", "444-44-4444");
        assertEquals(2L, reclaimed.getId());
        assertEquals(2.0, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testGetMedianID_InterleavedAddDeleteStress() throws BankException {
        addUser("Alice", "111-11-1111");
        assertEquals(1.0, bank.getMedianID(), 1e-9);
        addUser("Bob", "222-22-2222");
        assertEquals(1.5, bank.getMedianID(), 1e-9);
        addUser("Carol", "333-33-3333");
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        bank.deleteUser(2);
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        addUser("Dave", "444-44-4444");
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        bank.deleteUser(3);
        assertEquals(1.5, bank.getMedianID(), 1e-9);
        addUser("Eve", "555-55-5555");
        assertEquals(2.0, bank.getMedianID(), 1e-9);
        addUser("Frank", "666-66-6666");
        assertEquals(2.5, bank.getMedianID(), 1e-9);
        bank.deleteUser(1);
        assertEquals(3.0, bank.getMedianID(), 1e-9);
        bank.deleteUser(2);
        assertEquals(3.5, bank.getMedianID(), 1e-9);
        addUser("Grace", "777-77-7777");
        assertEquals(3.0, bank.getMedianID(), 1e-9);
        bank.deleteUser(1);
        bank.deleteUser(3);
        bank.deleteUser(4);
        assertEquals(-1.0, bank.getMedianID(), 1e-9);
    }

    // ------------------------------------------------------------------
    // mergeAccounts
    // ------------------------------------------------------------------

    @Test
    public void testMergeAccounts_Id1Zero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.mergeAccounts(0, 1));
    }

    @Test
    public void testMergeAccounts_NegativeId1_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(-1), () -> bank.mergeAccounts(-1, 1));
    }

    @Test
    public void testMergeAccounts_Id2Zero_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(0), () -> bank.mergeAccounts(1, 0));
    }

    @Test
    public void testMergeAccounts_NegativeId2_ThrowsInvalidAccount() throws BankException {
        addUser("Alice", "111-11-1111");
        addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT(-2), () -> bank.mergeAccounts(1, -2));
    }

    @Test
    public void testMergeAccounts_Id1Nonexistent_ThrowsAccountNotFound() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.ACCOUNT_NOT_FOUND(9), () -> bank.mergeAccounts(9, 1));
    }

    @Test
    public void testMergeAccounts_Id2Nonexistent_ThrowsAccountNotFound() throws BankException {
        addUser("Alice", "111-11-1111");
        assertBankException(BankException.ACCOUNT_NOT_FOUND(9), () -> bank.mergeAccounts(1, 9));
    }

    @Test
    public void testMergeAccounts_DifferentUsers_ThrowsInvalidMerge() throws BankException {
        Account first = addUser("Alice", "111-11-1111");
        Account second = addUser("Bob", "222-22-2222");
        assertBankException(BankException.INVALID_ACCOUNT_MERGE(first.getId(), second.getId()),
                () -> bank.mergeAccounts(first.getId(), second.getId()));
    }

    @Test
    public void testMergeAccounts_SameUser_BalancesSummed() throws BankException {
        Account first = bank.addUser(createUser("Alice", "111-11-1111"));
        Account second = bank.addUser(createUser("Alice", "111-11-1111"));
        bank.deposit(first.getId(), new BigDecimal("100"));
        bank.deposit(second.getId(), new BigDecimal("50"));
        bank.mergeAccounts(first.getId(), second.getId());
        assertBigDecimalEquals("150", first.getBalance());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(second.getId()),
                () -> bank.deposit(second.getId(), new BigDecimal("1")));
    }

    @Test
    public void testMergeAccounts_ZeroBalanceMerge() throws BankException {
        Account first = bank.addUser(createUser("Alice", "111-11-1111"));
        Account second = bank.addUser(createUser("Alice", "111-11-1111"));
        bank.mergeAccounts(first.getId(), second.getId());
        assertBigDecimalEquals("0", first.getBalance());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(second.getId()),
                () -> bank.deposit(second.getId(), new BigDecimal("1")));
    }

    @Test
    public void testMergeAccounts_MergedTail_MedianAndListConsistent() throws BankException {
        Account first = bank.addUser(createUser("Alice", "111-11-1111"));
        Account second = bank.addUser(createUser("Alice", "111-11-1111"));
        Account third = bank.addUser(createUser("Alice", "111-11-1111"));
        bank.deposit(first.getId(), new BigDecimal("100"));
        bank.deposit(third.getId(), new BigDecimal("30"));
        bank.mergeAccounts(first.getId(), third.getId());
        assertBigDecimalEquals("130", first.getBalance());
        assertBigDecimalEquals("0", second.getBalance());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(3),
                () -> bank.deposit(3, new BigDecimal("1")));
        assertEquals(1.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testMergeAccounts_MergedMiddle_MedianAndListConsistent() throws BankException {
        Account first = bank.addUser(createUser("Alice", "111-11-1111"));
        Account second = bank.addUser(createUser("Alice", "111-11-1111"));
        Account third = bank.addUser(createUser("Alice", "111-11-1111"));
        bank.deposit(second.getId(), new BigDecimal("40"));
        bank.deposit(third.getId(), new BigDecimal("30"));
        bank.mergeAccounts(second.getId(), third.getId());
        assertBigDecimalEquals("70", second.getBalance());
        assertBigDecimalEquals("0", first.getBalance());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(3),
                () -> bank.deposit(3, new BigDecimal("1")));
        assertEquals(1.5, bank.getMedianID(), 1e-9);
    }

    @Test
    public void testMergeAccounts_SelfMerge_ThrowsInvalidSelfMerge() throws BankException {
        Account account = addUser("Alice", "111-11-1111");
        bank.deposit(account.getId(), new BigDecimal("100"));
        assertBankException(BankException.INVALID_SELF_MERGE(),
                () -> bank.mergeAccounts(account.getId(), account.getId()));
        assertBigDecimalEquals("100", account.getBalance());
    }

    @Test
    public void testMergeAccounts_OrderSwap_SmallerIdSurvives() throws BankException {
        Account first = bank.addUser(createUser("Alice", "111-11-1111"));
        Account second = bank.addUser(createUser("Alice", "111-11-1111"));
        bank.deposit(first.getId(), new BigDecimal("100"));
        bank.deposit(second.getId(), new BigDecimal("30"));
        bank.mergeAccounts(second.getId(), first.getId());
        assertBigDecimalEquals("130", first.getBalance());
        assertBankException(BankException.ACCOUNT_NOT_FOUND(second.getId()),
                () -> bank.deposit(second.getId(), new BigDecimal("1")));
    }

    // ------------------------------------------------------------------
    // mergeBanks
    // ------------------------------------------------------------------

    @Test
    public void testMergeBanks_NullFirstArg_ThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> Bank.mergeBanks(null, new Bank()));
    }

    @Test
    public void testMergeBanks_NullSecondArg_ThrowsAssertionError() {
        assertThrows(AssertionError.class, () -> Bank.mergeBanks(new Bank(), null));
    }

    @Test
    public void testMergeBanks_LargerFirst_ReturnsFirstBank() {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        b1.addUser(createUser("Carol", "333-33-3333"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Dave", "444-44-4444"));
        b2.addUser(createUser("Eve", "555-55-5555"));
        Bank merged = Bank.mergeBanks(b1, b2);
        assertSame(b1, merged);
    }

    @Test
    public void testMergeBanks_SmallerFirst_ReturnsSecondBank() {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Bob", "222-22-2222"));
        b2.addUser(createUser("Carol", "333-33-3333"));
        b2.addUser(createUser("Dave", "444-44-4444"));
        assertSame(b2, Bank.mergeBanks(b1, b2));
    }

    @Test
    public void testMergeBanks_EqualCounts_ReturnsFirstBank() {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Bob", "222-22-2222"));
        assertSame(b1, Bank.mergeBanks(b1, b2));
    }

    @Test
    public void testMergeBanks_BothEmpty_ReturnsFirstBank() {
        Bank b1 = new Bank();
        Bank b2 = new Bank();
        Bank merged = Bank.mergeBanks(b1, b2);
        assertSame(b1, merged);
        assertEquals(-1.0, merged.getMedianID(), 1e-9);
    }

    @Test
    public void testMergeBanks_AppendsFreshIdsInOrder() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Carol", "333-33-3333"));
        b2.addUser(createUser("Dave", "444-44-4444"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.deposit(3, new BigDecimal("10"));
        merged.deposit(4, new BigDecimal("20"));
        assertBankException(BankException.ACCOUNT_NOT_FOUND(5), () -> merged.deposit(5, new BigDecimal("10")));
    }

    @Test
    public void testMergeBanks_ReusesClosedIdsSmallestFirst() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        b1.addUser(createUser("Carol", "333-33-3333"));
        b1.addUser(createUser("Dave", "444-44-4444"));
        b1.deleteUser(4);
        b1.deleteUser(1);
        Bank b2 = new Bank();
        b2.addUser(createUser("Eve", "555-55-5555"));
        b2.addUser(createUser("Frank", "666-66-6666"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.deposit(1, new BigDecimal("10"));
        merged.deposit(2, new BigDecimal("20"));
        merged.deposit(3, new BigDecimal("30"));
        merged.deposit(4, new BigDecimal("40"));
        assertBankException(BankException.ACCOUNT_NOT_FOUND(5), () -> merged.deposit(5, new BigDecimal("10")));
    }

    @Test
    public void testMergeBanks_MixedReuseAndFreshIds() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        b1.addUser(createUser("Carol", "333-33-3333"));
        b1.addUser(createUser("Dave", "444-44-4444"));
        b1.addUser(createUser("Eve", "555-55-5555"));
        b1.deleteUser(1);
        Bank b2 = new Bank();
        b2.addUser(createUser("Frank", "666-66-6666"));
        b2.addUser(createUser("Grace", "777-77-7777"));
        b2.addUser(createUser("Hank", "888-88-8888"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.deposit(1, new BigDecimal("10"));
        merged.deposit(2, new BigDecimal("20"));
        merged.deposit(3, new BigDecimal("30"));
        merged.deposit(4, new BigDecimal("40"));
        merged.deposit(5, new BigDecimal("50"));
        merged.deposit(6, new BigDecimal("60"));
        merged.deposit(7, new BigDecimal("70"));
        assertBankException(BankException.ACCOUNT_NOT_FOUND(8), () -> merged.deposit(8, new BigDecimal("10")));
    }

    @Test
    public void testMergeBanks_RemainingClosedIdsUsableByAddUser() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        b1.addUser(createUser("Carol", "333-33-3333"));
        b1.deleteUser(2);
        b1.deleteUser(1);
        Bank b2 = new Bank();
        b2.addUser(createUser("Dave", "444-44-4444"));
        Bank merged = Bank.mergeBanks(b1, b2);
        Account reclaimed = merged.addUser(createUser("Eve", "555-55-5555"));
        assertEquals(2L, reclaimed.getId());
        Account fresh = merged.addUser(createUser("Frank", "666-66-6666"));
        assertEquals(4L, fresh.getId());
    }

    @Test
    public void testMergeBanks_DeletedB2AccountsNotIncluded() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Carol", "333-33-3333"));
        b2.addUser(createUser("Dave", "444-44-4444"));
        b2.deleteUser(2);
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.deposit(3, new BigDecimal("10"));
        assertBankException(BankException.ACCOUNT_NOT_FOUND(4), () -> merged.deposit(4, new BigDecimal("10")));
    }

    // Regression: relocated accounts must keep their balances.
    @Test
    public void testMergeBanks_PreservesBalances_FreshIds() throws BankException {
        Bank b1 = new Bank();
        Account first = b1.addUser(createUser("Alice", "111-11-1111"));
        b1.deposit(first.getId(), new BigDecimal("100"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Bob", "222-22-2222"));
        b2.deposit(1, new BigDecimal("50"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.payUserToUser(2, 1, new BigDecimal("40"));
        assertBigDecimalEquals("140", first.getBalance());
    }

    // Regression: same as above but through a reclaimed (closed) id.
    @Test
    public void testMergeBanks_PreservesBalances_ReusedIds() throws BankException {
        Bank b1 = new Bank();
        Account first = b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        b1.deleteUser(2);
        Bank b2 = new Bank();
        b2.addUser(createUser("Carol", "333-33-3333"));
        b2.deposit(1, new BigDecimal("60"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.payUserToUser(2, 1, new BigDecimal("30"));
        assertBigDecimalEquals("30", first.getBalance());
    }

    // Regression: median must reflect the merged account set.
    @Test
    public void testMergeBanks_MedianCorrect() {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Carol", "333-33-3333"));
        b2.addUser(createUser("Dave", "444-44-4444"));
        Bank merged = Bank.mergeBanks(b1, b2);
        assertEquals(2.5, merged.getMedianID(), 1e-9);
    }

    @Test
    public void testMergeBanks_MergedBankSupportsOperations() throws BankException {
        Bank b1 = new Bank();
        Account first = b1.addUser(createUser("Alice", "111-11-1111"));
        b1.deposit(first.getId(), new BigDecimal("100"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        Bank b2 = new Bank();
        b2.addUser(createUser("Carol", "333-33-3333"));
        Bank merged = Bank.mergeBanks(b1, b2);
        merged.deposit(3, new BigDecimal("50"));
        merged.payUserToUser(1, 3, new BigDecimal("25"));
        assertBigDecimalEquals("75", first.getBalance());
        merged.deleteUser(2);
        assertBankException(BankException.ACCOUNT_NOT_FOUND(2), () -> merged.deposit(2, new BigDecimal("1")));
        Account added = merged.addUser(createUser("Dave", "444-44-4444"));
        assertEquals(2L, added.getId());
    }

    @Test
    public void testMergeBanks_SourceBankUnchanged() throws BankException {
        Bank b1 = new Bank();
        b1.addUser(createUser("Alice", "111-11-1111"));
        b1.addUser(createUser("Bob", "222-22-2222"));
        Bank b2 = new Bank();
        Account b2Account = b2.addUser(createUser("Carol", "333-33-3333"));
        b2.deposit(b2Account.getId(), new BigDecimal("42"));
        Bank.mergeBanks(b1, b2);
        assertBigDecimalEquals("42", b2Account.getBalance());
        assertEquals(1.0, b2.getMedianID(), 1e-9);
    }

    @Test
    public void testMergeBanks_SelfMerge_ReturnsSameBank() {
        Bank b = new Bank();
        b.addUser(createUser("Alice", "111-11-1111"));
        b.addUser(createUser("Bob", "222-22-2222"));
        assertSame(b, Bank.mergeBanks(b, b));
    }

    @Test
    public void testMergeBanks_SelfMerge_BankUnchanged() throws BankException {
        Bank b = new Bank();
        Account first = b.addUser(createUser("Alice", "111-11-1111"));
        Account second = b.addUser(createUser("Bob", "222-22-2222"));
        b.deposit(first.getId(), new BigDecimal("100"));
        b.deposit(second.getId(), new BigDecimal("50"));

        Bank merged = Bank.mergeBanks(b, b);

        assertSame(b, merged);
        assertBigDecimalEquals("100", first.getBalance());
        assertBigDecimalEquals("50", second.getBalance());
        assertEquals(1.5, merged.getMedianID(), 1e-9);
        assertBankException(BankException.ACCOUNT_NOT_FOUND(3), () -> merged.deposit(3, new BigDecimal("1")));
        merged.payUserToUser(first.getId(), second.getId(), new BigDecimal("25"));
        assertBigDecimalEquals("75", first.getBalance());
        assertBigDecimalEquals("75", second.getBalance());
    }
}
