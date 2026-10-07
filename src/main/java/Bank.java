import java.math.BigDecimal;

/**
 * A bank that manages user accounts.
 *
 * <p>Accounts are held in a doubly-linked list sorted by ascending account
 * id, headed by a sentinel root node, with a tail pointer and a median
 * pointer maintained for O(1) median queries. Deleted account ids are
 * recycled by {@link #addUser} in smallest-first order via a min-heap.
 * Deposits, transfers, deletions and merges are validated against the bank
 * rules and report failures through {@link BankException}. All monetary
 * values are {@link BigDecimal}s that never use more than two decimal
 * places.</p>
 */
public class Bank {
    /** Sentinel node at the head of the account list; never removed. */
    private final Node<Account> root;
    /** Node at, or just left of, the median position in the account list. */
    private Node<Account> median;
    /** The last node in the account list. */
    private Node<Account> tail;
    /** Min-heap of closed account ids available for reuse. */
    private final Heap<Long> closedAccounts = new Heap<>();
    /** The largest account id ever issued. */
    private long accountNumber = 0;

    /**
     * Creates a new, empty bank.
     *
     * <p>The account list initially contains only the sentinel root node.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    public Bank() {
        Node<Account> node = new Node<>(new Account(null, 0));
        root = node;
        median = node;
        tail = node;
    }

    /**
     * Deposits the given amount into the account with the given id.
     *
     * Time Complexity: O(n) where n is the number of open accounts.
     * Space Complexity: O(1).
     *
     * @param id     the id of the account to deposit into
     * @param amount the amount to deposit; must be greater than 0 and use
     *               at most 2 decimal places
     * @throws BankException if the id or amount is invalid, the amount uses
     *                       more than 2 decimal places, or the account does
     *                       not exist
     */
    public void deposit(long id, BigDecimal amount) throws BankException {
        if (id <= 0) {
            throw BankException.INVALID_ACCOUNT(id);
        }
        if (amount.scale() > 2) {
            throw BankException.INVALID_PRECISION(amount);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BankException.INVALID_AMOUNT(amount);
        }

        // Find and validate account
        Node<Account> node = getNodeAt(id);
        if (node.value.getId() != id) {
            throw BankException.ACCOUNT_NOT_FOUND(id);
        }

        // Add funds to account
        node.value.addFunds(amount);
    }

    /**
     * Adds a new user to the bank, reclaiming a previously closed id if one is available.
     *
     * Time Complexity: O(n + logk) where n is the number of open accounts
     * and k is the number of closed accounts.
     * Space Complexity: O(1).
     *
     * @param user the user to add
     * @return the newly created account for the user
     */
    public Account addUser(User user) {
        assert user != null;
        Node<Account> node;
        long id;
        if (closedAccounts.isEmpty()) {
            id = ++accountNumber;
            node = new Node<>(new Account(user, id));
            node.prev = tail;
            tail.next = node;
            tail = node;
        } else {
            id = closedAccounts.poll();
            node = new Node<>(new Account(user, id));
            Node<Account> leftOfNode = getNodeAt(node.value.getId());
            Node<Account> rightOfNode = leftOfNode.next;

            leftOfNode.next = node;
            node.prev = leftOfNode;
            node.next = rightOfNode;

            // Fix: Check if inserted at the end of the list
            if (rightOfNode != null) {
                rightOfNode.prev = node;
            } else {
                tail = node;
            }
        }

        boolean isSizeEven = (accountNumber - closedAccounts.size()) % 2 == 0;
        if (!isSizeEven && id > median.value.getId()) {
            median = median.next;
        } else if (isSizeEven && id < median.value.getId()) {
            median = median.prev;
        }
        return node.value;
    }

    /**
     * Deletes the account with the given id and frees its id for reuse.
     *
     * Time Complexity: O(n + logk) where n is the number of open accounts
     * and k is the number of closed accounts.
     * Space Complexity: O(1).
     *
     * @param id the id of the account to delete
     * @throws BankException if the id is invalid or the account does not exist
     */
    public void deleteUser(long id) throws BankException {
        if (id <= 0) {
            throw BankException.INVALID_ACCOUNT(id);
        }

        Node<Account> node = getNodeAt(id);

        if (node.value.getId() != id) {
            throw BankException.ACCOUNT_NOT_FOUND(id);
        }

        removeNode(node);
    }

