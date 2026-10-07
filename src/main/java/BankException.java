import java.math.BigDecimal;

/**
 * Signals an invalid bank operation.
 *
 * <p>Instances are created exclusively through the static factory methods,
 * each of which produces an exception with a message describing the specific
 * rule that was violated. Factories are used instead of singletons so that
 * every thrown exception carries a fresh stack trace pointing at the actual
 * call site.</p>
 */
public class BankException extends Exception {

    /**
     * Returns an exception indicating that the source account does not have
     * enough funds for a transfer.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return an "insufficient funds" exception
     */
    public static BankException INSUFFICIENT_FUNDS() {
        return new BankException("Insufficient funds!");
    }

    /**
     * Returns an exception indicating that the payer and payee of a transfer
     * are the same account.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return an "invalid transaction" exception
     */
    public static BankException INVALID_TRANSACTION() {
        return new BankException("Payer and payee cannot be the same account!");
    }

    /**
     * Returns an exception indicating that an account merge was attempted on
     * a single account.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return an "invalid self merge" exception
     */
    public static BankException INVALID_SELF_MERGE() {
        return new BankException("Cannot merge an account with itself!");
    }

    /**
     * Returns an exception indicating that the given amount violates the
     * deposit/transfer amount rules.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param amount the offending amount
     * @return an "invalid amount" exception naming the amount
     */
    public static BankException INVALID_AMOUNT(BigDecimal amount) {
        return new BankException("Invalid amount '" + amount + "'! Amount must be greater than 0!");
    }

    /**
     * Returns an exception indicating that the given amount uses more than
     * two decimal places.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param amount the offending amount
     * @return an "invalid precision" exception naming the amount
     */
    public static BankException INVALID_PRECISION(BigDecimal amount) {
        return new BankException("Invalid precision '" + amount + "'! Amount must have at most 2 decimal places!");
    }

    /**
     * Returns an exception indicating that the given account id violates the
     * id rules (ids must be greater than 0).
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param id the offending account id
     * @return an "invalid account" exception naming the id
     */
    public static BankException INVALID_ACCOUNT(long id) {
        return new BankException("Invalid account ID '" + id + "'! ID must be greater than 0.");
    }

    /**
     * Returns an exception indicating that no account with the given id
     * exists.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param id the id that could not be found
     * @return an "account not found" exception naming the id
     */
    public static BankException ACCOUNT_NOT_FOUND(long id) {
        return new BankException("Account ID '" + id + "' not found!");
    }

    /**
     * Returns an exception indicating that the two given accounts cannot be
     * merged because their user information does not match.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param id1 the id of the first account
     * @param id2 the id of the second account
     * @return an "invalid account merge" exception naming both ids
     */
    public static BankException INVALID_ACCOUNT_MERGE(long id1, long id2) {
        return new BankException("User info must match to merge account ID '" + id1 + "' and '" + id2 + "'!");
    }

    /**
     * Creates a new exception with the given message.
     *
     * <p>Private so that callers must use the factory methods, which encode
     * the bank's error messages in one place.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param message the detail message
     */
    private BankException(String message) {
        super(message);
    }
}
