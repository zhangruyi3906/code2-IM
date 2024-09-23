package com.lh.im.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImPair<K, V> {

    private K key;

    private V value;

    public ImPair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