    /**
     * Transfers the given amount from the payer's account to the payee's account.
     *
     * Time Complexity: O(n) where n is the number of open accounts.
     * Space Complexity: O(1).
     *
     * @param payerId the id of the account paying the amount
     * @param payeeId the id of the account receiving the amount
     * @param amount  the amount to transfer; must be greater than 0 and
     *                use at most 2 decimal places
     * @throws BankException if either id or the amount is invalid, the
     *                       amount uses more than 2 decimal places, either
     *                       account does not exist, the payer has
     *                       insufficient funds, or the payer and payee are
     *                       the same account
     */
    public void payUserToUser(long payerId, long payeeId, BigDecimal amount) throws BankException {
        if (payerId <= 0) {
            throw BankException.INVALID_ACCOUNT(payerId);
        }
        if (payeeId <= 0) {
            throw BankException.INVALID_ACCOUNT(payeeId);
        }
        if (amount.scale() > 2) {
            throw BankException.INVALID_PRECISION(amount);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BankException.INVALID_AMOUNT(amount);
        }
        if (payerId == payeeId) {
            throw BankException.INVALID_TRANSACTION();
        }

        Node<Account> payer, payee;
        // Get payer id first for faster runtime
        if (payerId < payeeId) {
            // Retrieve and validate payer info
            payer = getNodeAt(payerId);
            if (payer.value.getId() != payerId) {
                throw BankException.ACCOUNT_NOT_FOUND(payerId);
            } else if (payer.value.getBalance().compareTo(amount) < 0) {
                throw BankException.INSUFFICIENT_FUNDS();
            }

            // Retrieve and validate payee info
            payee = getNodeAt(payer, payeeId);
            if (payee.value.getId() != payeeId) {
                throw BankException.ACCOUNT_NOT_FOUND(payeeId);
            }

        } else {
            // Get payee id first for faster runtime
            // Retrieve and validate payee info
            payee = getNodeAt(payeeId);
            if (payee.value.getId() != payeeId) {
                throw BankException.ACCOUNT_NOT_FOUND(payeeId);
            }

            // Retrieve and validate payer info
            payer = getNodeAt(payee, payerId);
            if (payer.value.getId() != payerId) {
                throw BankException.ACCOUNT_NOT_FOUND(payerId);
            } else if (payer.value.getBalance().compareTo(amount) < 0) {
                throw BankException.INSUFFICIENT_FUNDS();
            }
        }
        // Perform transaction
        payer.value.subtractFunds(amount);
        payee.value.addFunds(amount);
    }

    /**
     * Returns the median account id, or the average of the two middle ids if the number of
     * accounts is even.
     *
     * <p>The average is exact (either an integer or {@code x.5}), so no
     * rounding is required. A primitive {@code double} is used only here;
     * it is the sole use of a primitive floating point type in the
     * project.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the median id, or {@code -1} if the bank has no accounts
     */
    public double getMedianID() {
        if (median == root) {
            return -1;
        } else if ((accountNumber - closedAccounts.size()) % 2 == 1) {
            return median.value.getId();
        } else {
            return (median.value.getId() + median.next.value.getId()) / 2.0;
        }
    }

    /**
     * Merges the accounts with the given ids by transferring the balance of the
     * smaller account number into the larger account number.
     *
     * Time Complexity: O(n + logk) where n is the number of open accounts
     * and k is the number of closed accounts.
     * Space Complexity: O(1).
     *
     * @param id1 the id of the first account
     * @param id2 the id of the second account
     * @throws BankException if either id is invalid, either account does not exist, or the
     *                       accounts cannot be merged
     */
    public void mergeAccounts(long id1, long id2) throws BankException {
        if (id1 <= 0) {
            throw BankException.INVALID_ACCOUNT(id1);
        }
        if (id2 <= 0) {
            throw BankException.INVALID_ACCOUNT(id2);
        }
        if (id1 == id2) {
            throw BankException.INVALID_SELF_MERGE();
        }

        // Ensure id1 is smaller for faster runtime
        if (id1 > id2) {
            long tmp = id1;
            id1 = id2;
            id2 = tmp;
        }

        // Retrieve and validate id1
        Node<Account> node1 = getNodeAt(id1);
        if (node1.value.getId() != id1) {
            throw BankException.ACCOUNT_NOT_FOUND(id1);
        }

        // Retrieve and validate id2 and if id2 can merge with id1
        Node<Account> node2 = getNodeAt(node1, id2);
        if (node2.value.getId() != id2) {
            throw BankException.ACCOUNT_NOT_FOUND(id2);
        } else if (!node1.value.canMerge(node2.value)) {
            throw BankException.INVALID_ACCOUNT_MERGE(id1, id2);
        }

        // Merge accounts (transfer balance and delete larger account id)
        node1.value.addFunds(node2.value.getBalance());
        removeNode(node2);
    }

