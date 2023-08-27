package com.backstreetbrogrammer.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Order {

    private final int orderId;
    private final String symbol;
    private final double price;
    private final int quantity;
    private final String side;

    private static final AtomicInteger counter = new AtomicInteger(1);

    public Order(final String symbol, final double price, final int quantity, final String side) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.side = side;

        orderId = counter.getAndIncrement();
    }

    public int getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSide() {
        return side;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order order = (Order) o;
        return orderId == order.orderId && Objects.equals(symbol, order.symbol) && Objects.equals(price, order.price) && Objects.equals(quantity, order.quantity) && Objects.equals(side, order.side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, symbol, price, quantity, side);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", side='" + side + '\'' +
                '}';
    }
}
