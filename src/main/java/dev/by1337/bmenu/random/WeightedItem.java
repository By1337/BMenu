package dev.by1337.bmenu.random;

/**
 * Represents an item with an associated weight.
 *
 * @param <T> the type of the value
 */
public interface WeightedItem<T> {

    static <T> WeightedItem<T> of(double weight, T value) {
        return new WeightedItem<T>() {
            @Override
            public T value() {
                return value;
            }

            @Override
            public double weight() {
                return weight;
            }
        };
    }

    /**
     * Gets the value of the item.
     *
     * @return the value of the item
     */
    T value();

    /**
     * Gets the weight of the item.
     *
     * @return the weight of the item
     */
    double weight();
}