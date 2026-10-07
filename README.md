# Assignment 1: Array and LinkedList

Java implementation of a linked-list banking system for UCI SWE 240P
Assignment 1. Accounts are stored in a doubly-linked list sorted by
account id, built from scratch alongside a dynamic array, a binary
heap, and the user and account models. No built-in list or priority
queue APIs are used. Each task includes JUnit 5 sample test cases.

[TOC]

## Task 1: Model users as a linked list

Model the list of users as a linked list where each account is a node
in the list. Users must be sorted by their ID in the linked list.

Implementation: `src/main/java/Node.java` is a generic doubly-linked
node with public `value`, `next`, and `prev` fields.
`src/main/java/Bank.java` holds accounts in a doubly-linked list sorted
by ascending account id, headed by a sentinel root node, and keeps tail
and median pointers updated on every insertion and removal. Two
supporting structures are built from scratch for later tasks:
`src/main/java/Array.java`, a generic dynamic array that grows by 50%
when full and shrinks by 50% when under a quarter full, and
`src/main/java/Heap.java`, a generic binary heap backed by `Array`.

### Task 1 sample test cases

`src/test/java/NodeTests.java` validates:

*   value storage, including `null` and generic types.
*   fresh nodes have `null` neighbors.
*   manual `next`/`prev` linkage, chains, cycles, and detaching.

`src/test/java/ArrayTests.java` validates:

*   lazy allocation, growth beyond the initial capacity, and shrinking.
*   `at()`/`set()` bounds checking and insertion order.
*   reference-based `contains`, tail retrieval and removal, clearing,
    and sorting.

`src/test/java/HeapTests.java` validates:

*   min-heap behavior under the natural ordering.
*   max-heaps via `Comparator.reverseOrder()` and custom comparators.
*   duplicate and `null` elements, interleaved add/poll sequences, and
    a 100-element stress test that drains in sorted order.

## Task 2: addUser

Write a method/function `addUser(user)` that adds a new user. Notice
that the new user should have a unique ID that is either 1 more than
the last unique ID or equal to the first free-up unique ID (by a user
closing up their account), whichever comes first.

Implementation: `Bank.addUser` issues the next incremental id when no
closed ids exist, appending the account at the tail. Otherwise it polls
the smallest closed id from the `closedAccounts` min-heap and splices
the account into its sorted position in the linked list. The median
pointer is adjusted after every insert.

### Task 2 sample test cases

`src/test/java/BankTests.java` validates:

*   the first user gets id 1 and ids increment.
*   the returned account holds the user and a zero balance.
*   a deleted id is reclaimed by the next `addUser`.
*   the smallest closed id is reclaimed first, and reclaiming the
    largest id appends at the tail.
*   `addUser(null)` throws `AssertionError`.
*   the median stays correct after every add.

## Task 3: deleteUser

Write a method/function `deleteUser(ID)` that deletes an existing
user. Free up the unique ID while deleting the user. This unique ID
can be re-assigned to a future new user.

Implementation: `Bank.deleteUser` unlinks the account's node from the
list (updating tail and median pointers) and pushes the freed id into
the `closedAccounts` min-heap for reuse. Invalid ids throw
`BankException.INVALID_ACCOUNT`; unknown ids throw
`BankException.ACCOUNT_NOT_FOUND`.

### Task 3 sample test cases

`src/test/java/BankTests.java` validates:

*   ids `<= 0` throw `INVALID_ACCOUNT`.
*   deleting a missing or already-deleted id throws
    `ACCOUNT_NOT_FOUND`.
*   deleting the head, middle, or tail keeps the list and median
    consistent.
*   deleted ids are unreachable by deposit and transfer.
*   draining the whole bank leaves the median at -1.

## Task 4: payUserToUser

Write a method/function `payUserToUser(payer ID, payee ID, amount)`
that lets the user with ID1 pay the user with ID3 by amount.

Implementation: `Bank.payUserToUser` validates the payer id, payee id,
and amount (greater than 0, at most 2 decimal places), rejects
self-payment, checks the payer's balance, then moves the amount with
`Account.subtractFunds`/`addFunds` on `BigDecimal` balances. Failures
report the specific rule violated through the static factories in
`src/main/java/BankException.java`.

### Task 4 sample test cases

`src/test/java/BankTests.java` validates:

