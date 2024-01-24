package org.javamaster.invocationlab.admin.service;

import lombok.Getter;

import java.util.StringJoiner;

/**
 * @author yudong
 */
@Getter
public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Pair.class.getSimpleName() + "[", "]")
                .add("left=" + left)
                .add("right=" + right)
                .toString();
    }

}
