package dev.by1337.bmenu.util.map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class HashMapLike<K, V> {

    private MapLike<K, V> map = null;
    private boolean copyOnWrite;

    private HashMapLike(MapLike<K, V> map, boolean copyOnWrite) {
        this.map = map;
        this.copyOnWrite = copyOnWrite && this.map != null;
    }

    public HashMapLike(Map<K, V> map) {
        this.map = new HashMapLikeImpl<>(map);
    }

    public HashMapLike() {
    }


    public @Nullable V get(@NotNull K key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public @Nullable V put(@NotNull K key, @Nullable V value) {
        ensureMapOwnership();
        if (map instanceof OneKeyHashMapLike<K, V> oneKey) {
            if (!Objects.equals(oneKey.key, key)) {
                MapLike<K, V> newMap = new HashMapLikeImpl<>();
                newMap.put(oneKey.key, oneKey.value);
                map = newMap;
            }
        }
        if (map == null) {
            map = new OneKeyHashMapLike<>();
        }
        return map.put(key, value);
    }

    public @Nullable V remove(@NotNull K key) {
        ensureMapOwnership();
        if (map != null) {
            return map.remove(key);
        }
        return null;
    }

    private void ensureMapOwnership() {
        if (copyOnWrite) {
            copyOnWrite = false;
            if (map != null) {
                map = map.copy();
            }
        }
    }

    public HashMapLike<K, V> copy() {
        return new HashMapLike<>(map, true);
    }

    public int size() {
        if (map == null) return 0;
        return map.size();
    }

    public boolean containsKey(K key) {
        if (map == null) return false;
        return map.containsKey(key);
    }

    public void forEach(BiConsumer<? super K, ? super V> consumer) {
        if (map instanceof OneKeyHashMapLike<K, V> oneKey) {
            if (oneKey.key == null) return;
            consumer.accept(oneKey.key, oneKey.value);
        } else if (map instanceof HashMapLike.HashMapLikeImpl<K, V> hashMapLike) {
            hashMapLike.map.forEach(consumer);
        }
    }

    private interface MapLike<K, V> {
        @Nullable V get(K key);

        @Nullable V put(@NotNull K key, V value);

        @Nullable V remove(@NotNull K key);

        int size();

        MapLike<K, V> copy();

        boolean containsKey(K key);
    }

    private static class HashMapLikeImpl<K, V> implements MapLike<K, V> {
        private final Object2ObjectOpenHashMap<K, V> map;

        public HashMapLikeImpl() {
            map = new Object2ObjectOpenHashMap<>();
        }

        public HashMapLikeImpl(Map<K, V> map) {
            this.map = new Object2ObjectOpenHashMap<>(map);
        }

        public HashMapLikeImpl(Object2ObjectOpenHashMap<K, V> map) {
            this.map = map;
        }

        @Override
        public @Nullable V get(K key) {
            return map.get(key);
        }

        @Override
        public @Nullable V put(@NotNull K key, V value) {
            return map.put(key, value);
        }

        @Override
        public @Nullable V remove(@NotNull K key) {
            return map.remove(key);
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public MapLike<K, V> copy() {
            return new HashMapLikeImpl<>(new Object2ObjectOpenHashMap<>(map));
        }

        @Override
        public boolean containsKey(K key) {
            return map.containsKey(key);
        }
    }

    private static class OneKeyHashMapLike<K, V> implements MapLike<K, V> {
        private K key = null;
        private V value = null;

        public OneKeyHashMapLike() {
        }

        public OneKeyHashMapLike(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public @Nullable V get(K key) {
            if (this.key == null || !Objects.equals(this.key, key)) return null;
            return value;
        }

        @Override
        public @Nullable V put(@NotNull K key, V value) {
            if (this.key != null && !Objects.equals(this.key, key)) throw new UnsupportedOperationException();
            V old = this.value;
            this.value = value;
            this.key = key;
            return old;
        }

        @Override
        public @Nullable V remove(@NotNull K key) {
            if (Objects.equals(this.key, key)) {
                this.key = null;
                V old = this.value;
                this.value = null;
                return old;
            }
            return null;
        }

        @Override
        public int size() {
            return key == null ? 0 : 1;
        }

        @Override
        public MapLike<K, V> copy() {
            return new OneKeyHashMapLike<>(this.key, this.value);
        }

        @Override
        public boolean containsKey(K key) {
            return Objects.equals(this.key, key);
        }
    }
}
