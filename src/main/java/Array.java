import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A generic, dynamically-resizable array backed by an {@code Object[]}.
 *
 * <p>The array grows by 50% (to at least {@code INITIAL_CAPACITY}) when it
 * becomes full and shrinks by 50% once usage drops to a quarter of its
 * capacity, keeping memory usage roughly bounded. The default constructor
 * allocates storage lazily at {@code INITIAL_CAPACITY} (10) when the first
 * element is added; an explicit initial capacity can be supplied to
 * {@link #Array(int)}.</p>
 *
 * @param <T> the type of elements held in this array
 */
public class Array<T> {
    /** The capacity allocated when backing storage is first needed. */
    private static final int INITIAL_CAPACITY = 10;
    /** Backing storage for the elements */
    private Object[] elements;
    /** The number of elements currently stored; also the next insertion index. */
    private int size = 0;

    /**
     * Creates a new, empty array whose backing storage is allocated
     * lazily with capacity {@code INITIAL_CAPACITY} (10) when the first
     * element is added.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    public Array() {}

    /**
     * Creates a new, empty array with the given initial capacity.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(n) where n is the given capacity.
     *
     * @param capacity the initial size of the backing array
     * @throws InvalidParameterException if {@code capacity} is less than 1
     */
    public Array(int capacity) {
        if (capacity < 1) {
            throw new InvalidParameterException("Capacity must be greater than 0.");
        }
        elements = new Object[capacity];
    }

    /**
     * Decrease the array size by 50%.
     *
     * Time Complexity: O(n) where n is the number of elements stored.
     * Space Complexity: O(n) where n is the new capacity.
     */
    private void shrinkArray() {
        resizeArray(elements.length >> 1);
    }

    /**
     * Increase the array size by 50%, or to the initial capacity when the
     * backing array is very small.
     *
     * Time Complexity: O(n) where n is the number of elements stored.
     * Space Complexity: O(n) where n is the new capacity.
     */
    private void growArray() {
        // Math.max is required for when a user sets their own initial capacity to 1
        int capacity = Math.max(INITIAL_CAPACITY, elements.length + (elements.length >> 1));
        resizeArray(capacity);
    }

    /**
     * Replaces the backing array with a new one of the given capacity,
     * copying the stored elements over while preserving their order and
     * indices.
     *
     * Time Complexity: O(n) where n is the number of elements copied.
     * Space Complexity: O(n) where n is the given capacity.
     *
     * @param capacity the size of the new backing array
     */
    private void resizeArray(int capacity) {
        Object[] resizedElements = new Object[capacity];
        System.arraycopy(elements, 0, resizedElements, 0, size);
        elements = resizedElements;
    }

    /**
     * Creates a new {@code Array} backed directly by the given elements.
     *
     * <p>The returned array contains exactly {@code elements}, in order, and
     * no copy of the elements array is made.</p>
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param elements the elements to place in the new array
     * @param <T>      the type of elements
     * @return a new {@code Array} containing {@code elements}
     */
    @SafeVarargs
    public static <T> Array<T> of(T... elements) {
        Array<T> arr = new Array<>();
        arr.elements = elements;
        arr.size = elements.length;
        return arr;
    }

    /**
     * Returns the number of elements currently stored in this array.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Sorts the elements currently in this array using the given comparator.
     *
     * Time Complexity: O(n logn) where n is the number of elements.
     * Space Complexity: O(n) worst case where n is the number of elements.
     *
     * @param comparator the comparator used to determine element ordering
     */
    public void sort(Comparator<T> comparator) {
        Arrays.sort((T[]) elements, 0, size, comparator);
    }

    /**
     * Determines whether this array contains the given object.
     *
     * <p>Comparison is performed using reference equality ({@code ==}).</p>
     *
     * Time Complexity: O(n) where n is the number of elements.
     * Space Complexity: O(1).
     *
     * @param obj the object to search for
     * @return {@code true} if the object is present, otherwise {@code false}
     */
    public boolean contains(T obj) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == obj) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the element at the specified index.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param index the index of the element to return
     * @return the element at the specified index
     * @throws IndexOutOfBoundsException if the index is negative or not less
     *                                   than the current size
     */
    public T at(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds!");
        }
        return (T) elements[index];
    }

    /**
     * Replaces the element at the specified index with the given object.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @param index the index of the element to replace
     * @param obj   the object to store at the specified index
     * @throws IndexOutOfBoundsException if the index is negative or not less
     *                                   than the current size
     */
    public void set(int index, T obj) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds!");
        }
        elements[index] = obj;
    }

    /**
     * Appends the given object to the end of this array.
     *
     * <p>If the backing storage has not been allocated yet (default
     * constructor), it is allocated at {@code INITIAL_CAPACITY} first.
     * If the backing array is full, it is grown before inserting.</p>
     *
     * Time Complexity: Amortized O(1).
     * Space Complexity: O(n) worst case during a grow, where n is the new
     * capacity.
     *
     * @param obj the object to add
     */
    public void add(T obj) {
        if (elements == null) {
            elements = new Object[INITIAL_CAPACITY];
        } else if (size == elements.length) {
            growArray();
        }
        elements[size] = obj;
        size += 1;
    }

    /**
     * Returns the last element in this array without modifying the array.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return the last element of the array.
     * @throws NoSuchElementException if this array is empty
     */
    public T getLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return (T) elements[size - 1];
    }

    /**
     * Removes and returns the last element in this array.
     *
     * <p>The backing array is halved in capacity when usage drops to
     * less than a quarter of its length, provided the capacity is
     * greater than {@code INITIAL_CAPACITY}.</p>
     *
     * Time Complexity: Amortized O(1).
     * Space Complexity: O(n) worst case during a shrink, where n is the new
     * capacity.
     *
     * @return the element that was removed
     * @throws NoSuchElementException if this array is empty
     */
    public T removeLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        size -= 1;
        T obj = (T) elements[size];
        elements[size] = null;
        // Less than 1 / 4 of array is used and elements.length is greater than initial capacity.
        if (size < elements.length >> 2 && elements.length != INITIAL_CAPACITY) {
            shrinkArray();
        }
        return obj;
    }

    /**
     * Determines whether this array contains no elements.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     *
     * @return {@code true} if this array is empty, otherwise {@code false}
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Replaces the backing storage with a new, empty array of capacity
     * {@code INITIAL_CAPACITY}.
     *
     * Time Complexity: O(1).
     * Space Complexity: O(1).
     */
    public void clear() {
        elements = new Object[INITIAL_CAPACITY];
        size = 0;
    }

    /**
     * Prints the elements of this array to standard output in
     * {@code [a,b,c]} format.
     *
     * Time Complexity: O(n) where n is the number of elements.
     * Space Complexity: O(n) where n is the number of elements.
     */
    public void println() {
        StringBuilder array = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            array.append(elements[i]).append(",");
        }
        if (array.length() == 1) {
            array.append("]");
        } else {
            array.replace(array.length() - 1, array.length(), "]");
        }
        IO.println(array.toString());
    }

}
