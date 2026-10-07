import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Heap}.
 *
 * <p>Verifies min-heap behavior under the natural ordering, max-heaps via
 * {@link Comparator#reverseOrder()}, custom and null-handling comparators,
 * empty-heap operations, duplicate and null elements, interleaved
 * add/poll sequences, and clearing.</p>
 */
public class HeapTests {

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    @Test
    public void testConstructor_Default_Empty() {
        Heap<Integer> heap = new Heap<>();
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
        assertNull(heap.peek());
        assertNull(heap.poll());
    }

    @Test
    public void testConstructor_NullComparator_BehavesLikeNaturalOrder() {
        Heap<Integer> heap = new Heap<>(null);
        heap.add(5);
        heap.add(1);
        heap.add(3);
        assertEquals(1, heap.poll());
        assertEquals(3, heap.poll());
        assertEquals(5, heap.poll());
    }

    // ------------------------------------------------------------------
    // add / poll (natural order)
    // ------------------------------------------------------------------

    @Test
    public void testAddPoll_NaturalOrder_IsMinHeap() {
        Heap<Integer> heap = new Heap<>();
        heap.add(3);
        heap.add(1);
        heap.add(2);
        assertEquals(1, heap.poll());
        assertEquals(2, heap.poll());
        assertEquals(3, heap.poll());
    }

    @Test
    public void testAddPoll_UnsortedSequence_DrainsSorted() {
        Heap<Integer> heap = new Heap<>();
        heap.add(5);
        heap.add(1);
        heap.add(4);
        heap.add(2);
        heap.add(3);
        for (int expected = 1; expected <= 5; expected++) {
            assertEquals(expected, heap.poll());
        }
    }

    @Test
    public void testAddPoll_SingleElement_ThenEmpty() {
        Heap<Integer> heap = new Heap<>();
        heap.add(42);
        assertEquals(1, heap.size());
        assertEquals(42, heap.poll());
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
    }

    @Test
    public void testAddPoll_TwoElements_SiftDownAfterRootRemoval() {
        Heap<Integer> heap = new Heap<>();
        heap.add(5);
        heap.add(1);
        assertEquals(1, heap.poll());
        assertEquals(5, heap.poll());
        assertEquals(0, heap.size());
    }

    @Test
    public void testAddPoll_Duplicates_Preserved() {
        Heap<Integer> heap = new Heap<>();
        heap.add(2);
        heap.add(2);
        heap.add(2);
        assertEquals(3, heap.size());
        assertEquals(2, heap.poll());
        assertEquals(2, heap.poll());
        assertEquals(2, heap.poll());
        assertTrue(heap.isEmpty());
    }

    @Test
    public void testAddPoll_MinMaxValues() {
        Heap<Integer> heap = new Heap<>();
        heap.add(Integer.MAX_VALUE);
        heap.add(0);
        heap.add(Integer.MIN_VALUE);
        heap.add(-1);
        assertEquals(Integer.MIN_VALUE, heap.poll());
        assertEquals(-1, heap.poll());
        assertEquals(0, heap.poll());
        assertEquals(Integer.MAX_VALUE, heap.poll());
    }

    @Test
    public void testAdd_SingleNull_Allowed() {
        Heap<Integer> heap = new Heap<>();
        heap.add(null);
        assertEquals(1, heap.size());
        assertNull(heap.peek());
        assertNull(heap.poll());
    }

    @Test
    public void testAdd_TwoNulls_NaturalOrder_Throws() {
        Heap<Integer> heap = new Heap<>();
        heap.add(null);
        assertThrows(NullPointerException.class, () -> heap.add(null));
    }

    @Test
    public void testAdd_NullAfterValue_NaturalOrder_Throws() {
        Heap<Integer> heap = new Heap<>();
        heap.add(1);
        assertThrows(NullPointerException.class, () -> heap.add(null));
    }

    @Test
    public void testAddPoll_Interleaved() {
        Heap<Integer> heap = new Heap<>();
        heap.add(10);
        heap.add(20);
        assertEquals(10, heap.poll());
        heap.add(5);
        heap.add(15);
        assertEquals(5, heap.poll());
        assertEquals(15, heap.poll());
        assertEquals(20, heap.poll());
    }

    @Test
    public void testAddPoll_Stress_DrainsInSortedOrder() {
        Heap<Integer> heap = new Heap<>();
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            heap.add(random.nextInt(1000));
        }
        assertEquals(100, heap.size());
        int previous = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int current = heap.poll();
            assertTrue(current >= previous, "Heap must drain in sorted order");
            previous = current;
        }
    }

    @Test
    public void testAddPoll_AfterDrain_Reusable() {
        Heap<Integer> heap = new Heap<>();
        heap.add(2);
        heap.add(1);
        assertEquals(1, heap.poll());
        assertEquals(2, heap.poll());
        heap.add(7);
        heap.add(3);
        assertEquals(3, heap.poll());
        assertEquals(7, heap.poll());
    }

    // ------------------------------------------------------------------
    // peek
    // ------------------------------------------------------------------

    @Test
    public void testPeek_ReturnsRootWithoutRemoving() {
        Heap<Integer> heap = new Heap<>();
        heap.add(3);
        heap.add(1);
        heap.add(2);
        assertEquals(1, heap.peek());
        assertEquals(1, heap.peek());
        assertEquals(3, heap.size());
    }

    @Test
    public void testPeek_Empty_ReturnsNull() {
        assertNull(new Heap<Integer>().peek());
    }

    // ------------------------------------------------------------------
    // max heap
    // ------------------------------------------------------------------

    @Test
    public void testMaxHeap_ReverseOrder() {
        Heap<Integer> heap = new Heap<>(Comparator.reverseOrder());
        heap.add(3);
        heap.add(1);
        heap.add(2);
        assertEquals(3, heap.peek());
        assertEquals(3, heap.poll());
        assertEquals(2, heap.poll());
        assertEquals(1, heap.poll());
    }

    @Test
    public void testMaxHeap_UnsortedSequence_DrainsDescending() {
        Heap<Integer> heap = new Heap<>(Comparator.reverseOrder());
        heap.add(2);
        heap.add(5);
        heap.add(1);
        heap.add(4);
        heap.add(3);
        for (int expected = 5; expected >= 1; expected--) {
            assertEquals(expected, heap.poll());
        }
    }

    @Test
    public void testMaxHeap_Empty_ReturnsNull() {
        Heap<Integer> heap = new Heap<>(Comparator.reverseOrder());
        assertNull(heap.peek());
        assertNull(heap.poll());
    }

    // ------------------------------------------------------------------
    // custom comparators
    // ------------------------------------------------------------------

    @Test
    public void testCustomComparator_StringLength() {
        Heap<String> heap = new Heap<>(Comparator.comparingInt(String::length));
        heap.add("aaa");
        heap.add("b");
        heap.add("cc");
        assertEquals("b", heap.poll());
        assertEquals("cc", heap.poll());
        assertEquals("aaa", heap.poll());
    }

    @Test
    public void testComparator_NullsFirst_AllowsNullElements() {
        Heap<Integer> heap = new Heap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        heap.add(5);
        heap.add(null);
        heap.add(1);
        assertEquals(3, heap.size());
        assertNull(heap.poll());
        assertEquals(1, heap.poll());
        assertEquals(5, heap.poll());
    }

    // ------------------------------------------------------------------
    // size / isEmpty
    // ------------------------------------------------------------------

    @Test
    public void testSize_TracksAddsAndPolls() {
        Heap<Integer> heap = new Heap<>();
        assertEquals(0, heap.size());
        heap.add(1);
        assertEquals(1, heap.size());
        heap.add(2);
        assertEquals(2, heap.size());
        heap.poll();
        assertEquals(1, heap.size());
        heap.poll();
        assertEquals(0, heap.size());
    }

    @Test
    public void testIsEmpty_FalseAfterAdd_TrueAfterPoll() {
        Heap<Integer> heap = new Heap<>();
        assertTrue(heap.isEmpty());
        heap.add(1);
        assertFalse(heap.isEmpty());
        heap.poll();
        assertTrue(heap.isEmpty());
    }

    // ------------------------------------------------------------------
    // clear
    // ------------------------------------------------------------------

    @Test
    public void testClear_EmptiesHeap() {
        Heap<Integer> heap = new Heap<>();
        heap.add(1);
        heap.add(2);
        heap.add(3);
        heap.clear();
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
        assertNull(heap.peek());
        assertNull(heap.poll());
    }

    @Test
    public void testClear_AddAfterClear_Works() {
        Heap<Integer> heap = new Heap<>();
        heap.add(1);
        heap.add(2);
        heap.clear();
        heap.add(9);
        assertEquals(1, heap.size());
        assertEquals(9, heap.peek());
        assertEquals(9, heap.poll());
    }

    @Test
    public void testClear_EmptyHeap_NoOp() {
        Heap<Integer> heap = new Heap<>();
        heap.clear();
        assertEquals(0, heap.size());
        assertTrue(heap.isEmpty());
    }
}