*   invalid payer/payee ids, zero/negative amounts, and amounts with
    more than 2 decimal places are rejected in the documented order.
*   self-payment throws `INVALID_TRANSACTION`.
*   missing accounts throw `ACCOUNT_NOT_FOUND`; insufficient funds
    throw `INSUFFICIENT_FUNDS`.
*   successful transfers move the exact amount, including
    exact-balance and large-amount transfers.
*   transfers work for reclaimed ids.

## Task 5: getMedianID

Write a method/function `getMedianID()` that returns the median of
all the account IDs, i.e., the middle node of the linked list. If
the number of nodes is even, then you can return the average of the
ids of the middle two nodes (return float), and you can also return
the first middle node's id.

Implementation: `Bank.getMedianID` returns the median in O(1) using
the maintained median pointer: the middle id for an odd number of
accounts and the average of the two middle ids for an even number,
returning `-1` for an empty bank.

### Task 5 sample test cases

`src/test/java/BankTests.java` validates:

*   an empty bank returns -1.
*   one through four accounts return 1, 1.5, 2, and 2.5.
*   non-contiguous ids after deletion.
*   an interleaved add/delete stress sequence.

## Task 6: mergeAccounts

Write a method/function `mergeAccounts(ID1, ID2)` that merges two
accounts into one. This function only merges two accounts if they
are owned by the same person and identified by the same name,
address, and SSN. While merging, sum the two balances, delete the
account with the biggest unique ID of the two, and keep the account
with the smallest unique ID with the new balance.

Implementation: `Bank.mergeAccounts` sums the balances and removes the
larger id via the shared removal routine. Eligibility is decided by
`Account.canMerge`, which compares owners with the value-based
`User.equals` over all fields. `src/main/java/User.java` builds users
through a validating `Builder` (name, street, state, zip, and ssn
formats, with truncation), and `src/main/java/Account.java` holds an
immutable id and a `BigDecimal` balance capped at two decimal places.

### Task 6 sample test cases

`src/test/java/BankTests.java` validates:

*   invalid or missing ids are rejected; self-merge throws
    `INVALID_SELF_MERGE`.
*   different users cannot merge; same users sum balances.
*   the smaller id survives regardless of argument order, and the
    median stays consistent.

`src/test/java/AccountTests.java` validates:

*   id immutability and `BigDecimal` balance arithmetic at up to 2
    decimal places.
*   `canMerge` for the same instance, value-equal users, and
    different users.

`src/test/java/UserTests.java` validates:

*   builder required-field enforcement and chaining.
*   setter validation and normalization (trimming, truncation,
    formats).
*   the value-based `equals` contract and hash code caching and
    invalidation.

## Task 7: mergeBanks

Imagine another bank, Bank of Los Angeles, which has the same banking
protocol and uses the same class as the Bank of Orange County. These
two banks have decided to merge into a new bank, Bank of Southern
California. Merge the two linked lists into one in the method
`mergeBanks(bankOfOrangeCounty, bankOfLosAngeles)`. If both lists
have a node with the same ID, create a new ID for one of the
duplicates and add it to the new list. While creating the new ID,
you have to maintain the incremental property.

Implementation: `Bank.mergeBanks` keeps the bank with more open
accounts, re-creates the other bank's open accounts inside it (reusing
closed ids smallest-first, then fresh incremental ids for the
remainder), and leaves the absorbed bank unmodified.

### Task 7 sample test cases

`src/test/java/BankTests.java` validates:

*   the larger bank survives, the smaller is absorbed, and ties keep
    the first bank.
*   ids are appended in order or closed ids are reused smallest-first.
*   balances survive the merge; deleted accounts are not carried over.
*   median and subsequent operations (deposit, transfer, delete,
    `addUser`) work on the merged bank.

## Build and test

Requires JDK 25 or newer. Build and run the tests with the Gradle
wrapper:

```shell
./gradlew build
./gradlew test
```

## Project layout

```text
src/main/java/Array.java
src/main/java/Heap.java
src/main/java/Node.java
src/main/java/User.java
src/main/java/Account.java
src/main/java/Bank.java
src/main/java/BankException.java
src/test/java/NodeTests.java
src/test/java/ArrayTests.java
src/test/java/HeapTests.java
src/test/java/UserTests.java
src/test/java/AccountTests.java
src/test/java/BankTests.java
```
