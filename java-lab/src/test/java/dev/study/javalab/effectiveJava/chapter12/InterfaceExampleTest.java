package dev.study.javalab.effectiveJava.chapter12;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterfaceExampleTest {
    @Test
    void changingThePolicyChangesThePriceWithoutChangingOrderService() {
        var fixedOrder = new OrderService(new FixedDiscount());
        var percentOrder = new OrderService(new PercentDiscount());

        assertEquals(19000, fixedOrder.finalPrice(20000));
        assertEquals(18000, percentOrder.finalPrice(20000));
    }

    @Test
    void bothPoliciesKeepTheSameContract() {
        for (DiscountPolicy policy : new DiscountPolicy[]{new FixedDiscount(), new PercentDiscount()}) {
            var order = new OrderService(policy);
            for (int price : new int[]{0, 1, 999, 1000, 1001, 20000, Integer.MAX_VALUE}) {
                int discount = policy.discount(price);
                assertTrue(discount >= 0 && discount <= price);
                assertEquals(price - discount, order.finalPrice(price));
            }
            assertEquals(0, order.finalPrice(0));
            assertThrows(IllegalArgumentException.class, () -> order.finalPrice(-1));
        }
    }

    @Test
    void lowPricesAndRoundingHaveExplicitResults() {
        assertEquals(0, new OrderService(new FixedDiscount()).finalPrice(500));
        assertEquals(901, new OrderService(new PercentDiscount()).finalPrice(1001));
        assertThrows(NullPointerException.class, () -> new OrderService(null));
    }
}
