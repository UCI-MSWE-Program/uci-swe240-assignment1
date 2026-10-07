import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Array}.
 *
 * <p>Verifies construction (including invalid capacities and lazy
 * allocation of the default backing storage), dynamic growth and shrinking,
 * insertion order, bounds checking on
 * {@link Array#at(int)} and {@link Array#set(int, Object)}, reference-based
 * containment, retrieval from the tail, removal from the tail, clearing, and
 * sorting.</p>
 */
public class ArrayTests {

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    @Test
    public void testConstructor_Default_Empty() {
        Array<Integer> array = new Array<>();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    @Test
    public void testConstructor_DefaultCapacity_HoldsTwoElements() {
        Array<Integer> array = new Array<>();
        array.add(1);
        array.add(2);
        assertEquals(2, array.size());
        assertEquals(1, array.at(0));
        assertEquals(2, array.at(1));
    }

    @Test
    public void testConstructor_ExplicitCapacity_Empty() {
        Array<Integer> array = new Array<>(100);
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    @Test
    public void testConstructor_ExplicitCapacity_AcceptsAdds() {
        Array<Integer> array = new Array<>(100);
        array.add(7);
        assertEquals(1, array.size());
        assertEquals(7, array.at(0));
    }

    @Test
    public void testConstructor_NegativeCapacity_ThrowsInvalidParameterException() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> new Array<>(-1));
        assertEquals("Capacity must be greater than 0.", e.getMessage());
    }

    @Test
    public void testConstructor_ZeroCapacity_ThrowsInvalidParameterException() {
        InvalidParameterException e = assertThrows(InvalidParameterException.class, () -> new Array<>(0));
        assertEquals("Capacity must be greater than 0.", e.getMessage());
    }

    @Test
    public void testConstructor_CapacityOne_Empty() {
        Array<Integer> array = new Array<>(1);
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    // ------------------------------------------------------------------
    // add
    // ------------------------------------------------------------------

    @Test
    public void testAdd_AppendsInOrder() {
        Array<Integer> array = new Array<>();
        array.add(10);
        array.add(20);
        array.add(30);
        assertEquals(3, array.size());
        assertEquals(10, array.at(0));
        assertEquals(20, array.at(1));
        assertEquals(30, array.at(2));
    }

    @Test
    public void testAdd_GrowsBeyondDefaultCapacity() {
        Array<Integer> array = new Array<>();
        array.add(1);
        array.add(2);
        array.add(3);
        assertEquals(3, array.size());
        assertEquals(1, array.at(0));
        assertEquals(3, array.at(2));
    }

    @Test
    public void testAdd_ManyElements() {
        Array<Integer> array = new Array<>();
        for (int i = 0; i < 100; i++) {
            array.add(i);
        }
        assertEquals(100, array.size());
        assertEquals(0, array.at(0));
        assertEquals(99, array.at(99));
    }

    @Test
    public void testAdd_NullElement_Allowed() {
        Array<Integer> array = new Array<>();
        array.add(null);
        assertEquals(1, array.size());
        assertNull(array.at(0));
        assertTrue(array.contains(null));
    }

    @Test
    public void testAdd_AfterRemoveLast_ReusesSlot() {
        Array<Integer> array = new Array<>();
        array.add(1);
        array.add(2);
        assertEquals(2, array.removeLast());
        array.add(3);
        assertEquals(2, array.size());
        assertEquals(1, array.at(0));
        assertEquals(3, array.at(1));
    }

    @Test
    public void testAdd_CapacityOne_GrowsOnSecondAdd() {
        Array<Integer> array = new Array<>(1);
        array.add(1);
        array.add(2);
        assertEquals(2, array.size());
        assertEquals(2, array.at(1));
    }

    @Test
    public void testAdd_DefaultConstructor_LazilyAllocatesAndGrows() {
        Array<Integer> array = new Array<>();
        for (int i = 1; i <= 11; i++) {
            array.add(i);
        }
        assertEquals(11, array.size());
        assertEquals(1, array.at(0));
        assertEquals(11, array.at(10));
    }

    @Test
    public void testAdd_CapacityFive_GrowsOnSixthAdd() {
        Array<Integer> array = new Array<>(5);
        for (int i = 1; i <= 6; i++) {
            array.add(i);
        }
        assertEquals(6, array.size());
        assertEquals(6, array.at(5));
    }

    // ------------------------------------------------------------------
    // of
    // ------------------------------------------------------------------

    @Test
    public void testOf_ContainsElementsInOrder() {
        Array<String> array = Array.of("a", "b", "c");
        assertEquals(3, array.size());
        assertEquals("a", array.at(0));
        assertEquals("b", array.at(1));
        assertEquals("c", array.at(2));
    }

    @Test
    public void testOf_Empty() {
        Array<Integer> array = Array.of();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    @Test
    public void testOf_Empty_SupportsAddingElements() {
        Array<Integer> array = Array.of();
        array.add(5);
        assertEquals(1, array.size());
        assertEquals(5, array.at(0));
    }

    @Test
    public void testOf_BacksOriginalArrayWithoutCopy() {
        Integer[] source = {1, 2, 3};
        Array<Integer> array = Array.of(source);
        source[0] = 77;
        assertEquals(77, array.at(0), "Mutating the source array should be visible through of()");
    }

    @Test
    public void testOf_SupportsAddingMoreElements() {
        Array<Integer> array = Array.of(1, 2);
        array.add(3);
        assertEquals(3, array.size());
        assertEquals(3, array.at(2));
    }

    // ------------------------------------------------------------------
    // size / isEmpty
    // ------------------------------------------------------------------

    @Test
    public void testSize_TracksAddsAndRemovals() {
        Array<Integer> array = new Array<>();
        assertEquals(0, array.size());
        array.add(1);
        assertEquals(1, array.size());
        array.removeLast();
        assertEquals(0, array.size());
    }

    @Test
    public void testIsEmpty_FalseAfterAdd() {
        Array<Integer> array = new Array<>();
        array.add(1);
        assertFalse(array.isEmpty());
    }

    @Test
    public void testIsEmpty_TrueAfterDraining() {
        Array<Integer> array = new Array<>();
        array.add(1);
        array.removeLast();
        assertTrue(array.isEmpty());
    }

    // ------------------------------------------------------------------
    // at
    // ------------------------------------------------------------------

    @Test
    public void testAt_ReturnsElementAtEachIndex() {
        Array<String> array = Array.of("x", "y", "z");
        assertEquals("x", array.at(0));
        assertEquals("y", array.at(1));
        assertEquals("z", array.at(2));
    }

    @Test
    public void testAt_NegativeIndex_Throws() {
        Array<Integer> array = new Array<>();
        array.add(1);
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> array.at(-1));
        assertEquals("Index -1 is out of bounds!", e.getMessage());
    }

    @Test
    public void testAt_IndexEqualToSize_Throws() {
        Array<Integer> array = new Array<>();
        array.add(1);
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> array.at(1));
        assertEquals("Index 1 is out of bounds!", e.getMessage());
    }

    @Test
    public void testAt_EmptyArray_Throws() {
        Array<Integer> array = new Array<>();
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> array.at(0));
        assertEquals("Index 0 is out of bounds!", e.getMessage());
    }

    @Test
    public void testAt_FarOutOfBounds_Throws() {
        Array<Integer> array = new Array<>();
        array.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> array.at(Integer.MAX_VALUE));
    }

    // ------------------------------------------------------------------
    // set
    // ------------------------------------------------------------------

    @Test
    public void testSet_ReplacesElement_SizeUnchanged() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.set(1, 20);
        assertEquals(3, array.size());
        assertEquals(1, array.at(0));
        assertEquals(20, array.at(1));
        assertEquals(3, array.at(2));
    }

    @Test
    public void testSet_CanStoreNull() {
        Array<Integer> array = Array.of(1, 2);
        array.set(0, null);
        assertNull(array.at(0));
        assertTrue(array.contains(null));
    }

    @Test
    public void testSet_NegativeIndex_Throws() {
        Array<Integer> array = new Array<>();
        array.add(1);
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> array.set(-1, 5));
        assertEquals("Index -1 is out of bounds!", e.getMessage());
    }

    @Test
    public void testSet_IndexEqualToSize_Throws() {
        Array<Integer> array = new Array<>();
        array.add(1);
        IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class, () -> array.set(1, 5));
        assertEquals("Index 1 is out of bounds!", e.getMessage());
    }

    @Test
    public void testSet_EmptyArray_Throws() {
        Array<Integer> array = new Array<>();
        assertThrows(IndexOutOfBoundsException.class, () -> array.set(0, 5));
    }

    // ------------------------------------------------------------------
    // contains
    // ------------------------------------------------------------------

    @Test
    public void testContains_UsesReferenceEquality_EqualButDistinctIsFalse() {
        Array<String> array = new Array<>();
        array.add(new String("abc"));
        assertFalse(array.contains(new String("abc")));
    }

    @Test
    public void testContains_SameInstance_True() {
        String value = "abc";
        Array<String> array = new Array<>();
        array.add(value);
        assertTrue(array.contains(value));
    }

    @Test
    public void testContains_AbsentElement_False() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertFalse(array.contains(4));
    }

    @Test
    public void testContains_EmptyArray_False() {
        Array<Integer> array = new Array<>();
        assertFalse(array.contains(null));
        assertFalse(array.contains(1));
    }

    @Test
    public void testContains_RemovedElement_False() {
        Array<Integer> array = Array.of(1, 2);
        array.removeLast();
        assertFalse(array.contains(2));
        assertTrue(array.contains(1));
    }

    // ------------------------------------------------------------------
    // sort
    // ------------------------------------------------------------------

    @Test
    public void testSort_NaturalOrder() {
        Array<Integer> array = Array.of(5, 1, 4, 2, 3);
        array.sort(Comparator.naturalOrder());
        assertEquals(5, array.size());
        assertEquals(1, array.at(0));
        assertEquals(2, array.at(1));
        assertEquals(3, array.at(2));
        assertEquals(4, array.at(3));
        assertEquals(5, array.at(4));
    }

    @Test
    public void testSort_ReverseOrder() {
        Array<Integer> array = Array.of(2, 4, 1, 3);
        array.sort(Comparator.reverseOrder());
        assertEquals(4, array.at(0));
        assertEquals(3, array.at(1));
        assertEquals(2, array.at(2));
        assertEquals(1, array.at(3));
    }

    @Test
    public void testSort_EmptyDefaultArray_ThrowsNullPointerException() {
        // The default constructor leaves the backing storage unallocated
        // (null) until the first add, so sorting touches a null array.
        Array<Integer> array = new Array<>();
        assertThrows(NullPointerException.class, () -> array.sort(Comparator.naturalOrder()));
    }

    @Test
    public void testSort_EmptyOfArray_NoOp() {
        Array<Integer> array = Array.of();
        array.sort(Comparator.naturalOrder());
        assertEquals(0, array.size());
    }

    @Test
    public void testSort_SingleElement_NoOp() {
        Array<Integer> array = new Array<>();
        array.add(1);
        array.sort(Comparator.naturalOrder());
        assertEquals(1, array.size());
        assertEquals(1, array.at(0));
    }

    @Test
    public void testSort_NullElements_WithNullsFirstComparator() {
        Array<Integer> array = new Array<>();
        array.add(5);
        array.add(null);
        array.add(1);
        array.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        assertNull(array.at(0));
        assertEquals(1, array.at(1));
        assertEquals(5, array.at(2));
    }

    @Test
    public void testSort_NullElement_WithNaturalComparator_Throws() {
        Array<Integer> array = new Array<>();
        array.add(5);
        array.add(null);
        array.add(1);
        assertThrows(NullPointerException.class, () -> array.sort(Comparator.naturalOrder()));
    }

    // ------------------------------------------------------------------
    // getLast
    // ------------------------------------------------------------------

    @Test
    public void testGetLast_ReturnsLastWithoutModifying() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertEquals(3, array.getLast());
        assertEquals(3, array.size());
        assertEquals(3, array.at(2));
    }

    @Test
    public void testGetLast_RepeatedCalls_Consistent() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertEquals(3, array.getLast());
        assertEquals(3, array.getLast());
        assertEquals(3, array.getLast());
        assertEquals(3, array.size());
    }

    @Test
    public void testGetLast_SingleElement() {
        Array<Integer> array = new Array<>();
        array.add(42);
        assertEquals(42, array.getLast());
        assertEquals(1, array.size());
    }

    @Test
    public void testGetLast_AfterRemoveLast() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.removeLast();
        assertEquals(2, array.getLast());
        assertEquals(2, array.size());
    }

    @Test
    public void testGetLast_AfterAdd() {
        Array<Integer> array = Array.of(1, 2);
        array.add(3);
        assertEquals(3, array.getLast());
    }

    @Test
    public void testGetLast_EmptyArray_Throws() {
        Array<Integer> array = new Array<>();
        assertThrows(NoSuchElementException.class, () -> array.getLast());
    }

    @Test
    public void testGetLast_EmptyOfArray_Throws() {
        Array<Integer> array = Array.of();
        assertThrows(NoSuchElementException.class, () -> array.getLast());
    }

    @Test
    public void testGetLast_AfterClear_Throws() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.clear();
        assertThrows(NoSuchElementException.class, () -> array.getLast());
    }

    @Test
    public void testGetLast_AfterRemoveAll_Throws() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.removeLast();
        array.removeLast();
        array.removeLast();
        assertThrows(NoSuchElementException.class, () -> array.getLast());
    }

    // ------------------------------------------------------------------
    // removeLast
    // ------------------------------------------------------------------

    @Test
    public void testRemoveLast_ReturnsLastAndDecrements() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertEquals(3, array.removeLast());
        assertEquals(2, array.size());
        assertEquals(1, array.at(0));
        assertEquals(2, array.at(1));
    }

    @Test
    public void testRemoveLast_DrainsInReverseOrder() {
        Array<Integer> array = Array.of(1, 2, 3);
        assertEquals(3, array.removeLast());
        assertEquals(2, array.removeLast());
        assertEquals(1, array.removeLast());
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    @Test
    public void testRemoveLast_SingleElement_ThenEmpty() {
        Array<Integer> array = new Array<>();
        array.add(42);
        assertEquals(42, array.removeLast());
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }

    @Test
    public void testRemoveLast_EmptyArray_Throws() {
        Array<Integer> array = new Array<>();
        assertThrows(NoSuchElementException.class, () -> array.removeLast());
    }

    @Test
    public void testRemoveLast_Shrink_RemainingElementsPreserved() {
        Array<Integer> array = new Array<>(8);
        for (int i = 1; i <= 8; i++) {
            array.add(i);
        }
        array.removeLast(); // i=7
        array.removeLast(); // i=6
        array.removeLast(); // i=5
        array.removeLast(); // i=4
        array.removeLast(); // i=3
        array.removeLast(); // i=2
        assertEquals(2, array.removeLast()); // i=1 < 8 >> 2 -> shrink to 4
        assertEquals(1, array.at(0), "Remaining element must survive the shrink");
        assertEquals(1, array.size());
    }

    @Test
    public void testRemoveLast_ShrinkToEmpty_ArrayStillUsable() {
        Array<Integer> array = new Array<>(4);
        array.add(1);
        array.add(2);
        array.add(3);
        array.add(4);
        assertEquals(4, array.removeLast());
        assertEquals(3, array.removeLast());
        assertEquals(2, array.removeLast());
        assertEquals(1, array.removeLast()); // i=0 < 4 >> 2 -> shrink to 2
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        array.add(100);
        assertEquals(100, array.at(0));
    }

    @Test
    public void testRemoveLast_MixedAddRemove_Cycle() {
        Array<Integer> array = new Array<>();
        for (int i = 0; i < 50; i++) {
            array.add(i);
        }
        for (int i = 49; i >= 25; i--) {
            assertEquals(i, array.removeLast());
        }
        assertEquals(25, array.size());
        assertEquals(24, array.at(24));
        array.add(1000);
        assertEquals(26, array.size());
        assertEquals(1000, array.at(25));
    }

    @Test
    public void testRemoveLast_InitialCapacity_NeverShrinks_StillUsable() {
        Array<Integer> array = new Array<>(10);
        for (int i = 1; i <= 10; i++) {
            array.add(i);
        }
        for (int i = 10; i >= 1; i--) {
            assertEquals(i, array.removeLast());
        }
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        array.add(100);
        assertEquals(100, array.at(0));
    }

    // ------------------------------------------------------------------
    // clear
    // ------------------------------------------------------------------

    @Test
    public void testClear_EmptiesArray() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.clear();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        assertFalse(array.contains(1));
    }

    @Test
    public void testClear_AddAfterClear_Works() {
        Array<Integer> array = Array.of(1, 2, 3);
        array.clear();
        array.add(5);
        assertEquals(1, array.size());
        assertEquals(5, array.at(0));
    }

    @Test
    public void testClear_EmptyArray_NoOp() {
        Array<Integer> array = new Array<>();
        array.clear();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
    }
}