    /**
     * Merges two banks into one and returns the merged bank.
     *
     * <p>The bank with more open accounts absorbs the other: its own accounts
     * keep their ids and balances, while the other bank's open accounts are
     * re-created inside it, reusing closed ids (smallest first) and then
     * fresh ids for any remainder. On a tie, {@code b1} absorbs {@code b2}.
     * The absorbed bank is not modified.</p>
     *
     * Time Complexity: O(n1 + k1*logk1 + n2) where n1 is the number of open
     * accounts in {@code b1}, k1 is the number of closed accounts in
     * {@code b1}, and n2 is the number of open accounts in {@code b2}.
     * Space Complexity: O(1) beyond the nodes created for the merged bank.
     *
     * @param b1 the first bank
     * @param b2 the second bank
     * @return the merged bank; {@code b1} if both banks have the same number
     *         of open accounts
     * @throws AssertionError if either bank is {@code null}
     */
    public static Bank mergeBanks(Bank b1, Bank b2) {
        assert b1 != null;
        assert b2 != null;
        if (b1 == b2) {
            return b1;
        }

        // Bank 1 will have more open accounts.
        if (b1.accountNumber - b1.closedAccounts.size() < b2.accountNumber - b2.closedAccounts.size()) {
            Bank tmp = b1;
            b1 = b2;
            b2 = tmp;
        }

        Node<Account> prev = b1.root;
        Node<Account> b2Node = b2.root.next;
        Node<Account> leftOfId, newNode;
        Account account;
        long id;

        // Reuse closed account numbers for b2 accounts.
        while (b2Node != null && !b1.closedAccounts.isEmpty()) {
            id = b1.closedAccounts.poll();
            account = new Account(b2Node.value.getUser(), id, b2Node.value.getBalance());
            newNode = new Node<>(account);

            leftOfId = b1.getNodeAt(prev, id);
            Node<Account> rightOfId = leftOfId.next;

            leftOfId.next = newNode;
            newNode.prev = leftOfId;
            newNode.next = rightOfId;

            if (rightOfId == null) {
                b1.tail = newNode;
            } else {
                rightOfId.prev = newNode;
            }
            prev = newNode;
            b2Node = b2Node.next;

            boolean isSizeEven = (b1.accountNumber - b1.closedAccounts.size()) % 2 == 0;
            if (!isSizeEven && id > b1.median.value.getId()) {
                b1.median = b1.median.next;
            } else if (isSizeEven && id < b1.median.value.getId()) {
                b1.median = b1.median.prev;
            }
        }

        // Add b2 accounts to end of b1
        while (b2Node != null) {
            b1.accountNumber += 1;
            account = new Account(b2Node.value.getUser(), b1.accountNumber, b2Node.value.getBalance());
            newNode = new Node<>(account);
            newNode.prev = b1.tail;
            b1.tail.next = newNode;
            b1.tail = newNode;
            b2Node = b2Node.next;

            boolean isSizeEven = (b1.accountNumber - b1.closedAccounts.size()) % 2 == 0;
            if (!isSizeEven) {
                b1.median = b1.median.next;
            }
        }

        return b1;
    }

    /**
     * Finds the node with the given id, searching from the given start node and switching to
     * the tail if it is closer to the id.
     *
     * Time Complexity: O(n) where n is the number of open accounts.
     * Space Complexity: O(1).
     *
     * @param start the node to begin the search from
     * @param id    the id to search for
     * @return the node with the given id, or the node left of the insertion point if the id
     *         is not found
     */
    private Node<Account> getNodeAt(Node<Account> start, long id) {
        // If tail is closer to node then use that.
        if (id - start.value.getId() > Math.abs(tail.value.getId() - id)) {
            start = tail;
        }
        // Find node for given id
        while (start.next != null && start.value.getId() < id) {
            start = start.next;
        }
        // If id is not found, the node left of the insertion point will be returned
        while (start.value.getId() > id) {
            start = start.prev;
        }
        return start;
    }

    /**
     * Finds the node with the given id, searching from the root of the account list.
     *
     * Time Complexity: O(n) where n is the number of open accounts.
     * Space Complexity: O(1).
     *
     * @param id the id to search for
     * @return the node with the given id, or the node left of the insertion point if the id
     *         is not found
     */
    private Node<Account> getNodeAt(long id) {
        return getNodeAt(root, id);
    }

    /**
     * Removes the given node from the account list, closes its account, and updates the
     * median pointer.
     *
     * Time Complexity: O(logk) where k is the number of closed accounts.
     * Space Complexity: O(1).
     *
     * @param node the node to remove
     */
    private void removeNode(Node<Account> node) {
        if (node == tail) {
            tail.prev.next = null;
            tail = tail.prev;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        closedAccounts.add(node.value.getId());

        boolean isSizeEven = (accountNumber - closedAccounts.size()) % 2 == 0;
        if (isSizeEven && node.value.getId() >= median.value.getId()) {
            median = median.prev;
        } else if (!isSizeEven && node.value.getId() <= median.value.getId()) {
            median = median.next;
        }
    }

}
