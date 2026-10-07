/**
 * A node in a doubly-linked list.
 *
 * @param <T> the type of the value held in this node
 */
public class Node<T> {
    /** The value held by this node. */
    public T value;
    /** The next node in the list, or {@code null} if this is the last node. */
    public Node<T> next;
    /** The previous node in the list, or {@code null} if this is the first node. */
    public Node<T> prev;

    /**
     * Creates a new node holding the given value with no neighbors.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param value the value to store in the node
     */
    public Node(T value) {
        this.value = value;
    }
}
