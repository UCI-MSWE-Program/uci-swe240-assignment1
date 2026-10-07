import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Node}.
 *
 * <p>Verifies value storage (including {@code null} and various types),
 * the default {@code null} neighbors of a fresh node, and manual
 * {@code next}/{@code prev} linkage, including chains and cycles.</p>
 */
public class NodeTests {

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    @Test
    public void testConstructor_StoresValue() {
        Node<String> node = new Node<>("hello");
        assertEquals("hello", node.value);
    }

    @Test
    public void testConstructor_NullValue_Allowed() {
        Node<String> node = new Node<>(null);
        assertNull(node.value);
    }

    @Test
    public void testConstructor_FreshNode_HasNoNeighbors() {
        Node<Integer> node = new Node<>(42);
        assertNull(node.next);
        assertNull(node.prev);
    }

    @Test
    public void testConstructor_GenericTypes() {
        Node<Integer> intNode = new Node<>(7);
        Node<Object> objNode = new Node<>(new Object());
        assertEquals(7, intNode.value);
        assertSame(objNode.value, objNode.value);
        assertNotNull(objNode.value);
    }

    // ------------------------------------------------------------------
    // value
    // ------------------------------------------------------------------

    @Test
    public void testValue_IsPublicAndMutable() {
        Node<Integer> node = new Node<>(1);
        node.value = 2;
        assertEquals(2, node.value);
        node.value = null;
        assertNull(node.value);
    }

    // ------------------------------------------------------------------
    // next / prev linkage
    // ------------------------------------------------------------------

    @Test
    public void testLinkage_TwoNodes() {
        Node<String> a = new Node<>("a");
        Node<String> b = new Node<>("b");
        a.next = b;
        b.prev = a;

        assertSame(b, a.next);
        assertSame(a, b.prev);
        assertNull(a.prev);
        assertNull(b.next);
    }

    @Test
    public void testLinkage_ThreeNodeChain() {
        Node<Integer> a = new Node<>(1);
        Node<Integer> b = new Node<>(2);
        Node<Integer> c = new Node<>(3);
        a.next = b;
        b.prev = a;
        b.next = c;
        c.prev = b;

        assertSame(b, a.next);
        assertSame(c, b.next);
        assertNull(c.next);
        assertSame(b, c.prev);
        assertSame(a, b.prev);
        assertNull(a.prev);
    }

    @Test
    public void testLinkage_TraversalForwardsAndBackwards() {
        Node<Integer> a = new Node<>(1);
        Node<Integer> b = new Node<>(2);
        Node<Integer> c = new Node<>(3);
        a.next = b;
        b.next = c;
        c.prev = b;
        b.prev = a;

        int forwardSum = 0;
        Node<Integer> current = a;
        while (current != null) {
            forwardSum += current.value;
            current = current.next;
        }
        assertEquals(6, forwardSum);

        int backwardSum = 0;
        current = c;
        while (current != null) {
            backwardSum += current.value;
            current = current.prev;
        }
        assertEquals(6, backwardSum);
    }

    @Test
    public void testLinkage_SingleNode_ForwardAndBackwardTraversalTerminates() {
        Node<Integer> node = new Node<>(5);
        assertNull(node.next);
        assertNull(node.prev);
    }

    @Test
    public void testLinkage_NextSetToNull_Detaches() {
        Node<String> a = new Node<>("a");
        Node<String> b = new Node<>("b");
        a.next = b;
        b.prev = a;

        a.next = null;
        assertNull(a.next);
        assertSame(a, b.prev, "Detaching a.next should not modify b");
    }

    @Test
    public void testLinkage_PrevSetToNull_Detaches() {
        Node<String> a = new Node<>("a");
        Node<String> b = new Node<>("b");
        a.next = b;
        b.prev = a;

        b.prev = null;
        assertNull(b.prev);
        assertSame(b, a.next, "Detaching b.prev should not modify a");
    }

    // ------------------------------------------------------------------
    // Edge cases
    // ------------------------------------------------------------------

    @Test
    public void testLinkage_SelfLoop_Allowed() {
        Node<Integer> node = new Node<>(1);
        node.next = node;
        node.prev = node;
        assertSame(node, node.next);
        assertSame(node, node.prev);
    }

    @Test
    public void testLinkage_TwoNodeCycle() {
        Node<Integer> a = new Node<>(1);
        Node<Integer> b = new Node<>(2);
        a.next = b;
        b.next = a;
        a.prev = b;
        b.prev = a;

        assertSame(b, a.next);
        assertSame(a, b.next);
        assertSame(b, a.prev);
        assertSame(a, b.prev);
    }

    @Test
    public void testLinkage_NullValueNodes_CanBeLinked() {
        Node<String> a = new Node<>(null);
        Node<String> b = new Node<>(null);
        a.next = b;
        b.prev = a;

        assertNull(a.value);
        assertNull(b.value);
        assertSame(b, a.next);
        assertSame(a, b.prev);
    }
}
