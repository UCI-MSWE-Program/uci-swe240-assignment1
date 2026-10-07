import java.util.Comparator;

/**
 * A generic binary heap backed by an {@code Array}.
 *
 * <p>The heap invariant is defined by a {@link Comparator}: for every node,
 * {@code comparator.compare(parent, child) <= 0} must hold, so the root is
 * always the element that sorts first. Elements are ordered by their natural
 * ordering unless a comparator is provided at instantiation; providing
 * {@code Comparator.reverseOrder()} produces a max-heap.</p>
 *
 * @param <T> the type of elements held in this heap
 */
public class Heap<T> {
    /** Storage for the elements, arranged in heap order. */
    private final Array<T> elements;
    /** The comparator defining the heap ordering. */
    private final Comparator<? super T> comparator;

    /**
     * Creates a new, empty heap using the natural ordering of elements.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    public Heap() {
        this(null);
    }

    /**
     * Creates a new, empty heap ordered by the given comparator.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param comparator the comparator used to determine element ordering,
     *                   or {@code null} to use the natural ordering
     */
    @SuppressWarnings("unchecked")
    public Heap(Comparator<? super T> comparator) {
        this.elements = new Array<>();
        this.comparator = comparator != null ? comparator : (Comparator<T>) Comparator.naturalOrder();
    }

    /**
     * Inserts the given element into this heap.
     *
     * <p>The element is appended at the end of the backing array and then
     * bubbled up until the heap invariant is restored.</p>
     *
     * Time Complexity: O(logn) where n is the number of elements in the heap.
     * Space Complexity: O(1).
     *
     * @param element the element to add
     */
    public void add(T element) {
        elements.add(element);
        int i = elements.size() - 1;
        while (i > 0 && before(elements.at(i), elements.at(parent(i)))) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    /**
     * Returns, without removing, the root element of this heap.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the root element, or {@code null} if this heap is empty
     */
    public T peek() {
        if (elements.isEmpty()) {
            return null;
        }
        return elements.at(0);
    }

    /**
     * Removes and returns the root element of this heap.
     *
     * <p>The last element is moved to the root and then sifted down until
     * the heap invariant is restored.</p>
     *
     * Time Complexity: O(logn) where n is the number of elements in the heap.
     * Space Complexity: O(1).
     *
     * @return the root element, or {@code null} if this heap is empty
     */
    public T poll() {
        if (elements.isEmpty()) {
            return null;
        }
        T root = elements.at(0);
        T last = elements.removeLast();
        if (!elements.isEmpty()) {
            elements.set(0, last);
            int i = 0;
            while (true) {
                int smallest = i;
                int left = leftChild(i);
                int right = rightChild(i);
                if (left < elements.size() && before(elements.at(left), elements.at(smallest))) {
                    smallest = left;
                }
                if (right < elements.size() && before(elements.at(right), elements.at(smallest))) {
                    smallest = right;
                }
                if (smallest == i) {
                    break;
                }
                swap(i, smallest);
                i = smallest;
            }
        }
        return root;
    }

    /**
     * Returns the number of elements currently stored in this heap.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the number of elements
     */
    public int size() {
        return elements.size();
    }

    /**
     * Determines whether this heap contains no elements.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return {@code true} if this heap is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Replaces this heap's backing storage with a new, empty array.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    public void clear() {
        elements.clear();
    }

    /**
     * Returns the index of the parent of the node at the given index.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param i the index of the child node
     * @return the index of the parent node
     */
    private int parent(int i) {
        return (i - 1) >> 1;
    }

    /**
     * Returns the index of the left child of the node at the given index.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param i the index of the parent node
     * @return the index of the left child node
     */
    private int leftChild(int i) {
        return (i << 1) + 1;
    }

    /**
     * Returns the index of the right child of the node at the given index.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param i the index of the parent node
     * @return the index of the right child node
     */
    private int rightChild(int i) {
        return (i << 1) + 2;
    }

    /**
     * Swaps the elements at the given indices.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param i the index of the first element
     * @param j the index of the second element
     */
    private void swap(int i, int j) {
        T temp = elements.at(i);
        elements.set(i, elements.at(j));
        elements.set(j, temp);
    }

    /**
     * Determines whether the first element sorts before or equal to the
     * second according to this heap's comparator.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param a the first element
     * @param b the second element
     * @return {@code true} if {@code a} sorts before or equal to {@code b}
     */
    private boolean before(T a, T b) {
        return comparator.compare(a, b) <= 0;
    }
}
