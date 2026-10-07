import java.math.BigDecimal;

/**
 * A bank account owned by a {@link User}.
 *
 * <p>Each account has an immutable id and a balance stored as a
 * {@link BigDecimal} that never uses more than two decimal places. The id
 * is assigned once at construction and never changes; the only way to
 * obtain a new id is to create a new account. Funds can be added to or
 * subtracted from the balance, and two accounts may only be merged when
 * they belong to the same user.</p>
 */
public class Account {
    /** The user that owns this account. */
    private final User user;
    /**
     * The unique id of this account.
     *
     * <p>The id should never be modified after construction; a new account
     * must be created for a new account id.</p>
     */
    private final long id;
    /** The current balance of this account; never more than 2 decimal places. */
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Creates a new account for the given user with the given id and a
     * starting balance of {@code 0}.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param user the user that owns the account
     * @param id   the unique id of the account
     */
    public Account(User user, long id) {
        this(user, id, BigDecimal.ZERO);
    }

    /**
     * Creates a new account for the given user with the given id and
     * starting balance.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param user    the user that owns the account
     * @param id      the unique id of the account
     * @param balance the starting balance of the account
     * @throws IllegalArgumentException if the balance uses more than 2
     *                                  decimal places
     */
    public Account(User user, long id, BigDecimal balance) {
        requireTwoDecimals(balance);
        this.user = user;
        this.id = id;
        this.balance = balance;
    }

    /**
     * Returns the user that owns this account.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the account owner
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the unique id of this account.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the account id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the current balance of this account.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the current balance, with at most 2 decimal places
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Increases this account's balance by the given amount.
     *
     * <p>No sign validation is performed; a negative amount decreases the
     * balance.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param amount the amount to add to the balance
     * @throws IllegalArgumentException if the amount uses more than 2
     *                                  decimal places
     */
    public void addFunds(BigDecimal amount) {
        requireTwoDecimals(amount);
        balance = balance.add(amount);
    }

    /**
     * Decreases this account's balance by the given amount.
     *
     * <p>No sign validation is performed; a negative amount increases the
     * balance, and the balance may go below zero.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param amount the amount to subtract from the balance
     * @throws IllegalArgumentException if the amount uses more than 2
     *                                  decimal places
     */
    public void subtractFunds(BigDecimal amount) {
        requireTwoDecimals(amount);
        balance = balance.subtract(amount);
    }

    /**
     * Determines whether this account can be merged with the given account.
     *
     * <p>Two accounts can be merged when they are owned by the same user,
     * as determined by {@link User#equals(Object)}.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param account the account to test for merge compatibility
     * @return {@code true} if both accounts belong to the same user,
     *         otherwise {@code false}
     * @throws NullPointerException if {@code account} is {@code null}
     */
    public boolean canMerge(Account account) {
        return this.user.equals(account.user);
    }

    /**
     * Ensures the given amount uses at most 2 decimal places.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param amount the amount to validate
     * @throws IllegalArgumentException if the amount is {@code null} or uses
     *                                  more than 2 decimal places
     */
    private static void requireTwoDecimals(BigDecimal amount) {
        if (amount == null || amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must have at most 2 decimal places!");
        }
    }
}
